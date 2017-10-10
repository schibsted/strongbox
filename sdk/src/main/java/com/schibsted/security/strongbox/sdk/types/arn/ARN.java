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

import com.google.common.base.Objects;
import com.schibsted.security.strongbox.sdk.exceptions.InvalidResourceName;
import com.schibsted.security.strongbox.sdk.types.Region;

import java.util.Optional;

/**
 * Based on: https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html
 *
 * arn:partition:service:region:account-id:resource
 * arn:partition:service:region:account-id:resourcetype/resource
 * arn:partition:service:region:account-id:resourcetype:resource
 */
public class ARN {
    protected final String partition;
    protected final String service;
    protected final long account;
    protected final Optional<Region> region;
    protected final Optional<String> resourceType;
    protected final String resource;
    protected final int numParts;
    protected final String arn;

    public ARN(final String arn) {
        this.arn = arn;

        String[] parts = arn.split(":");
        numParts = parts.length;

        if (numParts == 7) {
            resourceType = Optional.of(parts[5]);
            resource = parts[6];
        } else if (numParts == 6) {
            if (parts[5].contains("/")) {
                int divider = parts[5].indexOf("/");
                resourceType = Optional.of(parts[5].substring(0, divider));
                resource = parts[5].substring(divider+1);
            } else {
                resource = parts[5];
                resourceType = Optional.empty();
            }
        } else {
            throw new InvalidResourceName(arn, "An ARN should have 6 or 7 parts");
        }

        if (!parts[0].equals("arn")) {
            throw new InvalidResourceName(arn, "An ARN should start with 'arn'");
        }

        partition = parts[1];
        service = parts[2];

        if (!parts[3].isEmpty()) {
            region = Optional.of(Region.fromName(parts[3]));
        } else {
            region = Optional.empty();
        }
        account = Long.valueOf(parts[4]);

    }

    public String toArn() {
        return this.arn;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(arn);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ARN) {
            final ARN other = (ARN) obj;
            return Objects.equal(arn, other.arn);
        } else {
            return false;
        }
    }
}
