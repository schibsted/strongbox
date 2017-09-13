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

/**
 * AWS Role ARN
 */
public class RoleARN extends ARN {
    public RoleARN(final String arn) {
        super(arn);

        if (!resourceType.isPresent() || !resourceType.get().equals("role")) {
            throw new InvalidResourceName(arn, "Role ARN's should have 'role' as resource type");
        }

        if (region.isPresent()) {
            throw new InvalidResourceName(arn, "Role ARN's should not have a region");
        }

        if (!service.equals("iam")) {
            throw new InvalidResourceName(arn, String.format("Role ARN's service needs to be 'iam', not '%s'", service));
        }

        if (numParts != 6) {
            throw new InvalidResourceName(arn, String.format("Role ARN's should have 6 parts separated by ':', not %d parts", numParts));
        }
    }

    public String getRoleName() {
        return this.resource;
    }
}
