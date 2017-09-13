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
