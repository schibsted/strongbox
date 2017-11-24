/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import com.google.common.collect.ImmutableMap;
import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

import java.util.Map;

/**
 * @author stiankri
 */
public class FileEncryptionContext implements EncryptionContext {
    private static final String FILE_VERSION = "1";
    public final SecretsGroupIdentifier groupIdentifier;

    public FileEncryptionContext(SecretsGroupIdentifier groupIdentifier) {
        this.groupIdentifier = groupIdentifier;
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of(
                "0", FILE_VERSION,
                "1", groupIdentifier.region.getName(),
                "2", groupIdentifier.name);
    }
}
