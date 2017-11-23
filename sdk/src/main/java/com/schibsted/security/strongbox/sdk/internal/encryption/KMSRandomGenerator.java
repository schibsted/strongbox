/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.GenerateRandomRequest;
import com.amazonaws.services.kms.model.GenerateRandomResult;
import com.schibsted.security.strongbox.sdk.internal.RegionResolver;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;

import static com.schibsted.security.strongbox.sdk.internal.ClientConfigurationHelper.transformAndVerifyOrThrow;

/**
 * @author stiankri
 */
public class KMSRandomGenerator implements RandomGenerator {
    private final AWSKMS client;

    public KMSRandomGenerator(AWSCredentialsProvider awsCredentials, ClientConfiguration clientConfiguration) {
        this.client = AWSKMSClientBuilder.standard()
                .withCredentials(awsCredentials)
                .withClientConfiguration(transformAndVerifyOrThrow(clientConfiguration))
                .withRegion(RegionResolver.getRegion())
                .build();
    }

    public KMSRandomGenerator() {
        this(new DefaultAWSCredentialsProviderChain(), new ClientConfiguration());
    }

    public byte[] generateRandom(Integer numberOfBytes) {
        GenerateRandomRequest request = new GenerateRandomRequest();
        request.withNumberOfBytes(numberOfBytes);
        GenerateRandomResult result = client.generateRandom(request);
        return result.getPlaintext().array();
    }
}
