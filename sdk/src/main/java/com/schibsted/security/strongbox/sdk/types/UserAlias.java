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
