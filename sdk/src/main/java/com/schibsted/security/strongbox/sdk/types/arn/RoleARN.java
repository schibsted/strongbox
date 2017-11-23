/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
