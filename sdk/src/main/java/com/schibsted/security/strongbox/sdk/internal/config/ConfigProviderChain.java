/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.config;

import com.schibsted.security.strongbox.sdk.types.ProfileIdentifier;
import com.schibsted.security.strongbox.sdk.types.arn.RoleARN;

import java.io.File;
import java.util.Optional;
import java.util.function.Function;

/**
 * Locate config in credentials file with fallback to config file
 *
 * @author stiankri
 */
public class ConfigProviderChain {
    private final Optional<AWSConfig> credentials;
    private final Optional<AWSConfig> config;

    public ConfigProviderChain() {
        Optional<File> credentialsFile = AWSCLIConfigFile.getCredentialProfilesFile();
        Optional<File> configFile = AWSCLIConfigFile.getConfigFile();

        Optional<AWSCLIConfigFile.Config> credentialsProvider = credentialsFile
                .map(AWSCLIConfigFile::new)
                .map(AWSCLIConfigFile::getConfig);

        Optional<AWSCLIConfigFile.Config> configProvider = configFile
                .map(AWSCLIConfigFile::new)
                .map(AWSCLIConfigFile::getConfig);

        credentials = credentialsProvider.map(AWSConfig::new);
        config = configProvider.map(AWSConfig::new);
    }

    public boolean hasConfig() {
        return credentials.isPresent() || config.isPresent();
    }

    public Optional<String> getMFASerial(ProfileIdentifier profile) {
        return getPropertyFromEitherConfigFile(c -> c.getMFASerial(profile));
    }

    public Optional<ProfileIdentifier> getSourceProfile(ProfileIdentifier profile) {
        return getPropertyFromEitherConfigFile(c -> c.getSourceProfile(profile));
    }

    public Optional<RoleARN> getRoleArn(ProfileIdentifier profile) {
        return getPropertyFromEitherConfigFile(c -> c.getRoleArn(profile));
    }

    public Optional<String> getAWSAccessKeyId(ProfileIdentifier profile) {
        return getPropertyFromEitherConfigFile(c -> c.getAWSAccessKeyId(profile));
    }

    public String getAWSAccessKeyIdOrThrow(ProfileIdentifier profile) {
        return getPropertyFromEitherConfigFileOrThrow(c -> c.getAWSAccessKeyId(profile), profile, AWSConfigPropertyKey.AWS_ACCESS_KEY_ID);
    }

    public Optional<String> getAWSSecretKey(ProfileIdentifier profile) {
        return getPropertyFromEitherConfigFile(c -> c.getAWSSecretKey(profile));
    }

    public String getAWSSecretKeyOrThrow(ProfileIdentifier profile) {
        return getPropertyFromEitherConfigFileOrThrow(c -> c.getAWSSecretKey(profile), profile, AWSConfigPropertyKey.AWS_SECRET_ACCESS_KEY);
    }

    private <T> Optional<T> getPropertyFromEitherConfigFile(Function<AWSConfig, Optional<T>> fetchPropertyFunction) {
        Optional<T> result = Optional.empty();
        if (credentials.isPresent()) {
            result = fetchPropertyFunction.apply(credentials.get());
        }

        if (!result.isPresent() && config.isPresent()) {
            result = fetchPropertyFunction.apply(config.get());
        }

        return result;
    }

    private <T> T getPropertyFromEitherConfigFileOrThrow(Function<AWSConfig, Optional<T>> f, ProfileIdentifier profile, AWSConfigPropertyKey propertyKey) {
        Optional<T> result = getPropertyFromEitherConfigFile(f);

        if (!result.isPresent()) {
            throw new IllegalStateException(String.format("No property '%s' set for profile '%s'", propertyKey, profile.name));
        }

        return result.get();
    }
}
