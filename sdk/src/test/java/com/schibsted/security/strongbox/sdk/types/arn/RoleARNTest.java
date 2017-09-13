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
