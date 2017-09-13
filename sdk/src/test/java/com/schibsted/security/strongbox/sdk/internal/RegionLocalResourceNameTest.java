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
