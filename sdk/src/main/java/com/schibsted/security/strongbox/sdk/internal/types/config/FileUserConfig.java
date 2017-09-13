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
