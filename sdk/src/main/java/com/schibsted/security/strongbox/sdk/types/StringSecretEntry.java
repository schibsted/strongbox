/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import com.google.common.base.Objects;

public class StringSecretEntry {

    public final SecretIdentifier secretIdentifer;
    public final long version;
    public final String value;



    public StringSecretEntry(SecretIdentifier secretIdentifer, long version, String value) {
        this.secretIdentifer = secretIdentifer;
        this.version = version;
        this.value = value;
    }

    public static StringSecretEntry of(SecretEntry secretEntry) {
        return new StringSecretEntry(secretEntry.secretIdentifier, secretEntry.version, secretEntry.secretValue.asString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringSecretEntry that = (StringSecretEntry) o;
        return version == that.version &&
                Objects.equal(secretIdentifer, that.secretIdentifer) &&
                Objects.equal(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(secretIdentifer, version, value);
    }
}
