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
