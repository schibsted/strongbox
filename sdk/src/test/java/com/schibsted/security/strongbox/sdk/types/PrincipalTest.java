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

import com.schibsted.security.strongbox.sdk.exceptions.InvalidResourceName;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author kvlees
 * @author hawkaa
 */
public class PrincipalTest {

    @Test
    public void testFromArn() {
        Principal principal;
        principal = Principal.fromArn("arn:aws:iam::12345:user/bob", "12345");
        assertEquals(principal.type, PrincipalType.USER);
        assertEquals(principal.name, "bob" );

        principal = Principal.fromArn("arn:aws:iam::12345:group/team-awesome", "12345");
        assertEquals(principal.type, PrincipalType.GROUP);
        assertEquals(principal.name, "team-awesome");

        principal = Principal.fromArn("arn:aws:iam::12345:role/myrole", "12345");
        assertEquals(principal.type, PrincipalType.ROLE);
        assertEquals(principal.name, "myrole");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void testFromArnInvalidPrefix() {
        Principal principal = Principal.fromArn("arn:aws:invalid::12345:user/username", "12345");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void testFromArnInvalidNumberParts() {
        Principal principal = Principal.fromArn("arn:aws:iam::user/username", "12345");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromArnInvalidType() {
        Principal principal = Principal.fromArn("arn:aws:iam::12345:invalid-type/username", "12345");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void testFromArnInvalidResource() {
        Principal principal = Principal.fromArn("arn:aws:iam::12345:user/username/group", "12345");
    }

    @Test
    public void testToString() {
        Principal principal = Principal.fromArn("arn:aws:iam::12345:user/bob", "12345");
        assertEquals(principal.toString(), "bob [user]");
    }

    @Test
    public void testEquals() {
        Principal principal1 = Principal.fromArn("arn:aws:iam::12345:user/bob", "12345");
        Principal principal2 = new Principal(PrincipalType.USER, "bob");
        assertTrue(principal1.equals(principal2));
    }

    @Test
    public void testNotEquals() {
        Principal principal1 = Principal.fromArn("arn:aws:iam::12345:user/bob", "12345");
        Principal principal2 = new Principal(PrincipalType.GROUP, "bob");
        Principal principal3 = new Principal(PrincipalType.USER, "alice");
        assertFalse(principal1.equals(principal2));
        assertFalse(principal1.equals(principal3));
        assertFalse(principal2.equals(principal1));
        assertFalse(principal1.equals("some string value"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAnotherAccount() {
        Principal principal = Principal.fromArn("arn:aws:iam::123456:user/bob", "12345");
    }
}
