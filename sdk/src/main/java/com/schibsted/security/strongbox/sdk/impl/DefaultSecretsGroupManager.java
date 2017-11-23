/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.impl;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.schibsted.security.strongbox.sdk.internal.access.IAMPolicyManager;
import com.schibsted.security.strongbox.sdk.internal.encryption.Encryptor;
import com.schibsted.security.strongbox.sdk.internal.encryption.FileEncryptionContext;
import com.schibsted.security.strongbox.sdk.exceptions.AlreadyExistsException;
import com.schibsted.security.strongbox.sdk.exceptions.DoesNotExistException;
import com.schibsted.security.strongbox.sdk.exceptions.FailedToDeleteResourceException;
import com.schibsted.security.strongbox.sdk.exceptions.SecretsGroupException;
import com.schibsted.security.strongbox.sdk.exceptions.UnexpectedStateException;
import com.schibsted.security.strongbox.sdk.exceptions.UnsupportedTypeException;
import com.schibsted.security.strongbox.sdk.internal.srn.SecretsGroupSRN;
import com.schibsted.security.strongbox.sdk.internal.impl.DefaultSecretsGroup;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generated.DynamoDB;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generated.File;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Store;
import com.schibsted.security.strongbox.sdk.types.*;
import com.schibsted.security.strongbox.sdk.internal.types.config.UserConfig;
import com.schibsted.security.strongbox.sdk.internal.encryption.KMSEncryptor;
import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.SecretsGroupManager;
import com.schibsted.security.strongbox.sdk.internal.types.store.StorageType;
import com.schibsted.security.strongbox.sdk.internal.types.store.DynamoDBReference;
import com.schibsted.security.strongbox.sdk.internal.types.store.FileReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.schibsted.security.strongbox.sdk.internal.types.store.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author stiankri
 * @author kvlees
 * @author torarvid
 * @author hawkaa
 */
public class DefaultSecretsGroupManager implements SecretsGroupManager {
    private static final Logger log = LoggerFactory.getLogger(DefaultSecretsGroupManager.class);

    private final AWSCredentialsProvider awsCredentials;
    private final IAMPolicyManager policyManager;
    private final UserConfig userConfig;
    private final EncryptionStrength encryptionStrength;
    private final ClientConfiguration clientConfiguration;

    private final ConcurrentHashMap<SecretsGroupIdentifier, ReadWriteLock> readWriteLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<SecretsGroupIdentifier, KMSEncryptor> encryptors = new ConcurrentHashMap<>();

    public DefaultSecretsGroupManager() {
        this(new DefaultAWSCredentialsProviderChain());
    }

    public DefaultSecretsGroupManager(AWSCredentialsProvider awsCredentials) {
        this(awsCredentials, new UserConfig(), EncryptionStrength.AES_128, getDefaultClientConfiguration());
    }

    public DefaultSecretsGroupManager(AWSCredentialsProvider awsCredentials, UserConfig userConfig) {
        this(awsCredentials, userConfig, EncryptionStrength.AES_128, getDefaultClientConfiguration());
    }

    public DefaultSecretsGroupManager(AWSCredentialsProvider awsCredentials, UserConfig userConfig, EncryptionStrength encryptionStrength) {
        this(awsCredentials, userConfig, encryptionStrength, getDefaultClientConfiguration());
    }

    public DefaultSecretsGroupManager(
        AWSCredentialsProvider awsCredentials,
        UserConfig userConfig,
        EncryptionStrength encryptionStrength,
        ClientConfiguration clientConfiguration) {
        this.awsCredentials = awsCredentials;
        this.clientConfiguration = clientConfiguration;
        policyManager = IAMPolicyManager.fromCredentials(awsCredentials, this.clientConfiguration);
        this.userConfig = userConfig;
        this.encryptionStrength = encryptionStrength;
    }

    private static ClientConfiguration getDefaultClientConfiguration() {
        return new ClientConfiguration();
    }

    public SRN srn(SecretsGroupIdentifier group) {
        return new SecretsGroupSRN(policyManager.getAccount(), group);
    }

    @Override
    public SecretsGroupInfo create(SecretsGroupIdentifier group) {
        return create(group, new DynamoDBReference());
    }

    public SecretsGroupInfo create(SecretsGroupIdentifier group, StorageReference storageReference) {
        return create(group, storageReference, false);
    }

