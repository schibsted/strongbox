/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal;

import com.google.common.base.Objects;
import com.schibsted.security.strongbox.sdk.exceptions.InvalidResourceName;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

/**
 * @author stiankri
 * @author kvlees
 */
public final class RegionLocalResourceName {
    public final SecretsGroupIdentifier group;
    private static final String PREFIX = AWSResourceNameSerialization.GLOBAL_PREFIX;

    public RegionLocalResourceName(SecretsGroupIdentifier group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return String.format("%s_%s_%s", PREFIX, group.region.getName(), AWSResourceNameSerialization.encodeSecretsGroupName(group.name));
    }

    public static RegionLocalResourceName fromString(String wrappedAWSResourceName) {
        String[] parts = wrappedAWSResourceName.split(AWSResourceNameSerialization.GLOBAL_STRING_DELIMITER);

        if (!parts[0].equals(PREFIX)) {
            throw new InvalidResourceName(
                    wrappedAWSResourceName, "Regional local resource name should start with " + PREFIX);
        }

        if (parts.length != 3) {
            throw new InvalidResourceName(
                    wrappedAWSResourceName, "Regional local resource name should have exactly 3 parts");
        }

        Region region = Region.fromName(parts[1]);
        String name = AWSResourceNameSerialization.decodeSecretsGroupName(parts[2]);
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(region, name);

        return new RegionLocalResourceName(group);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof RegionLocalResourceName){
            final RegionLocalResourceName other = (RegionLocalResourceName) obj;
            return Objects.equal(group, other.group);
        } else{
            return false;
        }
    }
}
