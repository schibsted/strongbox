/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.types.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.schibsted.security.strongbox.sdk.exceptions.ParseException;
import com.schibsted.security.strongbox.sdk.exceptions.SerializationException;
import com.schibsted.security.strongbox.sdk.internal.json.StrongboxModule;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author stiankri
 * @author kvlees
 */
public class FileUserConfig extends UserConfig {
    private File configFile;
    private static ObjectMapper objectMapper = new ObjectMapper().registerModules(new Jdk8Module(), new StrongboxModule());

    public FileUserConfig(File configFile) {
        super(loadLocalFiles(configFile));
        this.configFile = configFile;
    }

    private static Map<SecretsGroupIdentifier, File> loadLocalFiles(File file) {
        try {
            if (file.exists()) {
                UserConfigPayload entry = objectMapper.readValue(file, UserConfigPayload.class);

                return entry.localFiles.stream().collect(Collectors.toMap(e -> e.group,
                        e -> new File(e.path)));
            } else {
                return new HashMap<>();
            }
        } catch (IOException e) {
            throw new ParseException("Failed to load file", e);
        }
    }

    @Override
    public Optional<File> getLocalFilePath(SecretsGroupIdentifier group) {
        return Optional.ofNullable(localFiles.get(group));
    }

    @Override
    public void addLocalFilePath(SecretsGroupIdentifier group, File path) {
        checkUniqueGroup(group);
        checkUniqueFilePath(path);
        localFiles.put(group, path);
        persist();
    }

    @Override
    public void updateLocalFilePath(SecretsGroupIdentifier group, File path) {
        checkUniqueFilePath(path);
        localFiles.put(group, path);
        persist();
    }

    private void persist() {
        try {
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
            }
            UserConfigPayload payload = new UserConfigPayload(this);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, payload);
        } catch (IOException e) {
            throw new SerializationException("Failed to serialize to file", e);
        }
    }

    @Override
    public void removeLocalFilePath(SecretsGroupIdentifier group) {
        localFiles.remove(group);
        persist();
    }
}
