/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal;

import com.schibsted.security.strongbox.sdk.exceptions.InvalidResourceName;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author kvlees
 */
public class RegionLocalResourceNameTest {
    private RegionLocalResourceName resourceName = new RegionLocalResourceName(
            new SecretsGroupIdentifier(Region.US_WEST_1, "test.group"));
    private String resourceAsString = "strongbox_us-west-1_test-group";

    @Test
    public void testToString() throws Exception {
        assertEquals(resourceName.toString(), resourceAsString);
    }

    @Test
    public void testFromString() throws Exception {
        assertEquals(RegionLocalResourceName.fromString(resourceAsString).group, resourceName.group);
        assertEquals(RegionLocalResourceName.fromString(resourceAsString).toString(), resourceAsString);
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void testFromStringInvalidPrefix() throws Exception {
        RegionLocalResourceName.fromString("sm_us-west-1_test-group");
    }

    @Test(expectedExceptions = InvalidResourceName.class)
    public void testFromStringMissingRegion() throws Exception {
        RegionLocalResourceName.fromString("strongbox_test-group");
    }

    @Test
    public void testEquals() {
        assertTrue(resourceName.equals(resourceName));
        assertTrue(resourceName.equals(new RegionLocalResourceName(new SecretsGroupIdentifier(Region.US_WEST_1, "test.group"))));

        assertFalse(resourceName.equals(new RegionLocalResourceName(new SecretsGroupIdentifier(Region.EU_WEST_1, "test.group"))));
        assertFalse(resourceName.equals(new RegionLocalResourceName(new SecretsGroupIdentifier(Region.US_WEST_1, "test.group2"))));
        assertFalse(resourceName.equals(resourceAsString));
    }
}
