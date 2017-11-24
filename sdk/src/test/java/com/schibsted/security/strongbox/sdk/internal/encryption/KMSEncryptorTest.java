/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoAlgorithm;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.EncryptionStrength;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

/**
 * @author kvlees
 */
public class KMSEncryptorTest {
    private static final String KMS_ARN = "arn:aws:kms:us-west-1:1234:key/d413a4de-5eb5-4eb4-b4af-373bcba5efdf";
    private final String encryptedPayload = "EncryptedPayload";
    private AwsCrypto mockAwsCrypto;
    private KMSEncryptor kmsEncryptor;
    private KMSManager mockKmsManager;
    private KmsMasterKeyProvider mockProvider;

    // TODO: these tests are mocking a lot of things. Investigate if there is a better way that mocks less and test more.

    @BeforeMethod
    public void setUp() throws Exception {
        AWSCredentialsProvider mockCredentials = mock(AWSCredentialsProvider.class);
        ClientConfiguration mockConfig = mock(ClientConfiguration.class);
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(Region.US_WEST_1, "test.group");

        this.mockAwsCrypto = mock(AwsCrypto.class);
        this.mockKmsManager = mock(KMSManager.class);
        KMSEncryptor encryptor = new KMSEncryptor(mockKmsManager, mockCredentials, mockConfig, group, mockAwsCrypto, EncryptionStrength.AES_256);

        this.kmsEncryptor = spy(encryptor);
        this.mockProvider = mock(KmsMasterKeyProvider.class);
        doReturn(mockProvider).when(kmsEncryptor).getProvider();

        // Verify the expected encryption algorithm was set.
        verify(mockAwsCrypto, times(1)).setEncryptionAlgorithm(
                CryptoAlgorithm.ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384);
    }

    @Test
    public void testEncrypt() throws Exception {
        String plaintext = "jsonblob";
        EncryptionContext mockContext = mock(EncryptionContext.class);
        CryptoResult mockCryptoResult = mock(CryptoResult.class);
        Map<String, String> contextMap = new HashMap<>();

        when(mockContext.toMap()).thenReturn(contextMap);
        when(mockCryptoResult.getResult()).thenReturn(encryptedPayload);
        when(mockAwsCrypto.encryptString(mockProvider, plaintext, contextMap)).thenReturn(
                mockCryptoResult);
        assertEquals(kmsEncryptor.encrypt(plaintext, mockContext), encryptedPayload);
    }

    // TODO: test the decrypt method with:
    //   (1) valid Ciphertext
    //   (2) invalid key
    //   (3) invalid context.
    //
    // Also it would be nice to encrypt something and then decrypt it. Figure out how this can be done from the tests.

    @Test
    public void testCreate() throws Exception {
        when(mockKmsManager.create()).thenReturn(KMS_ARN);
        assertEquals(kmsEncryptor.create(), KMS_ARN);
        verify(mockKmsManager, times(1)).create();
    }

    @Test
    public void testDelete() throws Exception {
        kmsEncryptor.delete();
        verify(mockKmsManager, times(1)).delete();
    }

    @Test
    public void testAwsAdminPolicy() throws Exception {
        String adminPolicy = "Admin policy";
        when(mockKmsManager.awsAdminPolicy()).thenReturn(Optional.of(adminPolicy));
        assertEquals(kmsEncryptor.awsAdminPolicy().get(),adminPolicy);
        verify(mockKmsManager, times(1)).awsAdminPolicy();
    }

    @Test
    public void testAwsReadOnlyPolicy() throws Exception {
        String readonlyPolicy = "Readonly policy";
        when(mockKmsManager.awsReadOnlyPolicy()).thenReturn(Optional.of(readonlyPolicy));
        assertEquals(kmsEncryptor.awsReadOnlyPolicy().get(), readonlyPolicy);
        verify(mockKmsManager, times(1)).awsReadOnlyPolicy();
    }

    @Test
    public void testGetArn() throws Exception {
        when(mockKmsManager.getArn()).thenReturn(KMS_ARN);
        assertEquals(kmsEncryptor.getArn(), KMS_ARN);
        verify(mockKmsManager, times(1)).getArn();
    }

    @Test
    public void testExists() throws Exception {
        when(mockKmsManager.exists()).thenReturn(true);
        assertTrue(kmsEncryptor.exists());
        verify(mockKmsManager, times(1)).exists();
    }

    @Test
    public void testNotExists() throws Exception {
        when(mockKmsManager.exists()).thenReturn(false);
        assertFalse(kmsEncryptor.exists());
        verify(mockKmsManager, times(1)).exists();
    }
}
