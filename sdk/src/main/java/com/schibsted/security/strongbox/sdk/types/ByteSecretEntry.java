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

import java.util.Arrays;

public class ByteSecretEntry {

    public final SecretIdentifier secretIdentifer;
    public final byte[] value;
    public final long version;

    public ByteSecretEntry(SecretIdentifier secretIdentifer, byte[] value, long version) {
        this.secretIdentifer = secretIdentifer;
        this.value = value;
        this.version = version;
    }

    public static ByteSecretEntry of(SecretEntry secretEntry) {
        return new ByteSecretEntry(secretEntry.secretIdentifier, secretEntry.secretValue.asByteArray(), secretEntry.version);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ByteSecretEntry that = (ByteSecretEntry) obj;
        return Objects.equal(secretIdentifer, that.secretIdentifer) && Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(secretIdentifer, value);
    }

}
