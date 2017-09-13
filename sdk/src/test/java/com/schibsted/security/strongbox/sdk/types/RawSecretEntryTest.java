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

import com.schibsted.security.strongbox.sdk.exceptions.ParseException;
import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static org.testng.Assert.*;

/**
 * @author stiankri
 * @author kvlees
 */
public class RawSecretEntryTest {
    ZonedDateTime timestamp = ZonedDateTime.of(2016,6,1,13,37,42,0, ZoneId.of("UTC"));
    SecretIdentifier secretIdentifier = new SecretIdentifier("key");

    byte[] secret = Encoder.asUTF8("value");
    long version = 1;
    State state = State.ENABLED;

    RawSecretEntry rawSecretEntry = new RawSecretEntry(secretIdentifier, version, state, Optional.of(timestamp), Optional.empty(), secret);
    String jsonBlob = "{\"secretIdentifier\":\"key\",\"version\":1,\"state\":\"enabled\",\"notBefore\":\"2016-06-01T13:37:42Z[UTC]\",\"notAfter\":null,\"encryptedPayload\":\"dmFsdWU=\"}";

    @Test
    public void serialize() {
        String serialized = rawSecretEntry.toJsonBlob();
        assertThat(serialized, is(jsonBlob));
    }

    @Test
    public void deserialize() {
        RawSecretEntry deserialized = RawSecretEntry.fromJsonBlob(jsonBlob);
        assertThat(deserialized, is(rawSecretEntry));
    }

    @Test(expectedExceptions = ParseException.class)
    public void deserializeInvalidJson() {
        RawSecretEntry.fromJsonBlob("{#$%^&*");
    }

    @Test
    public void equals() {
        // Wrong type.
        assertFalse(rawSecretEntry.equals(secretIdentifier));

        // Doesn't equal
        RawSecretEntry rawSecretEntry2 = new RawSecretEntry(
                secretIdentifier, version, state, Optional.empty(), Optional.empty(), secret);
        assertFalse(rawSecretEntry.equals(rawSecretEntry2));

        // Equals
        RawSecretEntry rawSecretEntry3 = new RawSecretEntry(
                secretIdentifier, version, state, Optional.empty(), Optional.empty(), secret);
        assertTrue(rawSecretEntry2.equals(rawSecretEntry3));
    }
}
