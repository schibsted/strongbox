/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.config.credentials;

import com.schibsted.security.strongbox.sdk.internal.config.credentials.SessionCache;
import com.schibsted.security.strongbox.sdk.types.ProfileIdentifier;
import com.schibsted.security.strongbox.sdk.types.arn.RoleARN;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author stiankri
 */
public class SessionCacheTest {
    @Test
    public void resolve_filename() {
        ProfileIdentifier profile = new ProfileIdentifier("my-profile");
        RoleARN arn = new RoleARN("arn:aws:iam::12345678910:role/my-role");

        SessionCache sessionCache = new SessionCache(profile, arn);

        assertThat(sessionCache.resolveFileName(), is("my-profile--arn_aws_iam__12345678910_role-my-role.json"));
    }
}
