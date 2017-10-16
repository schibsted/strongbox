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
