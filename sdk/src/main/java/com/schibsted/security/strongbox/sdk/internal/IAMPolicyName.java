/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal;

import com.google.common.base.Objects;
import com.schibsted.security.strongbox.sdk.exceptions.InvalidResourceName;
import com.schibsted.security.strongbox.sdk.internal.access.AccessLevel;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

/**
 * @author stiankri
 * @author kvlees
 */
public final class IAMPolicyName {
    public final SecretsGroupIdentifier group;
    public final AccessLevel accessLevel;

    public IAMPolicyName(SecretsGroupIdentifier group, AccessLevel accessLevel) {
        this.group = group;
        this.accessLevel = accessLevel;
    }

    @Override
    public String toString() {
        return String.format("%s_%s_%s_%s", AWSResourceNameSerialization.GLOBAL_PREFIX, group.region.getName(),
                             AWSResourceNameSerialization.encodeSecretsGroupName(group.name), accessLevel);
    }

    public static IAMPolicyName fromString(String wrappedAWSResourceName) {
        String[] parts = wrappedAWSResourceName.split(AWSResourceNameSerialization.GLOBAL_STRING_DELIMITER);

        if (!parts[0].equals(AWSResourceNameSerialization.GLOBAL_PREFIX)) {
            throw new InvalidResourceName(wrappedAWSResourceName, "An IAM policy name should start with " + AWSResourceNameSerialization.GLOBAL_PREFIX);
        }

        if (parts.length != 4) {
            throw new InvalidResourceName(wrappedAWSResourceName, "An IAM policy name should have exactly 4 parts");
        }

        Region region = Region.fromName(parts[1]);
        String name = AWSResourceNameSerialization.decodeSecretsGroupName(parts[2]);
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, name);

        AccessLevel accessLevel = AccessLevel.fromString(parts[3]);

        return new IAMPolicyName(group, accessLevel);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof IAMPolicyName) {
            final IAMPolicyName other = (IAMPolicyName) obj;
            return Objects.equal(group, other.group) && Objects.equal(accessLevel, other.accessLevel);
        } else {
            return false;
        }
    }
}
