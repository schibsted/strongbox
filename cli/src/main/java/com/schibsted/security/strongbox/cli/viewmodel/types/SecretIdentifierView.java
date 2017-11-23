/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.viewmodel.types;

import com.google.common.collect.ImmutableMap;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;

import java.util.Map;

/**
 * @author stiankri
 */
public class SecretIdentifierView implements View {
    public final String name;

    public SecretIdentifierView(SecretIdentifier secretIdentifier) {
        this.name = secretIdentifier.name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Map<String, BinaryString> toMap() {
        ImmutableMap.Builder<String, BinaryString> builder = ImmutableMap.builder();
        builder.put("name", new BinaryString(name));
        return builder.build();
    }

    @Override
    public String uniqueName() {
        return name;
    }
}
