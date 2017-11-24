/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
