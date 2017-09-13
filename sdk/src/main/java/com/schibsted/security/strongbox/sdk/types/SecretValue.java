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
import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import com.schibsted.security.strongbox.sdk.internal.encryption.BestEffortShred;
import com.schibsted.security.strongbox.sdk.internal.encryption.BestEffortShredder;

import java.util.Arrays;

/**
 * @author stiankri
 */
public final class SecretValue implements BestEffortShred {
    private static final int MAX_LENGTH = 50000;

    private byte[] secretValue;
    public final SecretType type;
    public final Encoding encoding;

    public SecretValue(byte[] secretValue, Encoding encoding, SecretType type) {
        if (secretValue.length == 0) {
            throw new IllegalArgumentException("SecretValue can't be empty");
        }
        if (secretValue.length > MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("SecretValue can at most be %d bytes", MAX_LENGTH));
        }

        this.secretValue = secretValue;
        this.encoding = encoding;
        this.type = type;
    }

    public SecretValue(byte[] secretValue, SecretType type) {
        this(secretValue, Encoding.BINARY, type);
    }

    public SecretValue(String secretValue, SecretType type) {
        this(Encoder.asUTF8(secretValue), Encoding.UTF8, type);
    }

    public byte[] asByteArray() {
        return secretValue;
    }

    public String asString() {
        return Encoder.fromUTF8(secretValue);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("secretEncoding", encoding)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(secretValue, type, encoding);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof SecretValue) {
            final SecretValue other = (SecretValue) obj;
            return Arrays.equals(secretValue, other.asByteArray())
                    && Objects.equal(type, other.type)
                    && Objects.equal(encoding, other.encoding);
        } else {
            return false;
        }
    }

    @Override
    public void bestEffortShred() {
        BestEffortShredder.shred(secretValue);
    }
}
