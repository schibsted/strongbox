/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author kvlees
 * @author stiankri
 */
public class SecretsGroupIdentifierTest {

    @Test
    public void testConstructWithValidNames() {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(Region.US_WEST_1, "project.service.group1");
        assertEquals(group.name, "project.service.group1");
        assertEquals(group.region, Region.US_WEST_1);
        assertEquals(group.toString(), "project.service.group1 [us-west-1]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructWithNameTooShort() {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(Region.US_WEST_1, "ab");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructWithNameTooLong() {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(
               Region.US_WEST_1, "aGroupWithAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongName");
    }
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructWithTrailingPeriod() {
        SecretsGroupIdentifier group = new SecretsGroupIdentifier(Region.US_WEST_1, "project.service.");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructWithInvalid() {
        SecretsGroupIdentifier secret = new SecretsGroupIdentifier(Region.US_WEST_1, "project-service-group1");
    }

    @Test
    public void testEquals() throws Exception {
        SecretsGroupIdentifier group1 = new SecretsGroupIdentifier(Region.US_WEST_1, "project.service.group1");
        SecretsGroupIdentifier group2 = new SecretsGroupIdentifier(Region.US_WEST_1, "project.service.group1");
        SecretsGroupIdentifier group3 = new SecretsGroupIdentifier(Region.EU_WEST_1, "project.service.group1");
        SecretsGroupIdentifier group4 = new SecretsGroupIdentifier(Region.US_WEST_1, "project.service.group2");

        assertTrue(group1.equals(group2));
        assertFalse(group1.equals(group3));
        assertFalse(group1.equals(group4));
        assertFalse(group1.equals("some string value"));
    }

    @Test
    public void testCompareTo() throws Exception {
        // Compare first sorts by region and then by name.
        // Regions are sorted by the ordering of the Regions enum. Name will be sorted using regular string
        // comparision.
        SecretsGroupIdentifier group1 = new SecretsGroupIdentifier(Region.US_WEST_1, "project.service.group1");
        SecretsGroupIdentifier group2 = new SecretsGroupIdentifier(Region.US_WEST_1, "project.service.group1");
        SecretsGroupIdentifier group3 = new SecretsGroupIdentifier(Region.EU_WEST_1, "project.service.group1");
        SecretsGroupIdentifier group4 = new SecretsGroupIdentifier(Region.US_WEST_1, "project.service.group2");

        assertEquals(group1.compareTo(group1), 0);
        assertEquals(group1.compareTo(group2), 0);
        assertEquals(group1.compareTo(group3), -2); // US_WEST_1 precedes EU_WET_1 because of the enum ordering
        assertEquals(group1.compareTo(group4), -1); // Group name ending "group1" precedes "group2"
        assertEquals(group3.compareTo(group4), 2); // EU will come after US in enum ordering
    }
}
