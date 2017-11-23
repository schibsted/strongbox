/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import com.google.common.base.MoreObjects;
import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import com.schibsted.security.strongbox.sdk.internal.encryption.BestEffortShred;
import com.schibsted.security.strongbox.sdk.internal.encryption.BestEffortShredder;

import java.util.Arrays;

/**
 * @author stiankri
 */
public class Comment implements BestEffortShred {
    public static final int MIN_LENGTH = 1;
    public static final int MAX_LENGTH = 1000;

    private byte[] comment;

    public Comment(String comment) {
        this(Encoder.asUTF8(comment));
    }

    public Comment(byte[] comment) {
        if (comment.length < MIN_LENGTH) {
            throw new IllegalArgumentException(String.format("The comment must be at least %d characters long", MIN_LENGTH));
        }
        if (comment.length > MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("The comment cannot be longer than %d characters long", MAX_LENGTH));
        }
        this.comment = comment;
    }

    public byte[] asByteArray() {
        return comment;
    }

    public String asString() {
        return Encoder.fromUTF8(comment);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(comment);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Comment) {
            final Comment other = (Comment) obj;
            return Arrays.equals(comment, other.asByteArray());
        } else {
            return false;
        }
    }

    @Override
    public void bestEffortShred() {
        BestEffortShredder.shred(comment);
    }
}
