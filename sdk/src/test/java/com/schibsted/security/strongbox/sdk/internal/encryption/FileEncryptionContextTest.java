/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author stiankri
 */
public class FileEncryptionContextTest {

    @Test
    public void testToMap() {
        FileEncryptionContext context = new FileEncryptionContext(
                new SecretsGroupIdentifier(Region.EU_WEST_1, "test.group")
        );

        Map<String, String> map = context.toMap();
        assertEquals(map.get("0"), "1");
        assertEquals(map.get("1"), "eu-west-1");
        assertEquals(map.get("2"), "test.group");
    }
}
