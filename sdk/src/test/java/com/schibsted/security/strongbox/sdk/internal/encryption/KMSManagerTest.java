/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.CancelKeyDeletionRequest;
import com.amazonaws.services.kms.model.CreateAliasRequest;
import com.amazonaws.services.kms.model.CreateKeyRequest;
import com.amazonaws.services.kms.model.CreateKeyResult;
import com.amazonaws.services.kms.model.DescribeKeyRequest;
import com.amazonaws.services.kms.model.DescribeKeyResult;
import com.amazonaws.services.kms.model.EnableKeyRequest;
import com.amazonaws.services.kms.model.KeyMetadata;
import com.amazonaws.services.kms.model.KeyState;
import com.amazonaws.services.kms.model.NotFoundException;
import com.amazonaws.services.kms.model.ScheduleKeyDeletionRequest;
import com.amazonaws.services.kms.model.ScheduleKeyDeletionResult;
import com.schibsted.security.strongbox.sdk.exceptions.AlreadyExistsException;
import com.schibsted.security.strongbox.sdk.exceptions.DoesNotExistException;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

/**
 * @author kvlees
 * @author stiankri
 */
public class KMSManagerTest {
    private static final String TEST_GROUP = "test.group";
    private static final Region TEST_REGION = Region.EU_WEST_1;

    private static final String ALIAS_KEY_NAME = "alias/strongbox_eu-west-1_test-group";
    private static final String KMS_ALIAS_ARN = "arn:aws:kms:us-west-1:1234:alias/" + ALIAS_KEY_NAME;
    private static final String KMS_ARN = "arn:aws:kms:us-west-1:1234:key/d413a4de-5eb5-4eb4-b4af-373bcba5efdf";

    private DescribeKeyRequest describeKeyRequest = new DescribeKeyRequest().withKeyId(KMS_ALIAS_ARN);
    private AWSKMSClient mockKMSClient;
    private KMSManager kmsManager;
    private SecretsGroupIdentifier group;

    @BeforeMethod
    public void setUp() throws Exception {
        AWSCredentialsProvider mockCredentials = mock(AWSCredentialsProvider.class);
        this.mockKMSClient = mock(AWSKMSClient.class);
        this.group = new SecretsGroupIdentifier(TEST_REGION, TEST_GROUP);
        ClientConfiguration mockConfig = mock(ClientConfiguration.class);

        KMSManager manager = new KMSManager(mockKMSClient, mockCredentials, mockConfig, group);
        this.kmsManager = spy(manager);
        doReturn(KMS_ALIAS_ARN).when(kmsManager).getAliasArn();
    }

    private static DescribeKeyResult constructDescribeKeyResult(KeyState state) {
        return new DescribeKeyResult().withKeyMetadata(
                new KeyMetadata().withKeyState(state).withArn(KMS_ARN));
    }

    private static DescribeKeyResult enabledKeyResult() {
        return constructDescribeKeyResult(KeyState.Enabled);
    }

    private static DescribeKeyResult disabledKeyResult() {
        return constructDescribeKeyResult(KeyState.Disabled);
    }

    private static DescribeKeyResult pendingDeletionKeyResult() {
        return constructDescribeKeyResult(KeyState.PendingDeletion);
    }

    @Test
    public void testCreate() throws Exception {
        // Mocks the responses from AWS.
        CreateKeyRequest createKeyRequest = new CreateKeyRequest().withDescription(
                "This key is automatically managed by Strongbox");
        CreateKeyResult createKeyResult = new CreateKeyResult().withKeyMetadata(new KeyMetadata().withArn(KMS_ARN));
        CreateAliasRequest createAliasRequest = new CreateAliasRequest().withAliasName(ALIAS_KEY_NAME).withTargetKeyId(KMS_ARN);

        when(mockKMSClient.describeKey(describeKeyRequest))
                .thenThrow(NotFoundException.class)
                .thenThrow(NotFoundException.class)  // still waiting for creation
                .thenReturn(enabledKeyResult());
        when(mockKMSClient.createKey(createKeyRequest)).thenReturn(createKeyResult);

        // Check the result from create method.
        String arn = kmsManager.create();
        assertEquals(arn, KMS_ARN);

        // Verify correct number of calls was made to AWS.
        verify(mockKMSClient, times(3)).describeKey(describeKeyRequest);
        verify(mockKMSClient, times(1)).createAlias(createAliasRequest);
        verify(mockKMSClient, times(1)).createKey(createKeyRequest);
    }

    @Test
    public void testCreateWhenDisabledForce() throws Exception {
        // Mocks the responses from AWS when the key starts in the disabled state.
        when(mockKMSClient.describeKey(describeKeyRequest))
                .thenReturn(disabledKeyResult())
                .thenReturn(disabledKeyResult()) // still waiting for state change
                .thenReturn(enabledKeyResult());

        // Check the result from create method.
        String arn = kmsManager.create(true);
        assertEquals(arn, KMS_ARN);

        // Verify correct number of calls was made to AWS.
        verify(mockKMSClient, times(3)).describeKey(describeKeyRequest);
        verify(mockKMSClient, times(1)).enableKey(new EnableKeyRequest().withKeyId(KMS_ARN));
        verify(mockKMSClient, never()).createKey(any());
        verify(mockKMSClient, never()).cancelKeyDeletion(any());
    }

