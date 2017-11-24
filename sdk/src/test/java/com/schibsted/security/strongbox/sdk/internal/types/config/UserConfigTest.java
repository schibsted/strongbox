/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
