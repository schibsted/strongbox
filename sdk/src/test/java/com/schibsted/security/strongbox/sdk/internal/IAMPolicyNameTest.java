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
