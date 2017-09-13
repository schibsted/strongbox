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
