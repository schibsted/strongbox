/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.types.config;

import com.schibsted.security.strongbox.sdk.types.Region;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import static org.testng.Assert.*;

/**
 * @author kvlees
 */
public class FileUserConfigTest {
    private File configFile = new File("./test-config.sm");
    private FileUserConfig fileUserConfig;

    private final SecretsGroupIdentifier GROUP1_EU = new SecretsGroupIdentifier(Region.EU_WEST_1, "test.group");
    private final SecretsGroupIdentifier GROUP1_US = new SecretsGroupIdentifier(Region.US_WEST_1, "test.group");
    private final SecretsGroupIdentifier GROUP2_EU = new SecretsGroupIdentifier(Region.EU_WEST_1, "test.group2");

    @BeforeMethod
    public void setUp() throws Exception {
        if (configFile.exists()) {
            configFile.delete();
        }
        fileUserConfig = new FileUserConfig(configFile);
        fileUserConfig.addLocalFilePath(GROUP1_US, new File("uswest1-test-group.sm"));
        fileUserConfig.addLocalFilePath(GROUP1_EU, new File("euwest1-test-group.sm"));
        fileUserConfig.addLocalFilePath(GROUP2_EU, new File("euwest1-test-group2.sm"));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        configFile.delete();
    }

    @Test
    public void testGetLocalFilePath() throws Exception {
        assertEquals(fileUserConfig.getLocalFilePath(GROUP1_US), Optional.of(new File("uswest1-test-group.sm")));
        assertEquals(fileUserConfig.getLocalFilePath(GROUP1_EU), Optional.of(new File("euwest1-test-group.sm")));
        assertEquals(fileUserConfig.getLocalFilePath(GROUP2_EU), Optional.of(new File("euwest1-test-group2.sm")));
        assertEquals(fileUserConfig.getLocalFilePath(
                new SecretsGroupIdentifier(Region.EU_CENTRAL_1, "test.group")), Optional.empty());
    }

    @Test
    public void testUpdateLocalFilePath() throws Exception {
        assertEquals(fileUserConfig.getLocalFilePath(GROUP1_US), Optional.of(new File("uswest1-test-group.sm")));
        fileUserConfig.updateLocalFilePath(GROUP1_US, new File("some-other-file.sm"));
        assertEquals(fileUserConfig.getLocalFilePath(GROUP1_US), Optional.of(new File("some-other-file.sm")));
    }

    @Test
    public void testRemoveLocalFilePath() throws Exception {
        fileUserConfig.removeLocalFilePath(GROUP1_EU);
        fileUserConfig.removeLocalFilePath(GROUP2_EU);

        assertEquals(fileUserConfig.getLocalFilePath(GROUP1_EU), Optional.empty());
        assertEquals(fileUserConfig.getLocalFilePath(GROUP2_EU), Optional.empty());
        assertEquals(fileUserConfig.getLocalFilePath(GROUP1_US), Optional.of(new File("uswest1-test-group.sm")));
    }

    @Test
    public void testGetMap() throws Exception {
        // Creates new fileUserConfig to test the loading from file.
        FileUserConfig fileUserConfig = new FileUserConfig(configFile);
        Map<SecretsGroupIdentifier, File> localFiles = fileUserConfig.getMap();
        assertEquals(localFiles.size(), 3);

        // AbsolutePath required because when they are loaded from file they have the full path
        // rather than relative path.
        assertEquals(localFiles.get(GROUP1_EU), new File("euwest1-test-group.sm").getAbsoluteFile());
        assertEquals(localFiles.get(GROUP1_US), new File("uswest1-test-group.sm").getAbsoluteFile());
        assertEquals(localFiles.get(GROUP2_EU), new File("euwest1-test-group2.sm").getAbsoluteFile());
    }
}
