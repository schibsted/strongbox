/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.srn;

import com.google.common.base.Objects;
import com.schibsted.security.strongbox.sdk.exceptions.InvalidResourceName;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

/**
 * @author stiankri
 */
public class SecretSRN extends SecretsGroupSRN {
    public final SecretIdentifier secretIdentifier;

    private static final String RESOURCE_PREFIX = "secret";

    public SecretSRN(String account, SecretsGroupIdentifier groupIdentifier, SecretIdentifier secretIdentifier) {
        super(account, groupIdentifier);
        this.secretIdentifier = secretIdentifier;
    }

    @Override
    public String toString() {
        return toSrn();
    }

    public String toSrn() {
        //srn:aws:sm:region:account-id:secret/team/project/service/MySecret
        return String.format("srn:aws:strongbox:%s:%s:%s/%s/%s", groupIdentifier.region.getName(), account, RESOURCE_PREFIX, groupIdentifier.name.replace('.','/'), secretIdentifier.name);
    }

    public static SecretSRN fromSrn(String srn) {
        String[] parts = srn.split(":");

        if (!parts[0].equals("srn") || !parts[1].equals("aws") || !parts[2].equals("strongbox") || parts.length != 6) {
            throw new InvalidResourceName(srn, "An SRN should start with 'srn:aws:sm' and have 6 parts");
        }

        Region region = Region.fromName(parts[3]);
        String account = parts[4];

        String[] suffix = parts[5].split("/");

        if (!suffix[0].equals(RESOURCE_PREFIX)) {
            throw new InvalidResourceName(srn, "Resource name part of SRN should start with " + RESOURCE_PREFIX);
        }

        String secretsGroupName = parts[5].substring(RESOURCE_PREFIX.length() + 1, parts[5].lastIndexOf('/')).replace("/", ".");
        String secretName = suffix[suffix.length-1];
        return new SecretSRN(account, new SecretsGroupIdentifier(region, secretsGroupName), new SecretIdentifier(secretName));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(account, groupIdentifier, secretIdentifier);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof SecretSRN){
            final SecretSRN other = (SecretSRN) obj;
            return Objects.equal(account, other.account)
                    && Objects.equal(groupIdentifier, other.groupIdentifier)
                    && Objects.equal(secretIdentifier, other.secretIdentifier);
        } else {
            return false;
        }
    }
}
