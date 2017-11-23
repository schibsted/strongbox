/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
            throw new RuntimeException(String.format("Failed to load config from '%s': %s", file.getAbsolutePath(), e.getMessage()), e);
        }
    }

    Config getConfig(final BufferedReader reader) {
        try {
            Map<String, Section> sections = new HashMap<>();

            Optional<String> currentSection = Optional.empty();
            String currentLine;
            int lineNumber = 1;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.isEmpty() || currentLine.startsWith(";") || currentLine.startsWith("#")) {
                    continue;
                }

                Optional<String> newSection = extractSection(currentLine);
                if (newSection.isPresent()) {
                    currentSection = newSection;
                } else {
                    Property property = extractProperty(currentLine, lineNumber);
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

    Property extractProperty(final String line, int lineNumber) {
        Matcher matcher = propertyPattern.matcher(line);

        if (!matcher.find()) {
            throw new IllegalStateException(String.format("Failed to interpret line #%d as 'key=value'. Please note that comment lines must start with '#'.", lineNumber));
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