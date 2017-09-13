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

package com.schibsted.security.strongbox.sdk.internal.types.config;

import com.schibsted.security.strongbox.sdk.exceptions.AlreadyExistsException;
import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Optional;

import static org.testng.Assert.assertEquals;

/**
 * @author kvlees
 */
public class UserConfigTest {
    private static final SecretsGroupIdentifier EU_GROUP =  new SecretsGroupIdentifier(Region.EU_WEST_1, "test.group");
    private static final SecretsGroupIdentifier US_GROUP =  new SecretsGroupIdentifier(Region.US_WEST_1, "test.group");
    private UserConfig userConfig;

    @BeforeMethod
    public void setUp() throws Exception {
        userConfig = new UserConfig();
    }

    @Test
    public void testConfig() {
        assertEquals(userConfig.getMap().size(), 0);

        // Try removing one that does not exist.
        userConfig.removeLocalFilePath(new SecretsGroupIdentifier(Region.US_WEST_1, "test.group"));

        // Add some files.
        userConfig.addLocalFilePath(EU_GROUP, new File("path-eu"));
        userConfig.addLocalFilePath(US_GROUP, new File("path-us"));
        assertEquals(userConfig.getMap().size(), 2);

        Optional<File> path = userConfig.getLocalFilePath(EU_GROUP);
        assertEquals(userConfig.getLocalFilePath(EU_GROUP).get().getPath(), "path-eu");
        assertEquals(userConfig.getLocalFilePath(US_GROUP).get().getPath(), "path-us");

        userConfig.updateLocalFilePath(EU_GROUP, new File("path-foobar"));
        assertEquals(userConfig.getLocalFilePath(EU_GROUP).get().getPath(), "path-foobar");
    }

    @Test(expectedExceptions = AlreadyExistsException.class)
    public void addSamePathToConfig() {
        SecretsGroupIdentifier anotherGroup = new SecretsGroupIdentifier(Region.EU_CENTRAL_1, "test2.group");

        userConfig.addLocalFilePath(EU_GROUP, new File("path-eu"));
        userConfig.addLocalFilePath(anotherGroup, new File("path-eu"));
    }

    @Test(expectedExceptions = AlreadyExistsException.class)
    public void addSameGroupToConfigWithDifferentPath() {
        userConfig.addLocalFilePath(EU_GROUP, new File("path-eu"));
        userConfig.addLocalFilePath(EU_GROUP, new File("path-foobar"));
    }
}
