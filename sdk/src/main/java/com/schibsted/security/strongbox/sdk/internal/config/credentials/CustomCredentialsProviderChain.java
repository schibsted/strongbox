/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.config.credentials;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.schibsted.security.strongbox.sdk.types.ClientConfiguration;
import com.schibsted.security.strongbox.sdk.types.ProfileIdentifier;

import java.util.function.Supplier;

/**
 * @author stiankri
 */
public class CustomCredentialsProviderChain extends AWSCredentialsProviderChain {

    public CustomCredentialsProviderChain(ClientConfiguration clientConfiguration, ProfileIdentifier profile, Supplier<MFAToken> mfaTokenSupplier) {
        super(new EnvironmentVariableCredentialsProvider(),
                new SystemPropertiesCredentialsProvider(),
                new ProfileCredentialProvider(clientConfiguration, profile, mfaTokenSupplier),
                new EC2ContainerCredentialsProviderWrapper());
    }
}
