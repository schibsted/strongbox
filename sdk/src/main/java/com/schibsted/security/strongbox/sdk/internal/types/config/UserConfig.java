/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.types.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.schibsted.security.strongbox.sdk.exceptions.AlreadyExistsException;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author stiankri
 */
public class UserConfig {
    @JsonProperty("localFiles")
    protected final Map<SecretsGroupIdentifier, File> localFiles;

    protected void checkUniqueGroup(SecretsGroupIdentifier group) {
        if (localFiles.containsKey(group)) {
            throw new AlreadyExistsException(String.format("Group %s already found in user config ", group.name));
        }
    }

    protected void checkUniqueFilePath(File path) {
        if (localFiles.containsValue(path)) {
            throw new AlreadyExistsException(String.format("Path %s already found in user config", path.getAbsoluteFile()));
        }
    }

    public UserConfig(@JsonProperty("localFiles") Map<SecretsGroupIdentifier, File> localFiles) {
        this.localFiles = localFiles;
    }

    public UserConfig() {
        this.localFiles = new HashMap<>();
    }

    public Map<SecretsGroupIdentifier, File> getMap() {
        return localFiles;
    }

    public Optional<File> getLocalFilePath(SecretsGroupIdentifier group) {
        return Optional.ofNullable(localFiles.get(group));
    }

    public void addLocalFilePath(SecretsGroupIdentifier group, File path) {
        checkUniqueGroup(group);
        checkUniqueFilePath(path);
        localFiles.put(group, path);
    }

    public void updateLocalFilePath(SecretsGroupIdentifier group, File path) {
        checkUniqueFilePath(path);
        localFiles.put(group, path);
    }

    public void removeLocalFilePath(SecretsGroupIdentifier group) {
        localFiles.remove(group);
    }
}
