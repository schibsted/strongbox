/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import com.google.common.base.Objects;

/**
 * AWS profile used in AWS CLI credential and config files
 *
 * @author stiankri
 */
public class ProfileIdentifier {
    public final String name;

    public ProfileIdentifier(final String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProfileIdentifier) {
            final ProfileIdentifier other = (ProfileIdentifier) obj;
            return Objects.equal(name, other.name);
        } else {
            return false;
        }
    }
}
