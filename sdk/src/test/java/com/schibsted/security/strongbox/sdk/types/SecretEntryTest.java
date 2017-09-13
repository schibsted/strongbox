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
