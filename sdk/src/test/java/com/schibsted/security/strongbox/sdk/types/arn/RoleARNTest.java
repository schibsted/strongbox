/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types.arn;

import com.schibsted.security.strongbox.sdk.exceptions.InvalidResourceName;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author stiankri
 */
public class RoleARNTest {
    @Test
    public void role() {
        RoleARN arn = new RoleARN("arn:aws:iam::12345:role/role-name");

        assertThat(arn.getRoleName(), is("role-name"));
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void region_present() {
        new RoleARN("arn:aws:iam:eu-west-1:12345:role/role-name");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void wrong_resource_type() {
        new RoleARN("arn:aws:iam::12345:myRole/role-name");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void no_resource_type() {
        new RoleARN("arn:aws:iam::12345:role-name");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void wrong_service_type() {
        new RoleARN("arn:aws:foo::12345:role/role-name");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void wrong_number_of_parts() {
        new RoleARN("arn:aws:iam::12345:role:role-name");
    }
}
