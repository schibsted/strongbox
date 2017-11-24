/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.config;

import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author stiankri
 */
public class AWSCLIConfigFileTest {

    @Test
    public void single_section_single_property() {
        AWSCLIConfigFile configFile = new AWSCLIConfigFile(new File(""));

        AWSCLIConfigFile.Config config = configFile.getConfig(asBufferedReader("[default]\nkey=value\n"));

        assertSectionAndPropertyExists(config, "default", new AWSCLIConfigFile.Property("key", "value"));
    }

    @Test
    public void multiple_sections_multiple_properties() {
        AWSCLIConfigFile configFile = new AWSCLIConfigFile(new File(""));

        AWSCLIConfigFile.Config config = configFile.getConfig(asBufferedReader("[default]\nkey=value\n[my section]\nkey2=value2\nkey3=value3"));

        assertSectionAndPropertyExists(config, "default", new AWSCLIConfigFile.Property("key", "value"));
        assertSectionAndPropertyExists(config, "my section", new AWSCLIConfigFile.Property("key2", "value2"));
        assertSectionAndPropertyExists(config, "my section", new AWSCLIConfigFile.Property("key3", "value3"));
    }

    @Test
    public void surrounding_spaces_in_section_name() {
        AWSCLIConfigFile configFile = new AWSCLIConfigFile(new File(""));

        AWSCLIConfigFile.Config config = configFile.getConfig(asBufferedReader("[ default  ]\nkey=value\n"));

        assertSectionAndPropertyExists(config, "default", new AWSCLIConfigFile.Property("key", "value"));
    }

    @Test
    public void surrounding_spaces_in_property() {
        AWSCLIConfigFile configFile = new AWSCLIConfigFile(new File(""));

        AWSCLIConfigFile.Config config = configFile.getConfig(asBufferedReader("[default]\n  key = value  \n"));

        assertSectionAndPropertyExists(config, "default", new AWSCLIConfigFile.Property("key", "value"));
    }

    @Test
    public void when_there_are_duplicate_properties_use_the_last() {
        AWSCLIConfigFile configFile = new AWSCLIConfigFile(new File(""));

        AWSCLIConfigFile.Config config = configFile.getConfig(asBufferedReader("[default]\nkey=value\n[default]\nkey=value2\n"));

        assertSectionAndPropertyExists(config, "default", new AWSCLIConfigFile.Property("key", "value2"));
    }

    private void assertSectionAndPropertyExists(AWSCLIConfigFile.Config config, String sectionName, AWSCLIConfigFile.Property expectedProperty) {
        Optional<AWSCLIConfigFile.Section> section = config.getSection(sectionName);

        assertThat("section must be present", section.isPresent());
        assertThat(section.get().sectionName, is(sectionName));

        assertThat(section.get().getProperty(expectedProperty.key), is(Optional.of(expectedProperty.value)));
    }

    private BufferedReader asBufferedReader(String string) {
        return new BufferedReader(new StringReader(string));
    }
}
