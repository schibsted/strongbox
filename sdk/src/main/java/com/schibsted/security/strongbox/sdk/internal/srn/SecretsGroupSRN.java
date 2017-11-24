/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.srn;

import com.google.common.base.Objects;
import com.schibsted.security.strongbox.sdk.types.SRN;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

/**
 * @author stiankri
 */
public class SecretsGroupSRN extends SRN {
    public final String account;
    public final SecretsGroupIdentifier groupIdentifier;

    private static final String RESOURCE_PREFIX = "group";

    public SecretsGroupSRN(String account, SecretsGroupIdentifier groupIdentifier) {
        this.account = account;
        this.groupIdentifier = groupIdentifier;
    }

    @Override
    public String toSrn() {
        return String.format("srn:aws:strongbox:%s:%s:%s/%s", groupIdentifier.region.getName(), account, RESOURCE_PREFIX, groupIdentifier.name.replace('.','/'));
    }

    @Override
    public String toString() {
        return toSrn();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(account, groupIdentifier);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof SecretsGroupSRN){
            final SecretsGroupSRN other = (SecretsGroupSRN) obj;
            return Objects.equal(account, other.account)
                    && Objects.equal(groupIdentifier, other.groupIdentifier);
        } else{
            return false;
        }
    }
}
