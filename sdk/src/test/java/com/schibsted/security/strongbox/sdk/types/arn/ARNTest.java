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
public class ARNTest {
    @Test
    public void parse_6_parts_no_resource_type() {
        new ARN("arn:partition:service:eu-west-1:1234567890:resource");
    }

    @Test
    public void parse_6_parts_resource_type() {
        new ARN("arn:partition:service:eu-west-1:1234567890:resourceType/resource");
    }

    @Test
    public void parse_7_parts() {
        new ARN("arn:partition:service:eu-west-1:1234567890:resourceType:resource");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void illegal_region() {
        new ARN("arn:partition:service:abc-def-99:1234567890:resourceType:resource");
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void illegal_account() {
        new ARN("arn:partition:service:eu-west-1:account:resourceType:resource");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void too_few_elements() {
        new ARN("arn:partition:service:eu-west-1:account:resourceType:resource:extra:elements");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void too_many_elements() {
        new ARN("arn:partition:service:eu-west-1:account");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void does_not_start_with_arn() {
        new ARN("arn:partition:service:eu-west-1:account");
    }

    @Test
    public void pass_through() {
        String arn = "arn:partition:service:eu-west-1:1234567890:resource";
        assertThat((new ARN(arn)).arn, is(arn));
    }
}
