/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.*;
import com.schibsted.security.strongbox.sdk.internal.access.IAMPolicyManager;
import com.schibsted.security.strongbox.sdk.exceptions.DoesNotExistException;
import com.schibsted.security.strongbox.sdk.exceptions.UnexpectedStateException;
import com.schibsted.security.strongbox.sdk.internal.RegionLocalResourceName;
import com.schibsted.security.strongbox.sdk.internal.interfaces.ManagedResource;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.schibsted.security.strongbox.sdk.internal.ClientConfigurationHelper.transformAndVerifyOrThrow;

/**
 * @author stiankri
 * @author kvlees
 * @author torarvid
 */
public class KMSManager implements ManagedResource {
    private static final Logger log = LoggerFactory.getLogger(KMSManager.class);
    private static final int SLEEP_TIME = 100;
    private static final int MAX_RETRIES = 30;

    private final AWSKMS kms;
    private static final String ALIAS_PREFIX = "alias/";
    private final String aliasKeyName;
    private final SecretsGroupIdentifier group;
    private final AWSCredentialsProvider awsCredentials;
    private final ClientConfiguration clientConfiguration;

    public KMSManager(AWSKMS client, AWSCredentialsProvider awsCredentials, ClientConfiguration clientConfiguration, SecretsGroupIdentifier groupIdentifier) {
        this.kms = client;
        this.awsCredentials = awsCredentials;
        this.clientConfiguration = clientConfiguration;
        this.group = groupIdentifier;

        RegionLocalResourceName resourceName = new RegionLocalResourceName(groupIdentifier);
        this.aliasKeyName = ALIAS_PREFIX + resourceName.toString();
    }

    public static KMSManager fromCredentials(AWSCredentialsProvider awsCredentials,
                                             ClientConfiguration clientConfiguration,
                                             SecretsGroupIdentifier groupIdentifier) {
        AWSKMS client = AWSKMSClientBuilder.standard()
            .withCredentials(awsCredentials)
            .withClientConfiguration(transformAndVerifyOrThrow(clientConfiguration))
            .withRegion(groupIdentifier.region.getName())
            .build();
        return new KMSManager(client, awsCredentials, clientConfiguration, groupIdentifier);
    }

    public String create(boolean allowExistingPendingDeletedOrDisabledKey) {
        String arn;

        Optional<KeyMetadata> current = describeKey();
        if (current.isPresent()) {
            if (!allowExistingPendingDeletedOrDisabledKey) {
                throw new com.schibsted.security.strongbox.sdk.exceptions.AlreadyExistsException(String.format(
                        "KMS key already exists for group=%s,region=%s, and override not set", group.name, group.region.getName()));
            }

            arn = current.get().getArn();

            KMSKeyState state = KMSKeyState.fromString(current.get().getKeyState());
            switch (state) {
                case PENDING_DELETION:
                    CancelKeyDeletionRequest request = new CancelKeyDeletionRequest();
                    request.withKeyId(arn);
                    kms.cancelKeyDeletion(request);
                    // No break in this case statement because when key deletion is cancelled, the key is set to
                    // disabled state, so we need to also make an enable request.
                    // See: https://docs.aws.amazon.com/kms/latest/APIReference/API_CancelKeyDeletion.html
                case DISABLED:
                    EnableKeyRequest enableKeyRequest = new EnableKeyRequest();
                    enableKeyRequest.withKeyId(arn);
                    kms.enableKey(enableKeyRequest);
                    break;
                default:
                    throw new com.schibsted.security.strongbox.sdk.exceptions.AlreadyExistsException(String.format(
                            "KMS key already exists for group=%s,region=%s", group.name, group.region.getName()));
            }
        } else {
            CreateKeyRequest keyRequest = new CreateKeyRequest();
            keyRequest.setDescription("This key is automatically managed by Strongbox");

            CreateKeyResult result = kms.createKey(keyRequest);
            arn = result.getKeyMetadata().getArn();

            CreateAliasRequest createAliasRequest = new CreateAliasRequest();
            createAliasRequest.setAliasName(aliasKeyName);
            createAliasRequest.setTargetKeyId(arn);
            kms.createAlias(createAliasRequest);

            EnableKeyRotationRequest enableKeyRotationRequest = new EnableKeyRotationRequest();
            enableKeyRotationRequest.setKeyId(arn);
            kms.enableKeyRotation(enableKeyRotationRequest);
        }

        waitForKeyState(KMSKeyState.ENABLED);
        return arn;
    }

