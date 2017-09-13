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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.schibsted.security.strongbox.sdk.internal.encryption.BestEffortShred;
import com.schibsted.security.strongbox.sdk.internal.encryption.BestEffortShredder;

import java.util.Arrays;

/**
 * @author stiankri
 */
public final class UserData implements BestEffortShred {
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 50000;

    private byte[] userData;

    public UserData(byte[] userData) {
        if (userData.length < MIN_LENGTH) {
            throw new IllegalArgumentException(String.format("UserData must be at least %d bytes", MIN_LENGTH));
        }
        if (userData.length > MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("UserData can at most be %d bytes", MAX_LENGTH));
        }
        this.userData = userData;
    }

    public byte[] asByteArray() {
        return userData;
    }

    public void clear() {
        Arrays.fill(userData, (byte) 0 );
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userData);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof UserData) {
            final UserData other = (UserData) obj;
            return Arrays.equals(userData, other.asByteArray());
        } else {
            return false;
        }
    }

    @Override
    public void bestEffortShred() {
        BestEffortShredder.shred(userData);
    }
}
