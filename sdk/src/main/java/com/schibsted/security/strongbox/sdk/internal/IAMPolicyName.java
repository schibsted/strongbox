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
