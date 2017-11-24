/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author kvlees
 * @author stiankri
 * @author hawkaa
 */
// TODO parametrize test
public class SecretIdentifierTest {
    @Test
    public void testConstructWithValidNames() {
        SecretIdentifier secret = new SecretIdentifier("fooBar1");
        assertEquals(secret.name, "fooBar1");
        assertEquals(secret.toString(), "fooBar1");
    }

    @Test
    public void testConstructWithValidUnderscoreNames() {
        SecretIdentifier secret = new SecretIdentifier("foo_bar_1");
        assertEquals(secret.name, "foo_bar_1");
        assertEquals(secret.toString(), "foo_bar_1");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructWithNameTooShort() {
        SecretIdentifier secret = new SecretIdentifier("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructWithNameTooLong() {
        SecretIdentifier secret = new SecretIdentifier(
                "aSecretWithAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVery" +
                "VeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongName");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void trailing_underscore() {
        SecretIdentifier secret = new SecretIdentifier("foo_");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void trailing_hyphen() {
        SecretIdentifier secret = new SecretIdentifier("foo-");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void trailing_period() {
        SecretIdentifier secret = new SecretIdentifier("foo.");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructWithInvalid() {
        SecretIdentifier secret = new SecretIdentifier("toByteArray bar");
    }

    @Test
    public void mixed() {
        SecretIdentifier secret = new SecretIdentifier("foo_Bar.FOZ-3");
    }

    @Test
    public void mixedWithNumbersFirst() {
        SecretIdentifier secret = new SecretIdentifier("123foo_Bar.FOZ-3");
    }

    @Test
    public void justNumbers() {
        SecretIdentifier secret = new SecretIdentifier("123");
    }

    @Test
    public void numericalFirstInName() {
        SecretIdentifier secret = new SecretIdentifier("567secretName");
    }

    @Test
    public void underscore() {
        SecretIdentifier secret = new SecretIdentifier("foo_bar");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void consecutive_underscore() {
        SecretIdentifier secret = new SecretIdentifier("foo__bar");
    }

    @Test
    public void hyphen() {
        SecretIdentifier secret = new SecretIdentifier("foo-bar");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void consecutive_hyphens() {
        SecretIdentifier secret = new SecretIdentifier("foo--bar");
    }

    @Test
    public void period() {
        SecretIdentifier secret = new SecretIdentifier("foo.bar");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void consecutive_periods() {
        SecretIdentifier secret = new SecretIdentifier("foo..bar");
    }

    @Test
    public void testEquals() throws Exception {
        SecretIdentifier secret1 = new SecretIdentifier("fooBar1");
        SecretIdentifier secret2 = new SecretIdentifier("fooBar1");
        SecretIdentifier secret3 = new SecretIdentifier("fooBar2");

        assertTrue(secret1.equals(secret2));
        assertFalse(secret1.equals(secret3));
        assertFalse(secret1.equals("some value"));
    }
}
