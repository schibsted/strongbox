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

package com.schibsted.security.strongbox.cli.config;

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
