/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.config;

import com.schibsted.security.strongbox.sdk.types.ProfileIdentifier;
import com.schibsted.security.strongbox.sdk.types.arn.RoleARN;

import java.util.Optional;

/**
 * @author stiankri
 */
public class AWSConfig {
    private AWSCLIConfigFile.Config config;

    public AWSConfig(AWSCLIConfigFile.Config config) {
        this.config = config;
    }

    public Optional<String> getMFASerial(ProfileIdentifier profile) {
        return getProperty(profile, AWSConfigPropertyKey.MFA_SERIAL);
    }

    public Optional<ProfileIdentifier> getSourceProfile(ProfileIdentifier profile) {
        return getProperty(profile, AWSConfigPropertyKey.SOURCE_PROFILE).map(ProfileIdentifier::new);
    }

    public Optional<RoleARN> getRoleArn(ProfileIdentifier profile) {
        return getProperty(profile, AWSConfigPropertyKey.ROLE_ARN).map(RoleARN::new);
    }

    public Optional<String> getAWSAccessKeyId(ProfileIdentifier profile) {
        return getProperty(profile, AWSConfigPropertyKey.AWS_ACCESS_KEY_ID);
    }

    public Optional<String> getAWSSecretKey(ProfileIdentifier profile) {
        return getProperty(profile, AWSConfigPropertyKey.AWS_SECRET_ACCESS_KEY);
    }

    private Optional<String> getProperty(ProfileIdentifier profile, AWSConfigPropertyKey awsConfigField) {
        return getProperty(profile.name, awsConfigField.toString());
    }

    private Optional<String> getProperty(String profile, String property) {
        Optional<String> result = config.getSection(profile)
                .flatMap(s -> s.getProperty(property));

        // legacy fallback
        if (!result.isPresent()) {
            result = config.getSection("profile " + profile)
                    .flatMap(s -> s.getProperty(property));
        }

        return result;
    }
}
