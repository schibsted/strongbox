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

package com.schibsted.security.strongbox.sdk.internal.config;

import com.amazonaws.profile.path.AwsProfileFileLocationProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class was only written to avoid having an extra external dependency
 *
 * @author stiankri
 */
public class AWSCLIConfigFile {
    private static final Pattern sectionPattern = Pattern.compile("\\[([^\\[\\]]*)]");
    private static final Pattern propertyPattern = Pattern.compile("([^=]*)=([^=]*)");

    private final File file;

    public AWSCLIConfigFile(final File file) {
        this.file = file;
    }

    public Config getConfig() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            return getConfig(bufferedReader);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to load config from '%s'", file.getAbsolutePath()), e);
        }
    }

    Config getConfig(final BufferedReader reader) {
        try {
            Map<String, Section> sections = new HashMap<>();

            Optional<String> currentSection = Optional.empty();
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.isEmpty() || currentLine.startsWith(";")) {
                    continue;
                }

                Optional<String> newSection = extractSection(currentLine);
                if (newSection.isPresent()) {
                    currentSection = newSection;
                } else {
                    Property property = extractProperty(currentLine);
                    Section sectionToUpdate = sections.computeIfAbsent(currentSection.orElse(""), Section::new);
                    sectionToUpdate.addProperty(property);
                }
            }

            return new Config(sections);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config", e);
        }
    }


    Optional<String> extractSection(final String line) {
        Matcher matcher = sectionPattern.matcher(line);
        if (matcher.find()) {
            return Optional.of(matcher.group(1).trim());
        } else {
            return Optional.empty();
        }
    }

    Property extractProperty(final String line) {
        Matcher matcher = propertyPattern.matcher(line);

        if (!matcher.find()) {
            throw new IllegalStateException(String.format("Failed to read '%s' as a property on the form 'key=value'", line));
        }

        return new Property(matcher.group(1).trim(), matcher.group(2).trim());
    }

    public static class Config {
        private final Map<String, Section> sections;

        Config(final Map<String, Section> sections) {
            this.sections = sections;
        }

        public Optional<Section> getSection(final String sectionName) {
            return Optional.ofNullable(sections.get(sectionName));
        }
    }

    public static class Section {
        private final Map<String, String> properties = new HashMap<>();
        public final String sectionName;

        Section(final String sectionName) {
            this.sectionName = sectionName;
        }

        void addProperty(final Property property) {
            properties.put(property.key, property.value);
        }

        public Optional<String> getProperty(final String propertyName) {
            return Optional.ofNullable(properties.get(propertyName));
        }
    }

    public static class Property {
        public final String key;
        public final String value;

        Property(final String key, final String value) {
            this.key = key;
            this.value = value;
        }
    }

    public static Optional<File> getCredentialProfilesFile() {
        return Optional.ofNullable(AwsProfileFileLocationProvider.DEFAULT_CREDENTIALS_LOCATION_PROVIDER.getLocation());
    }

    public static Optional<File> getConfigFile() {
        return Optional.ofNullable(AwsProfileFileLocationProvider.DEFAULT_CONFIG_LOCATION_PROVIDER.getLocation());
    }
}