/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import com.google.common.base.Objects;

import java.util.Arrays;

public class ByteSecretEntry {

    public final SecretIdentifier secretIdentifer;
    public final long version;
    public final byte[] value;

    public ByteSecretEntry(SecretIdentifier secretIdentifer, long version, byte[] value) {
        this.secretIdentifer = secretIdentifer;
        this.version = version;
        this.value = value;
    }

    public static ByteSecretEntry of(SecretEntry secretEntry) {
        return new ByteSecretEntry(secretEntry.secretIdentifier, secretEntry.version, secretEntry.secretValue.asByteArray());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteSecretEntry that = (ByteSecretEntry) o;
        return version == that.version &&
                Objects.equal(secretIdentifer, that.secretIdentifer) &&
                Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(secretIdentifer, version, value);
    }

}