    @Override
    public String create() {
        return create(false);
    }

    private void waitForKeyState(KMSKeyState requiredState) {
        String currentState = "Unknown";
        try {
            int retries = 0;
            while (retries < MAX_RETRIES) {
                Optional<KeyMetadata> keyData = describeKey();
                if (keyData.isPresent()) {
                    currentState = keyData.get().getKeyState();
                    if (KMSKeyState.fromString(currentState) == requiredState) {
                        return;
                    }
                }

                log.info("Waiting for key to reach state:'{}'", requiredState.name());
                Thread.sleep(SLEEP_TIME);
                retries++;
            }
        } catch (InterruptedException e) {
            throw new UnexpectedStateException(aliasKeyName, currentState, requiredState.name(),
                                               "Error occurred while waiting for KMS key to update", e);
        }
        throw new UnexpectedStateException(aliasKeyName, currentState, requiredState.name(),
                                           "KMS key did not reach expected state before timeout");
    }

    public byte[] generateRandom(Integer numberOfBytes) {
        GenerateRandomRequest request = new GenerateRandomRequest();
        request.withNumberOfBytes(numberOfBytes);
        GenerateRandomResult result = kms.generateRandom(request);
        return result.getPlaintext().array();
    }

    @Override
    public void delete() {
        deleteAndGetSchedule();
        waitForKeyState(KMSKeyState.PENDING_DELETION);
    }

    @Override
    public Optional<String> awsAdminPolicy() {
        return  Optional.of(
                "    {\n" +
                "        \"Sid\": \"KMS\",\n" +
                "        \"Effect\": \"Allow\",\n" +
                "        \"Action\": [\n" +
                "            \"kms:*\"\n" +
                "        ],\n" +
                "        \"Resource\": \"" + getArn() + "\"\n" +
                "    }");
    }

    @Override
    public Optional<String> awsReadOnlyPolicy() {
        return Optional.of(
                "    {\n" +
                "        \"Sid\": \"KMS\",\n" +
                "        \"Effect\": \"Allow\",\n" +
                "        \"Action\": [\n" +
                "            \"kms:Decrypt\",\n" +
                "            \"kms:DescribeKey\"\n" +
                "        ],\n" +
                "        \"Resource\": \"" + getArn() + "\"\n" +
                "    }");
    }

    @Override
    public String getArn() {
        Optional<KeyMetadata> metadata = describeKey();

        if (metadata.isPresent()) {
            return metadata.get().getArn();
        } else {
            throw new DoesNotExistException(String.format("Failed to find KMS key with alias '%s'", getAliasArn()));
        }
    }

    @Override
    public boolean exists() {
        return exists(false);
    }

    public boolean exists(boolean allowExistingPendingDeletedOrDisabledKey) {
        Optional<KeyMetadata> current = describeKey();
        if (current.isPresent()) {
            if (!allowExistingPendingDeletedOrDisabledKey) {
                return true;
            }

            KMSKeyState state = KMSKeyState.fromString(current.get().getKeyState());
            switch (state) {
                case PENDING_DELETION:
                case DISABLED:
                    return false;
                default:
                    return true;
            }
        } else {
            return false;
        }
    }

    public String getAliasArn() {
        return String.format("arn:aws:kms:%s:%s:%s", group.region.getName(), IAMPolicyManager.getAccount(awsCredentials, clientConfiguration), aliasKeyName);
    }

    private Optional<KeyMetadata> describeKey() {
        try {
            DescribeKeyRequest request = new DescribeKeyRequest();
            request.withKeyId(getAliasArn());

            DescribeKeyResult result = kms.describeKey(request);
            return Optional.of(result.getKeyMetadata());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    public int pendingDeletionWindowInDays() {
        return 7;
    }

    private Date deleteAndGetSchedule() {
        String arn = getArn();
        try {
            int daysUntilDeletion = pendingDeletionWindowInDays();
            ScheduleKeyDeletionRequest request = new ScheduleKeyDeletionRequest();
            request.withKeyId(arn).withPendingWindowInDays(daysUntilDeletion);

            ScheduleKeyDeletionResult result = kms.scheduleKeyDeletion(request);
            // TODO: which time zone is this?
            return result.getDeletionDate();
        } catch (KMSInvalidStateException e) {
            throw new UnexpectedStateException(arn, KMSKeyState.ENABLED.toString(), KMSKeyState.PENDING_DELETION.toString(), "Unable to delete KMS keys", e);
        }
    }

}
