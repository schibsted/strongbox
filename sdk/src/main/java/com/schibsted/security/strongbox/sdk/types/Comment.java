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
