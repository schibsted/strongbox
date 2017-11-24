/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
