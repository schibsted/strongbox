/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import com.google.common.base.Objects;
import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;

import java.util.regex.Pattern;

/**
 * @author stiankri
 */
public final class UserAlias {
    public final String alias;

    private static final int ALIAS_MIN_LENGTH = 1;
    private static final int ALIAS_MAX_LENGTH = 32;
    private static final String ALIAS_REGEX = "^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$";
    private static Pattern pattern = Pattern.compile(ALIAS_REGEX);

    public UserAlias(String alias) {
        int length = Encoder.asUTF8(alias).length;

        if (length < ALIAS_MIN_LENGTH) {
            throw new IllegalArgumentException(String.format("The user alias '%s' must be at least %d characters long", alias, ALIAS_MIN_LENGTH));
        }

        if (length > ALIAS_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("The user alias '%s' cannot be longer than %d characters", alias, ALIAS_MAX_LENGTH));
        }

        if (!pattern.matcher(alias).find()) {
            throw new IllegalArgumentException(String.format("The user alias '%s' did not match the regular expression '%s'", alias, ALIAS_REGEX));
        }

        this.alias = alias;
    }

    @Override
    public String toString() {
        return alias;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(alias);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof UserAlias){
            final UserAlias other = (UserAlias) obj;
            return Objects.equal(alias, other.alias);
        } else{
            return false;
        }
    }
}
