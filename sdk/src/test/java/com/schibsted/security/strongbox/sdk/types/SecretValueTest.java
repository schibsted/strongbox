/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author stiankri
 */
public class SecretValueTest {

    @Test
    public void testEquals() {
        byte[] value1 = {1, 2, 3, 4};
        byte[] value2 = {1, 2, 3, 4};
        byte[] value3 = {1, 2, 3, 5};

        SecretValue secretValue1 = new SecretValue(value1, SecretType.OPAQUE);
        SecretValue secretValue2 = new SecretValue(value2, SecretType.OPAQUE);
        SecretValue secretValue3 = new SecretValue(value3, SecretType.OPAQUE);

        assertTrue(secretValue1.equals(secretValue2));
        assertFalse(secretValue1.equals(secretValue3));
    }
}
