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
