/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generated;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.backend.dynamodb.GenericDynamoDB;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

import java.util.concurrent.locks.ReadWriteLock;

import static com.schibsted.security.strongbox.sdk.internal.ClientConfigurationHelper.verifyOrThrow;
import static com.schibsted.security.strongbox.sdk.internal.ClientConfigurationHelper.transformAndVerifyOrThrow;

/**
 * @author stiankri
 * @author kvlees
 * @author torarvid
 */
public class DynamoDB extends GenericDynamoDB<RawSecretEntry, SecretIdentifier> implements Store {
    public DynamoDB(AmazonDynamoDB dynamoDBClient,
                    AWSCredentialsProvider awsCredentials,
                    ClientConfiguration clientConfiguration,
                    SecretsGroupIdentifier groupIdentifier,
                    ReadWriteLock readWriteLock) {
        super(dynamoDBClient, awsCredentials, clientConfiguration, groupIdentifier, RawSecretEntry.class, Config.converters, readWriteLock);
    }

    public static DynamoDB fromCredentials(AWSCredentialsProvider awsCredentials,
                                           ClientConfiguration clientConfiguration,
                                           SecretsGroupIdentifier groupIdentifier,
                                           ReadWriteLock readWriteLock) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(awsCredentials)
            .withClientConfiguration(verifyOrThrow(transformAndVerifyOrThrow(clientConfiguration)))
            .withRegion(groupIdentifier.region.getName())
            .build();
        return new DynamoDB(client, awsCredentials, clientConfiguration, groupIdentifier, readWriteLock);
    }
}
