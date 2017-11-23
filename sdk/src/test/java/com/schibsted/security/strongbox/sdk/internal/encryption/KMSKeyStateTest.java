/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author kvlees
 */
public class KMSKeyStateTest {
    @Test
    public void testFromString() throws Exception {
        assertEquals(KMSKeyState.fromString("Enabled"), KMSKeyState.ENABLED);
        assertEquals(KMSKeyState.fromString("Disabled"), KMSKeyState.DISABLED);
        assertEquals(KMSKeyState.fromString("PendingDeletion"), KMSKeyState.PENDING_DELETION);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromStringInvalidState() throws Exception {
        KMSKeyState.fromString("Invalid");
    }

    @Test
    public void testToString() throws Exception {
        assertEquals(KMSKeyState.fromString("Enabled").toString(), "Enabled");
        assertEquals(KMSKeyState.fromString("Disabled").toString(), "Disabled");
        assertEquals(KMSKeyState.fromString("PendingDeletion").toString(), "PendingDeletion");
    }
}
