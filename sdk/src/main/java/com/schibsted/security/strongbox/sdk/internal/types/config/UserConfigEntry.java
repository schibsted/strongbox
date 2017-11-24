/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.types.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

/**
 * @author stiankri
 */
public class UserConfigEntry {
    @JsonProperty("group")
    public final SecretsGroupIdentifier group;

    @JsonProperty("path")
    public final String path;

    public UserConfigEntry(@JsonProperty("group") SecretsGroupIdentifier group,
                           @JsonProperty("path") String path) {
        this.group = group;
        this.path = path;
    }
}
