/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.impl;

import com.schibsted.security.strongbox.sdk.exceptions.AlreadyExistsException;
import com.schibsted.security.strongbox.sdk.internal.encryption.BestEffortShredder;
import com.schibsted.security.strongbox.sdk.internal.encryption.DefaultEncryptionContext;
import com.schibsted.security.strongbox.sdk.internal.encryption.EncryptionContext;
import com.schibsted.security.strongbox.sdk.internal.encryption.EncryptionPayload;
import com.schibsted.security.strongbox.sdk.internal.encryption.Encryptor;
import com.schibsted.security.strongbox.sdk.exceptions.PotentiallyMaliciousDataException;
import com.schibsted.security.strongbox.sdk.exceptions.DoesNotExistException;
import com.schibsted.security.strongbox.sdk.types.SRN;
import com.schibsted.security.strongbox.sdk.internal.srn.SecretSRN;
import com.schibsted.security.strongbox.sdk.internal.converter.FormattedTimestamp;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Store;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.KVStream;
import com.schibsted.security.strongbox.sdk.SecretsGroup;
import com.schibsted.security.strongbox.sdk.types.NewSecretEntry;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretMetadata;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.types.State;
import com.schibsted.security.strongbox.sdk.types.UserAlias;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;

import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.name;
import static com.schibsted.security.strongbox.sdk.internal.kv4j.generated.Config.version;

/**
 * @author stiankri
 * @author kvlees
 */
public class DefaultSecretsGroup implements SecretsGroup {
    private final Store store;
    private final Encryptor encryptor;
    private final SecretsGroupIdentifier groupIdentifier;
    private final String account;
    private final ReadWriteLock readWriteLock;

    public DefaultSecretsGroup(String account,
                               SecretsGroupIdentifier groupIdentifier,
                               Store store,
                               Encryptor encryptor,
                               ReadWriteLock readWriteLock) {
        this.store = store;
        this.encryptor = encryptor;
        this.groupIdentifier = groupIdentifier;
        this.account = account;
        this.readWriteLock = readWriteLock;
    }

    @Override
    public SRN srn(SecretIdentifier secretIdentifier) {
        return new SecretSRN(account, groupIdentifier, secretIdentifier);
    }

