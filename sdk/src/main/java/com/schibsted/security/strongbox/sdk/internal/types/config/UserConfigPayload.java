/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.types.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author stiankri
 */
public class UserConfigPayload {
    @JsonProperty("localFiles")
    public List<UserConfigEntry> localFiles;

    public UserConfigPayload(@JsonProperty("localFiles") List<UserConfigEntry> localFiles) {
        this.localFiles = localFiles;
    }

    public UserConfigPayload(UserConfig userConfig) {
        localFiles = new ArrayList<>();
        for (Map.Entry<SecretsGroupIdentifier, File> entry : userConfig.getMap().entrySet()) {
            localFiles.add(new UserConfigEntry(entry.getKey(), entry.getValue().getAbsolutePath()));
        }
    }
}
