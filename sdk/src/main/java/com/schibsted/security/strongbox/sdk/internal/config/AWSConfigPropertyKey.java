/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.config;

import com.amazonaws.auth.profile.internal.ProfileKeyConstants;

/**
 * @author stiankri
 */
public enum AWSConfigPropertyKey {
    MFA_SERIAL("mfa_serial"),
    SOURCE_PROFILE(ProfileKeyConstants.SOURCE_PROFILE),
    ROLE_ARN(ProfileKeyConstants.ROLE_ARN),
    AWS_ACCESS_KEY_ID(ProfileKeyConstants.AWS_ACCESS_KEY_ID),
    AWS_SECRET_ACCESS_KEY(ProfileKeyConstants.AWS_SECRET_ACCESS_KEY);

    private final String name;

    AWSConfigPropertyKey(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
