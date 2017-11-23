/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