    @Test
    public void testCreateWhenPendingDeletionForce() throws Exception {
        // Mocks the responses from AWS when the key starts in the pending deletion state.
        when(mockKMSClient.describeKey(describeKeyRequest))
                .thenReturn(pendingDeletionKeyResult())
                .thenReturn(pendingDeletionKeyResult()) // still waiting for state change
                .thenReturn(enabledKeyResult());

        // Check the result from create method.
        String arn = kmsManager.create(true);
        assertEquals(arn, KMS_ARN);

        // Verify correct number of calls was made to AWS.
        verify(mockKMSClient, times(3)).describeKey(describeKeyRequest);
        verify(mockKMSClient, times(1)).cancelKeyDeletion(new CancelKeyDeletionRequest().withKeyId(KMS_ARN));
        verify(mockKMSClient, times(1)).enableKey(new EnableKeyRequest().withKeyId(KMS_ARN));
        verify(mockKMSClient, never()).createKey(any());
    }

    @Test
    public void testCreateAlreadyExistsAndEnabled() throws Exception {
        // Mocks the responses from AWS when the key already exists in enabled state.
        when(mockKMSClient.describeKey(describeKeyRequest)).thenReturn(enabledKeyResult());

        boolean exceptionThrown = false;
        try {
            kmsManager.create();
        } catch (AlreadyExistsException e) {
            assertEquals(e.getMessage(), "KMS key already exists for group=test.group,region=eu-west-1, and override not set");
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        verify(mockKMSClient, never()).createKey(any());
    }

    @Test
    public void testCreateAlreadyExistsAndDisabled() throws Exception {
        when(mockKMSClient.describeKey(describeKeyRequest)).thenReturn(disabledKeyResult());

        boolean exceptionThrown = false;
        try {
            kmsManager.create();
        } catch (AlreadyExistsException e) {
            assertEquals(e.getMessage(), "KMS key already exists for group=test.group,region=eu-west-1, and override not set");
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        verify(mockKMSClient, never()).createKey(any());
    }

    @Test
    public void testCreateAlreadyExistsAndPendingDeletion() throws Exception {
        when(mockKMSClient.describeKey(describeKeyRequest)).thenReturn(pendingDeletionKeyResult());

        boolean exceptionThrown = false;
        try {
            kmsManager.create();
        } catch (AlreadyExistsException e) {
            assertEquals(e.getMessage(), "KMS key already exists for group=test.group,region=eu-west-1, and override not set");
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        verify(mockKMSClient, never()).createKey(any());
    }

    @Test
    public void testDelete() throws Exception {
        ScheduleKeyDeletionRequest deleteRequest = new ScheduleKeyDeletionRequest()
                .withKeyId(KMS_ARN)
                .withPendingWindowInDays(7);
        ScheduleKeyDeletionResult deleteResult = new ScheduleKeyDeletionResult().
                withDeletionDate(new Date());
        when(mockKMSClient.scheduleKeyDeletion(deleteRequest)).thenReturn(deleteResult);

        // Waits for the state change.
        when(mockKMSClient.describeKey(describeKeyRequest))
                .thenReturn(enabledKeyResult())  // called when getting the ARN
                .thenReturn(enabledKeyResult())  // waiting for state change
                .thenReturn(constructDescribeKeyResult(KeyState.PendingDeletion));

        kmsManager.delete();
        verify(mockKMSClient, times(3)).describeKey(describeKeyRequest);
        verify(mockKMSClient, times(1)).scheduleKeyDeletion(deleteRequest);
    }

    @Test
    public void testDeleteNonExisting() throws Exception {
        when(mockKMSClient.describeKey(describeKeyRequest)).thenThrow(NotFoundException.class);

        boolean exceptionThrown = false;
        try {
            kmsManager.delete();
        } catch (DoesNotExistException e) {
            assertEquals(e.getMessage(), String.format("Failed to find KMS key with alias '%s'", KMS_ALIAS_ARN));
            exceptionThrown = true;
        }
        verify(mockKMSClient, times(1)).describeKey(describeKeyRequest);
        verify(mockKMSClient, never()).scheduleKeyDeletion(any());
    }

    @Test
    public void testExists() throws Exception {
        when(mockKMSClient.describeKey(describeKeyRequest)).thenReturn(enabledKeyResult());
        assertTrue(kmsManager.exists());
    }

    @Test
    public void testExistsWhenDisabled() throws Exception {
        when(mockKMSClient.describeKey(describeKeyRequest)).thenReturn(disabledKeyResult());
        assertTrue(kmsManager.exists());
    }

    @Test
    public void testExistsWhenPendingDeletion() throws Exception {
        when(mockKMSClient.describeKey(describeKeyRequest)).thenReturn(pendingDeletionKeyResult());
        assertTrue(kmsManager.exists());
    }

    @Test
    public void testExistsWhenPendingDeletionForce() throws Exception {
        when(mockKMSClient.describeKey(describeKeyRequest)).thenReturn(pendingDeletionKeyResult());
        assertFalse(kmsManager.exists(true));
    }

    @Test
    public void testExistsKeyDoesNotExist() throws Exception {
        when(mockKMSClient.describeKey(describeKeyRequest)).thenThrow(NotFoundException.class);
        assertFalse(kmsManager.exists());
    }
}