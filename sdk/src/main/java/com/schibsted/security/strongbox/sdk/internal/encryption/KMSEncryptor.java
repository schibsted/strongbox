/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoAlgorithm;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.exception.AwsCryptoException;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.schibsted.security.strongbox.sdk.exceptions.UnlimitedEncryptionNotSetException;
import com.schibsted.security.strongbox.sdk.internal.interfaces.ManagedResource;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.EncryptionStrength;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

import java.util.Map;
import java.util.Optional;

import static com.schibsted.security.strongbox.sdk.internal.ClientConfigurationHelper.transformAndVerifyOrThrow;

/**
 * @author stiankri
 * @author kvlees
 * @author torarvid
 */
public class KMSEncryptor implements Encryptor, ManagedResource {
    private final AwsCrypto crypto;
    private final KMSManager kmsManager;
    private final AWSCredentialsProvider awsCredentials;
    private final SecretsGroupIdentifier groupIdentifier;
    private final ClientConfiguration clientConfiguration;
    private Optional<KmsMasterKeyProvider> prov = Optional.empty();
    private Optional<String> keyArn = Optional.empty();

    public KMSEncryptor(KMSManager kmsManager, AWSCredentialsProvider awsCredentials, ClientConfiguration clientConfiguration, SecretsGroupIdentifier groupIdentifier, AwsCrypto awsCrypto, EncryptionStrength encryptionStrength) {
        this.awsCredentials = awsCredentials;
        this.clientConfiguration = clientConfiguration;
        this.groupIdentifier = groupIdentifier;
        this.kmsManager = kmsManager;

        if (encryptionStrength.equals(EncryptionStrength.AES_128)) {
            awsCrypto.setEncryptionAlgorithm(CryptoAlgorithm.ALG_AES_128_GCM_IV12_TAG16_HKDF_SHA256_ECDSA_P256);
        } else if (encryptionStrength.equals(EncryptionStrength.AES_256)) {
            awsCrypto.setEncryptionAlgorithm(CryptoAlgorithm.ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384);
        } else {
            throw new IllegalArgumentException(String.format("Unrecognized encryption strength %s", encryptionStrength.toString()));
        }

        this.crypto = awsCrypto;
    }

    public static KMSEncryptor fromCredentials(AWSCredentialsProvider awsCredentials,
                                               ClientConfiguration clientConfiguration,
                                               SecretsGroupIdentifier groupIdentifier,
                                               EncryptionStrength encryptionStrength) {
        KMSManager manager = KMSManager.fromCredentials(awsCredentials, clientConfiguration, groupIdentifier);
        return new KMSEncryptor(manager, awsCredentials, clientConfiguration, groupIdentifier, new AwsCrypto(), encryptionStrength);
    }

    /**
     * Assumes UTF-8 to decrypt properly
     */
    @Override
    public String encrypt(String plaintext, EncryptionContext context) {
        return crypto.encryptString(getProvider(), plaintext, context.toMap()).getResult();
    }

    @Override
    public String decrypt(String ciphertext, EncryptionContext context) {
        final CryptoResult<String, KmsMasterKey> decryptResult = crypto.decryptString(getProvider(), ciphertext);

        verify(decryptResult, context);

        return decryptResult.getResult();
    }

    boolean isInvalidKeyException(AwsCryptoException e) {
        return e.getMessage().equals("java.security.InvalidKeyException: Illegal key size");
    }

    @Override
    public byte[] encrypt(byte[] plaintext, EncryptionContext context) {
        try {
            return crypto.encryptData(getProvider(), plaintext, context.toMap()).getResult();
        } catch (AwsCryptoException e) {
            if (isInvalidKeyException(e)) {
                throw new UnlimitedEncryptionNotSetException();
            } else {
                throw e;
            }
        }
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, EncryptionContext context) {
        try {
            final CryptoResult<byte[], KmsMasterKey> decryptResult = crypto.decryptData(getProvider(), ciphertext);

            verify(decryptResult, context);

            return decryptResult.getResult();
        } catch (AwsCryptoException e) {
            if (isInvalidKeyException(e)) {
                throw new UnlimitedEncryptionNotSetException();
            } else {
                throw e;
            }
        }
    }

    private void verify(CryptoResult<?, KmsMasterKey> decryptResult, EncryptionContext context) {
        if (!decryptResult.getMasterKeyIds().get(0).equals(getKeyArn())) {
            throw new IllegalStateException("Wrong key id!");
        }

        for (final Map.Entry<String, String> e : context.toMap().entrySet()) {
            if (!e.getValue().equals(decryptResult.getEncryptionContext().get(e.getKey()))) {
                throw new IllegalStateException("Wrong Encryption Context!");
            }
        }
    }

    public byte[] generateRandom(Integer numberOfBytes) {
        return kmsManager.generateRandom(numberOfBytes);
    }

    @Override
    public String create() {
        return kmsManager.create();
    }

    public String create(boolean allowExistingPendingDeletedOrDisabledKey) {
        return kmsManager.create(allowExistingPendingDeletedOrDisabledKey);
    }

    @Override
    public void delete() {
        kmsManager.delete();
    }

    @Override
    public Optional<String> awsAdminPolicy() {
        return kmsManager.awsAdminPolicy();
    }

    @Override
    public Optional<String> awsReadOnlyPolicy() {
        return kmsManager.awsReadOnlyPolicy();
    }

    @Override
    public String getArn() {
        return kmsManager.getArn();
    }

    @Override
    public boolean exists() {
        return kmsManager.exists();
    }

    public boolean exists(boolean allowExistingPendingDeletedOrDisabledKey) {
        return kmsManager.exists(allowExistingPendingDeletedOrDisabledKey);
    }

    public int pendingDeletionWindowInDays() {
        return kmsManager.pendingDeletionWindowInDays();
    }

    protected KmsMasterKeyProvider getProvider() {
        if (!prov.isPresent()) {
            Region region = RegionUtils.getRegion(groupIdentifier.region.getName());
            prov = Optional.of(new KmsMasterKeyProvider(awsCredentials, region, transformAndVerifyOrThrow(clientConfiguration), getKeyArn()));
        }
        return prov.get();
    }

    protected String getKeyArn() {
        if (!keyArn.isPresent()) {
            keyArn = Optional.of(kmsManager.getArn());
        }
        return keyArn.get();
    }
}
