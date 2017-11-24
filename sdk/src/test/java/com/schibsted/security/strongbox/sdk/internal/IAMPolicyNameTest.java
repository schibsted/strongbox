/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal;

import com.schibsted.security.strongbox.sdk.exceptions.InvalidResourceName;
import com.schibsted.security.strongbox.sdk.internal.access.AccessLevel;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author kvlees
 */
public class IAMPolicyNameTest {
    private SecretsGroupIdentifier group = new SecretsGroupIdentifier(Region.US_WEST_1, "test.group");
    private IAMPolicyName adminPolicyName = new IAMPolicyName(group, AccessLevel.ADMIN);
    private IAMPolicyName readonlyPolicyName = new IAMPolicyName(group, AccessLevel.READONLY);
    private String adminPolicyAsString = "strongbox_us-west-1_test-group_admin";
    private String readonlyPolicyAsString = "strongbox_us-west-1_test-group_readonly";


    @Test
    public void testToString() throws Exception {
        assertEquals(adminPolicyName.toString(), adminPolicyAsString);
        assertEquals(readonlyPolicyName.toString(), readonlyPolicyAsString);
    }

    @Test
    public void testFromString() throws Exception {
        IAMPolicyName adminFromString = IAMPolicyName.fromString(adminPolicyAsString);
        assertEquals(adminFromString.group, group);
        assertEquals(adminFromString.accessLevel, AccessLevel.ADMIN);


        IAMPolicyName readonlyFromString = IAMPolicyName.fromString(readonlyPolicyAsString);
        assertEquals(readonlyFromString.group, group);
        assertEquals(readonlyFromString.accessLevel, AccessLevel.READONLY);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromStringInvalidAccessLevel() throws Exception {
        IAMPolicyName.fromString("strongbox_us-west-1_test-group_invalid");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void testFromStringMissingRegion() throws Exception {
        IAMPolicyName.fromString("strongbox_test-group_admin");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void testFromStringInvalidPrefix() throws Exception {
        IAMPolicyName.fromString("sm_eu-west-1_test-group_admin");
    }

    @Test
    public void testToAndFromStringAreSymmetric() {
        assertEquals(IAMPolicyName.fromString(adminPolicyAsString).toString(), adminPolicyAsString);
        assertEquals(IAMPolicyName.fromString(adminPolicyName.toString()), adminPolicyName);
    }

    @Test
    public void testEquals() throws Exception {
        // Some cases that should be equal.
        assertTrue(adminPolicyName.equals(new IAMPolicyName(group, AccessLevel.ADMIN)));
        assertTrue(readonlyPolicyName.equals(new IAMPolicyName(group, AccessLevel.READONLY)));

        // Some cases that should not be equal.
        assertFalse(adminPolicyName.equals(readonlyPolicyName));
        assertFalse(adminPolicyName.equals(
                new IAMPolicyName(new SecretsGroupIdentifier(Region.EU_WEST_1, "test.group"), AccessLevel.ADMIN)));
        assertFalse(adminPolicyName.equals(
                new IAMPolicyName(new SecretsGroupIdentifier(Region.US_WEST_1, "test.group2"), AccessLevel.ADMIN)));
    }
}
