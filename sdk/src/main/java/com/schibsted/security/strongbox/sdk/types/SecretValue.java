/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
