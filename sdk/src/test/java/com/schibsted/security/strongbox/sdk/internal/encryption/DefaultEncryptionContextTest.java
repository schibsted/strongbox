/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import com.schibsted.security.strongbox.sdk.types.State;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * @author kvlees
 * @author stiankri
 */
public class DefaultEncryptionContextTest {
    @Test
    public void testToMap() {
        DefaultEncryptionContext context = new DefaultEncryptionContext(
                new SecretsGroupIdentifier(Region.US_WEST_1, "test.group"),
                new SecretIdentifier("secret1"), Long.parseUnsignedLong("8446744073709551615"),
                State.ENABLED,
                Optional.of(ZonedDateTime.of(2016,1,2,3,4,0,0, ZoneId.of("UTC"))),
                Optional.empty()
        );

        Map<String, String> map = context.toMap();
        assertEquals(map.get("0"), "us-west-1     ");
        assertEquals(map.get("0").length(), 14);
        assertEquals(map.get("1"), "test.group                                                      ");
        assertEquals(map.get("1").length(), 64);
        assertEquals(map.get("2"), "secret1                                                                                                                         ");
        assertEquals(map.get("2").length(), 128);
        assertEquals(map.get("3"), "08446744073709551615");
        assertEquals(map.get("3").length(), 20);
        assertEquals(map.get("4"), "2");
        assertEquals(map.get("4").length(), 1);
        assertEquals(map.get("5"), "1");
        assertEquals(map.get("5").length(), 1);
        assertEquals(map.get("6"), "00000000001451703840");
        assertEquals(map.get("6").length(), 20);
        assertEquals(map.get("7"), "0");
        assertEquals(map.get("7").length(), 1);
        assertEquals(map.get("8"), "00000000000000000000");
        assertEquals(map.get("8").length(), 20);
    }
}
