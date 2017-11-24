/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
