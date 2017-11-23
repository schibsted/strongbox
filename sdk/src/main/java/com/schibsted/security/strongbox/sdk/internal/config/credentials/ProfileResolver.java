/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.config.credentials;

import com.amazonaws.auth.profile.internal.AwsProfileNameLoader;
import com.schibsted.security.strongbox.sdk.types.ProfileIdentifier;

import java.util.Optional;

/**
 * @author stiankri
 */
public class ProfileResolver {
    public static ProfileIdentifier resolveProfile(Optional<String> profileOverride) {
        if (profileOverride.isPresent()) {
            return new ProfileIdentifier(profileOverride.get());
        } else {
            String resolvedProfile = AwsProfileNameLoader.INSTANCE.loadProfileName();

            if (resolvedProfile.equals(AwsProfileNameLoader.DEFAULT_PROFILE_NAME)) {
                String awsDefaultProfile = System.getenv("AWS_DEFAULT_PROFILE");

                if (awsDefaultProfile != null) {
                    resolvedProfile = awsDefaultProfile;
                }
            }

            return new ProfileIdentifier(resolvedProfile);
        }
    }
}
