/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author kvlees
 */
public class StateTest {
    @Test
    public void testFromString() {
        assertEquals(State.fromString("compromised"), State.COMPROMISED);
        assertEquals(State.fromString("disabled"), State.DISABLED);
        assertEquals(State.fromString("enabled"), State.ENABLED);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromStringInvalidState() {
        State.fromString("invalid");
    }

    @Test
    public void testToString() {
        assertEquals(State.COMPROMISED.toString(), "compromised");
        assertEquals(State.ENABLED.toString(), "enabled");
        assertEquals(State.DISABLED.toString(), "disabled");
    }
}
