/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import com.google.common.base.Objects;
import com.schibsted.security.strongbox.sdk.exceptions.InvalidResourceName;

/**
 * @author stiankri
 * @author kvlees
 * @author hawkaa
 */
public final class Principal {
    public final PrincipalType type;
    public final String name;

    public Principal(PrincipalType type, String principalName) {
        // TODO: add regex based on AWS limitations, or just let the call fail?
        this.type = type;
        this.name = principalName;
    }

    // TODO verify that the ARN is in the same account
    public static Principal fromArn(String arn, String account) {
        String[] parts = arn.split(":");
        if (parts.length != 6 || !parts[0].equals("arn") || !parts[1].equals("aws") || !parts[2].equals("iam")) {
            throw new InvalidResourceName(arn, "A principal ARN should start with 'arn:aws:iam' and have 6 parts");
        }

        if (!account.equals(parts[4])) {
            throw new IllegalArgumentException("The account in the ARN does not match account of the Principal trying to " +
                    "perform the action.");
        }

        String last = parts[5];
        String[] elements = last.split("/");
        if (elements.length != 2) {
            throw new InvalidResourceName(
                    arn, "Resource name part of principal ARN should contain exactly one '/'. Only principal ARNs for" +
                    "groups, users or roles can be used.");
        }

        String type = elements[0];
        String name = elements[1];
        return new Principal(PrincipalType.fromString(type), name);
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", name, type.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, type);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof Principal){
            final Principal other = (Principal) obj;
            return Objects.equal(name, other.name) && Objects.equal(type, other.type);
        } else{
            return false;
        }
    }

}