    @Override
    public RawSecretEntry create(NewSecretEntry newSecretEntry) {
        readWriteLock.writeLock().lock();

        try {
            RawSecretEntry entry = createEntry(newSecretEntry, 1);
            store.create(entry);

            entry.bestEffortShred();

            return entry;
        } catch (AlreadyExistsException e) {
            throw new AlreadyExistsException(String.format("A secret named '%s' already exists", newSecretEntry.secretIdentifier.name), e);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public RawSecretEntry addVersion(NewSecretEntry newSecretEntry) {
        readWriteLock.writeLock().lock();

        try {
            Optional<RawSecretEntry> last = store.stream().filter(name.eq(newSecretEntry.secretIdentifier)).reverse().findFirst();

            // TODO sanity check on last?
            if (last.isPresent()) {
                // TODO: check for overflow?
                long version = last.get().version + 1;

                RawSecretEntry entry = createEntry(newSecretEntry, version);
                store.create(entry);

                last.get().bestEffortShred();

                return entry;
            } else {
                throw new DoesNotExistException(String.format(
                        "Secret with name '%s' does not exist", newSecretEntry.secretIdentifier.name));
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private RawSecretEntry createEntry(NewSecretEntry newSecretEntry, long version) {
        ZonedDateTime now = FormattedTimestamp.now();
        return createEntry(newSecretEntry, version, now, now, newSecretEntry.createdBy);
    }

    private RawSecretEntry createEntry(NewSecretEntry secret, long version, ZonedDateTime created, ZonedDateTime modified, Optional<UserAlias> modifiedBy) {
        EncryptionContext encryptionContext = new DefaultEncryptionContext(groupIdentifier, secret.secretIdentifier, version, secret.state, secret.notBefore, secret.notAfter);
        EncryptionPayload encryptionPayload = new EncryptionPayload(secret.secretValue, secret.userData, created, secret.createdBy, modified, modifiedBy, secret.comment);

        byte[] encryptedPayload = encryptor.encrypt(encryptionPayload.toByteArray(), encryptionContext);

        return new RawSecretEntry(secret.secretIdentifier, version, secret.state, secret.notBefore, secret.notAfter, encryptedPayload);
    }

    @Override
    public SecretEntry decrypt(RawSecretEntry rawSecretEntry, SecretIdentifier expectedSecretIdentifier, long expectedVersion) {
        SecretEntry entry = decryptEvenIfNotActive(rawSecretEntry, expectedSecretIdentifier, expectedVersion);

        ZonedDateTime now = FormattedTimestamp.now();
        if ((entry.notAfter.isPresent() && entry.notAfter.get().compareTo(now) < 0)
            || (entry.notBefore.isPresent() && entry.notBefore.get().compareTo(now) > 0)
            || entry.state != State.ENABLED) {
            throw new IllegalArgumentException("The secret must be active to be decrypted with this method");
        }

        return entry;
    }

    @Override
    public SecretEntry decryptEvenIfNotActive(RawSecretEntry rawSecretEntry, SecretIdentifier expectedSecretIdentifier, long expectedVersion) {
        // TODO consider removing expected Secret Identifier and BigInteger (as this is checked on return)
        DefaultEncryptionContext encryptionContext = new DefaultEncryptionContext(groupIdentifier, expectedSecretIdentifier, expectedVersion, rawSecretEntry.state, rawSecretEntry.notBefore, rawSecretEntry.notAfter);

        byte[] decryptedPayload = encryptor.decrypt(rawSecretEntry.encryptedPayload, encryptionContext);
        EncryptionPayload encryptionPayload = EncryptionPayload.fromByteArray(decryptedPayload);

        verifyNotTamperedWithOrThrow(rawSecretEntry, encryptionContext);

        SecretEntry secretEntry = new SecretEntry(encryptionPayload, rawSecretEntry);

        BestEffortShredder.shred(decryptedPayload);

        return secretEntry;
    }

    private void verifyNotTamperedWithOrThrow(RawSecretEntry rawSecretEntry, DefaultEncryptionContext encryptionContext) {
        if (!rawSecretEntry.secretIdentifier.equals(encryptionContext.secretIdentifier)
            || !rawSecretEntry.version.equals(encryptionContext.secretVersion)) {
            throw new PotentiallyMaliciousDataException("The metadata in the raw entry does not match the encrypted data!");
        }
    }

    @Override
    public RawSecretEntry update(SecretMetadata metadata) {
        readWriteLock.writeLock().lock();

        try {
            Optional<RawSecretEntry> existingEntry = stream().filter(name.eq(metadata.secretIdentifier).AND(version.eq(metadata.version))).findFirst();
            if (!existingEntry.isPresent()) {
                throw new DoesNotExistException(String.format(
                        "Secret with name=%s,version=%s does not exist", metadata.secretIdentifier.name, metadata.version));
            }
            SecretEntry current = decryptEvenIfNotActive(existingEntry.get(), metadata.secretIdentifier, metadata.version);

            NewSecretEntry newSecretEntry = new NewSecretEntry(metadata.secretIdentifier,
                    current.secretValue,
                    metadata.state.orElse(current.state),
                    current.createdBy,
                    current.notBefore,
                    current.notAfter,
                    metadata.comment.orElse(current.comment),
                    metadata.userData.orElse(current.userData)
            );

            RawSecretEntry entry = createEntry(newSecretEntry, current.version, current.created, FormattedTimestamp.now(), metadata.modifiedBy);

            store.update(entry, existingEntry.get());

            existingEntry.get().bestEffortShred();
            current.bestEffortShred();
            newSecretEntry.bestEffortShred();

            return entry;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public KVStream<RawSecretEntry> stream() {
        return store.stream();
    }

    @Override
    public void delete(SecretIdentifier secretIdentifier) {
        store.delete(secretIdentifier);
    }

    @Override
    public Set<SecretIdentifier> identifiers() {
        return store.keySet();
    }

    @Override
    public void close() {
        readWriteLock.writeLock().lock();

        try {
            store.close();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
