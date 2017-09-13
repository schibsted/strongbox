/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Schibsted Products & Technology AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
