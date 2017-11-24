/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import com.schibsted.security.strongbox.sdk.internal.encryption.EncryptionPayload;
import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.testng.Assert.*;

/**
 * @author kvlees
 * @author stiankri
 */
public class SecretEntryTest {
    ZonedDateTime timestamp = ZonedDateTime.of(2016,6,1,13,37,42,0, ZoneId.of("UTC"));

    private RawSecretEntry createRawEntry() {
        SecretIdentifier secretIdentifier = new SecretIdentifier("key");

        byte[] secret = Encoder.asUTF8("value");
        long version = 1;
        State state = State.ENABLED;
        return new RawSecretEntry(secretIdentifier, version, state, Optional.of(timestamp), Optional.empty(), secret);
    }

    private EncryptionPayload createPayload(boolean withComment) {
        SecretValue value = new SecretValue("secretValue", SecretType.OPAQUE);
        UserData userData = new UserData(Encoder.asUTF8("userdata"));
        ZonedDateTime created = ZonedDateTime.of(2016,6,1,0,0,0,0, ZoneId.of("UTC"));
        ZonedDateTime modified = ZonedDateTime.of(2017,6,1,0,0,0,0, ZoneId.of("UTC"));
        Optional<UserAlias> createdBy = Optional.empty();
        Optional<UserAlias> updatedBy = Optional.empty();
        Optional<ZonedDateTime> notAfter = Optional.of(ZonedDateTime.of(2016,7,1,0,0,0,0, ZoneId.of("UTC")));
        Optional<Comment> comment = Optional.empty();
        if (withComment) {
            comment = Optional.of(new Comment("some comment"));
        }
        return new EncryptionPayload(value, Optional.of(userData), created, createdBy, modified, updatedBy, comment);
    }

    @Test
    public void testSecretEntry() {
        EncryptionPayload payload = createPayload(true);
        RawSecretEntry rawEntry = createRawEntry();

        SecretEntry secretEntry = new SecretEntry(payload, rawEntry);
        assertEquals(secretEntry.secretIdentifier.name, rawEntry.secretIdentifier.name);
        assertEquals(secretEntry.version, rawEntry.version.longValue());
        assertEquals(secretEntry.secretValue, payload.value);
        assertEquals(secretEntry.created, payload.created);
        assertEquals(secretEntry.comment, payload.comment);
        assertEquals(secretEntry.userData, payload.userData);

        assertEquals(
                secretEntry.toString(),
                "SecretEntry{secretIdentifier=key, version=1, secretValue=SecretValue{type=opaque, secretEncoding=utf8}, " +
                "created=2016-06-01T00:00Z[UTC], modified=2017-06-01T00:00Z[UTC], state=enabled, notBefore=Optional[2016-06-01T13:37:42Z[UTC]], " +
                "notAfter=Optional.empty, comment=Optional[Comment{}], userData=Optional[UserData{}]}");
    }
}