    public SecretsGroupInfo create(SecretsGroupIdentifier group, StorageReference storageReference, boolean allowExistingPendingDeletedOrDisabledKey) {
        synchronized (readWriteLocks) {
            ReadWriteLock readWriteLock = getReadWriteLock(group);
            readWriteLock.writeLock().lock();

            try {
                final KMSEncryptor kmsEncryptor = getEncryptor(group);

                // This method is checking and creating resources in parallel, and since we have already taken the lock globally,
                // it is ok that the threads spawned from this method are not sharing the global lock.
                // TODO: consider rewriting the code so that the lock is used from this thread, making the local lock unnecessary.
                final ReadWriteLock localReadWriteLock = new ReentrantReadWriteLock();

                verifyThatNonOfTheResourcesExistsOrThrow(group, kmsEncryptor, localReadWriteLock, allowExistingPendingDeletedOrDisabledKey);

                ExecutorService executor = Executors.newFixedThreadPool(6);
                try {
                    Future<Store> storeFuture = executor.submit(() -> {
                        Store store = createStore(group, storageReference, localReadWriteLock);
                        setLocalState(group, storageReference);
                        return  store;
                    });

                    Future<Void> encryptorFuture = executor.submit((Callable<Void>) () -> {
                        kmsEncryptor.create(allowExistingPendingDeletedOrDisabledKey);
                        return null;
                    });

                    encryptorFuture.get();
                    final Store store = storeFuture.get();

                    Future<String> adminArnFuture = executor.submit(() -> policyManager.createAdminPolicy(group, kmsEncryptor, store));
                    Future<String> readOnlyArnFuture = executor.submit(() -> policyManager.createReadOnlyPolicy(group, kmsEncryptor, store));

                    String adminArn = adminArnFuture.get();
                    String readOnlyArn = readOnlyArnFuture.get();

                    SecretsGroupSRN secretsGroupSRN = new SecretsGroupSRN(policyManager.getAccount(), group);

                    return new SecretsGroupInfo(secretsGroupSRN, Optional.of(kmsEncryptor.getArn()), Optional.of(store.getArn()),
                            Optional.of(adminArn), Optional.of(readOnlyArn), new ArrayList<>(), new ArrayList<>());
                } catch (InterruptedException | ExecutionException e) {
                    throw new SecretsGroupException(group, "Failed to create group: this might have left a partially constructed group, which can be deleted.", e);
                } finally {
                    executor.shutdownNow();
                }
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }
    }

    private ReadWriteLock getReadWriteLock(SecretsGroupIdentifier group) {
        ReadWriteLock newLock = new ReentrantReadWriteLock();
        ReadWriteLock existingLock = readWriteLocks.putIfAbsent(group, new ReentrantReadWriteLock());
        return existingLock != null ? existingLock : newLock;
    }

    private void verifyThatNonOfTheResourcesExistsOrThrow(final SecretsGroupIdentifier group,
                                                          final KMSEncryptor encryptor,
                                                          final ReadWriteLock readWriteLock,
                                                          boolean allowExistingPendingDeletedOrDisabledKey) {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            Future<Optional<StorageType>> storageExistsFuture = executor.submit(() -> storageExists(group, readWriteLock));
            Future<Boolean> encryptorExistsFuture = executor.submit(() -> encryptor.exists(allowExistingPendingDeletedOrDisabledKey));
            Future<Boolean> adminPolicyExistsFuture = executor.submit(() -> policyManager.adminPolicyExists(group));
            Future<Boolean> readOnlyPolicyExistsFuture = executor.submit(() -> policyManager.readOnlyPolicyExists(group));

            Optional<StorageType> storageType = storageExistsFuture.get();
            if (storageType.isPresent()) {
                throw new AlreadyExistsException(String.format("There already exists a storage backend for the group '%s' of type '%s'", group, storageType.get()));
            }

            if (adminPolicyExistsFuture.get()) {
                throw new AlreadyExistsException(String.format("There already exists an admin policy for the group '%s'", group));
            }

            if (readOnlyPolicyExistsFuture.get()) {
                throw new AlreadyExistsException(String.format("There already exists a read only policy for the group '%s'", group));
            }

            if (encryptorExistsFuture.get()) {
                throw new AlreadyExistsException(String.format("There already exists an encryptor backend for the group '%s'. Please note that it takes %d days for a key to be deleted. If you intend to reuse the key, use the '--allow-key-reuse' flag.", group, encryptor.pendingDeletionWindowInDays()));
            }
        } catch (AlreadyExistsException e) {
            throw new SecretsGroupException(group, "The group already exists", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new SecretsGroupException(group, "Failed to verify if the group already exists", e);
        } finally {
            executor.shutdownNow();
        }
    }

    // TODO: infer storage type: if not in overwrite, try DynamoDB then s3? pinning?
    private Store getCurrentStore(SecretsGroupIdentifier group, ReadWriteLock readWriteLock) {
        if (userConfig.getLocalFilePath(group).isPresent()) {
            // TODO: load encryptor once
            final KMSEncryptor kmsEncryptor = getEncryptor(group);
            return new File(userConfig.getLocalFilePath(group).get(), kmsEncryptor, new FileEncryptionContext(group), readWriteLock);
        }
        try {
            DynamoDB dynamoDB = DynamoDB.fromCredentials(awsCredentials, clientConfiguration, group, readWriteLock);
            return dynamoDB;
        } catch (ResourceNotFoundException e) {
            throw new DoesNotExistException("No storage backend found!", e);
        }
    }

    private StorageReference getCurrentStorageReference(SecretsGroupIdentifier group) {
        if (userConfig.getLocalFilePath(group).isPresent()) {
            return new FileReference(userConfig.getLocalFilePath(group).get());
        } else {
            return new DynamoDBReference();
        }
    }

    private Optional<StorageType> storageExists(SecretsGroupIdentifier group, ReadWriteLock readWriteLock) {
        if (userConfig.getLocalFilePath(group).isPresent()) {
            final KMSEncryptor kmsEncryptor = getEncryptor(group);
            File file = new File(userConfig.getLocalFilePath(group).get(), kmsEncryptor, new FileEncryptionContext(group), readWriteLock);

            return file.exists() ? Optional.of(StorageType.FILE) : Optional.empty();
        } else {
            DynamoDB dynamoDB = DynamoDB.fromCredentials(awsCredentials, clientConfiguration, group, readWriteLock);
            return dynamoDB.exists() ? Optional.of(StorageType.DYNAMODB) : Optional.empty();
        }
    }

    private Store createStore(SecretsGroupIdentifier group, StorageReference storageReference, ReadWriteLock readWriteLock) {
        if (storageReference instanceof DynamoDBReference) {
            DynamoDB store = DynamoDB.fromCredentials(awsCredentials, clientConfiguration, group, readWriteLock);
            store.create();
            return store;
        } else if (storageReference instanceof FileReference) {
            FileReference fileReference = (FileReference) storageReference;
            final KMSEncryptor kmsEncryptor = getEncryptor(group);
            File store = new File(fileReference.path, kmsEncryptor, new FileEncryptionContext(group), readWriteLock);
            store.create();
            return store;
        } else {
            throw new UnsupportedTypeException(storageReference.getClass().getName());
        }
    }


    private KMSEncryptor getEncryptor(SecretsGroupIdentifier group) {
        return encryptors.computeIfAbsent(group, k -> KMSEncryptor.fromCredentials(awsCredentials, clientConfiguration, group, this.encryptionStrength));
    }

    public Encryptor encryptor(SecretsGroupIdentifier group) {
        return getEncryptor(group);
    }

    private void setLocalState(SecretsGroupIdentifier group, StorageReference storageReference) {
        if (storageReference instanceof FileReference) {
            FileReference fileReference = (FileReference) storageReference;

            java.io.File newPath = fileReference.path;
            userConfig.addLocalFilePath(group, newPath);
        }
    }

    private void removeLocalState(SecretsGroupIdentifier group, Store store) {
        if (store instanceof File) {
            userConfig.removeLocalFilePath(group);
        }
    }

    @Override
    public SecretsGroup get(SecretsGroupIdentifier group) {
        ReadWriteLock readWriteLock = getReadWriteLock(group);
        Store store = getCurrentStore(group, readWriteLock);
        KMSEncryptor encryptor = getEncryptor(group);
        return new DefaultSecretsGroup(getAccount(), group, store, encryptor, readWriteLock);
    }

    @Override
    public Set<SecretsGroupIdentifier> identifiers() {
        synchronized (readWriteLocks) {
            IAMPolicyManager policyManager = IAMPolicyManager.fromCredentials(awsCredentials, clientConfiguration);
            return policyManager.getSecretsGroupIdentifiers();
        }
    }

    @Override
    public SecretsGroupInfo info(SecretsGroupIdentifier group) {
        ReadWriteLock readWriteLock = getReadWriteLock(group);
        readWriteLock.readLock().lock();

        try {
            ExecutorService executor = Executors.newFixedThreadPool(6);

            KMSEncryptor kmsEncryptor = getEncryptor(group);
            Future<Optional<String>> kmsArnFuture = executor.submit(() -> {
                if (kmsEncryptor.exists()) {
                    return Optional.of(kmsEncryptor.getArn());
                } else {
                    return Optional.empty();
                }
            });

            Future<Optional<String>> storeArnFuture = executor.submit(() -> {
                try {
                    Store store = getCurrentStore(group, readWriteLock);
                    if (store.exists()) {
                        return Optional.of(store.getArn());
                    } else {
                        return Optional.empty();
                    }
                } catch (DoesNotExistException e) {
                    return Optional.empty();
                }
            });

            Future<Optional<String>> adminPolicyArnFuture = executor.submit(() -> {
                if (policyManager.adminPolicyExists(group)) {
                    return Optional.of(policyManager.getAdminPolicyArn(group));
                } else {
                    return Optional.empty();
                }
            });

            Future<Optional<String>> readOnlyPolicyArnFuture = executor.submit(() -> {
                if (policyManager.readOnlyPolicyExists(group)) {
                    return Optional.of(policyManager.getReadOnlyArn(group));
                } else {
                    return Optional.empty();
                }
            });

            Future<List<Principal>> adminFuture = executor.submit(() -> {
                try {
                    return policyManager.listAttachedAdmin(group);
                } catch (DoesNotExistException e) {
                    return new ArrayList<>();
                }
            });

            Future<List<Principal>> readOnlyFuture = executor.submit(() -> {
                try {
                    return policyManager.listAttachedReadOnly(group);
                } catch (DoesNotExistException e) {
                    return new ArrayList<>();
                }
            });

            executor.shutdown();

            try {
                Optional<String> kmsArn = kmsArnFuture.get();
                Optional<String> storeArn = storeArnFuture.get();
                Optional<String> adminPolicyArn = adminPolicyArnFuture.get();
                Optional<String> readOnlyPolicyArn = readOnlyPolicyArnFuture.get();

                List<Principal> admin = adminFuture.get();
                List<Principal> readOnly = readOnlyFuture.get();

                SecretsGroupSRN secretsGroupSRN = new SecretsGroupSRN(policyManager.getAccount(), group);

                return new SecretsGroupInfo(secretsGroupSRN, kmsArn, storeArn, adminPolicyArn, readOnlyPolicyArn, admin, readOnly);
            } catch (InterruptedException | ExecutionException e) {
                throw new SecretsGroupException(group, "Error getting group information", e);
            }
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public void delete(SecretsGroupIdentifier group) {
        synchronized (readWriteLocks) {
            ReadWriteLock readWriteLock = getReadWriteLock(group);
            readWriteLock.readLock().lock();

            try {
                log.info("About to delete Secrets Group: {}", group.name);
                ExecutorService executor = Executors.newFixedThreadPool(3);

                executor.submit((Callable<Void>) () -> {
                    try {
                        // We have already taken a global lock, and we want this thread to proceed independently
                        final ReadWriteLock localReadWriteLock = new ReentrantReadWriteLock();
                        Store store = getCurrentStore(group, localReadWriteLock);
                        store.delete();
                        removeLocalState(group, store);
                        log.info("  Deleted Store");
                    } catch (DoesNotExistException e) {
                        // Ignore
                    }
                    return null;
                });

                executor.submit((Callable<Void>) () -> {
                    try {
                        policyManager.detachAllPrincipals(group);
                        log.info("  Detached all Principals from the IAM Policies");
                        policyManager.deleteAdminPolicy(group);
                        log.info("  Deleted Admin Policy");
                        policyManager.deleteReadonlyPolicy(group);
                        log.info("  Deleted Readonly Policy");
                    } catch (DoesNotExistException e) {
                        // Ignore
                    }
                    return null;
                });

                executor.submit((Callable<Void>) () -> {
                    try {
                        KMSEncryptor kmsEncryptor = getEncryptor(group);
                        kmsEncryptor.delete();
                        log.info(String.format("  Scheduled KMS key for deletion in %s days", kmsEncryptor.pendingDeletionWindowInDays()));
                    } catch (DoesNotExistException | UnexpectedStateException e) {
                        // Ignore
                    }
                    return null;
                });

                executor.shutdown();

                // TODO: make this more robust; when it times out or gets interrupted, the threads might still carry on in the background even though we release the lock
                int timeoutValue = 2;
                TimeUnit timeoutUnit = TimeUnit.MINUTES;
                if (!executor.awaitTermination(timeoutValue, timeoutUnit)) {
                    throw new InterruptedException(String.format("Timeout of %d %s was reached when deleting resources for the group '%s'. This might have left the system in a dirty state.", timeoutValue, timeoutUnit.name(), group.name));
                }
            } catch (InterruptedException e) {
                throw new FailedToDeleteResourceException(String.format("Deletion of group '%s' was interrupted, this might have left the resources in a dirty state.", group.name), e);
            } finally {
                readWriteLock.readLock().unlock();
            }
        }
    }

    @Override
    public void attachAdmin(SecretsGroupIdentifier group, Principal principal) {
        ReadWriteLock readWriteLock = getReadWriteLock(group);
        readWriteLock.writeLock().lock();

        try {
            policyManager.attachAdmin(group, principal);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void detachAdmin(SecretsGroupIdentifier group, Principal principal) {
        ReadWriteLock readWriteLock = getReadWriteLock(group);
        readWriteLock.writeLock().lock();

        try {
            policyManager.detachAdmin(group, principal);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void detachReadOnly(SecretsGroupIdentifier group, Principal principal) {
        ReadWriteLock readWriteLock = getReadWriteLock(group);
        readWriteLock.writeLock().lock();

        try {
            policyManager.detachReadOnly(group, principal);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void attachReadOnly(SecretsGroupIdentifier group, Principal principal) {
        ReadWriteLock readWriteLock = getReadWriteLock(group);
        readWriteLock.writeLock().lock();

        try {
            policyManager.attachReadOnly(group, principal);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private String getAccount() {
        return policyManager.getAccount();
    }

    // TODO: should this be in the interface?
    public void backup(SecretsGroupIdentifier group, Store backupStore, boolean failIfBackupStoreAlreadyExists) {
        ReadWriteLock readWriteLock = getReadWriteLock(group);
        readWriteLock.writeLock().lock();

        try {
            Store currentStore = getCurrentStore(group, readWriteLock);

            if (backupStore.exists()) {
                if (failIfBackupStoreAlreadyExists) {
                    throw new AlreadyExistsException("The store to backup to already exists");
                }
                backupStore.delete();
            }
            backupStore.create();
            currentStore.stream().forEach(backupStore::create);
            backupStore.close();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public void restore(SecretsGroupIdentifier group, Store backupStore, boolean failIfStoreToRestoreAlreadyExists) {
        ReadWriteLock readWriteLock = getReadWriteLock(group);
        readWriteLock.writeLock().lock();

        try {
            Store currentStore = getCurrentStore(group, readWriteLock);
            if (currentStore.exists()) {
                if (failIfStoreToRestoreAlreadyExists) {
                    throw new AlreadyExistsException("The store to restore already exists");
                }
                currentStore.delete();
            }
            currentStore.create();
            backupStore.stream().forEach(currentStore::create);
            currentStore.close();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public SecretsGroupInfo migrate(SecretsGroupIdentifier group, StorageReference newStorageReference) {
        ReadWriteLock readWriteLock = getReadWriteLock(group);
        readWriteLock.writeLock().lock();

        try {
            StorageReference currentStorageReference = getCurrentStorageReference(group);
            if (currentStorageReference.equals(newStorageReference)) {
                throw new IllegalStateException("You cannot migrate to the same backend!");
            }

            Store currentStore = getCurrentStore(group, readWriteLock);
            try (Store newStore = createStore(group, newStorageReference, readWriteLock)) {
                currentStore.stream().forEach(newStore::create);
            }

            if (newStorageReference instanceof FileReference) {
                setLocalState(group, newStorageReference);
            } else {
                removeLocalState(group, currentStore);
            }
            currentStore.delete();

            return info(group);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
