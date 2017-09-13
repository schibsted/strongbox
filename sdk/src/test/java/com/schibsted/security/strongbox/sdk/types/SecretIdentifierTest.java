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
