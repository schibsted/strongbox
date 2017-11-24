/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Joiner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stiankri
 */
public enum PrincipalType {
    GROUP("group"),
    USER("user"),
    ROLE("role");

    public String name;
    private static Map<String, PrincipalType> typeMap = new HashMap<>();
    static {
        for (PrincipalType type : PrincipalType.values()) {
            typeMap.put(type.name, type);
        }
    }

    PrincipalType(String name) {
        this.name = name;
    }

    public static PrincipalType fromString(String principalType) {
        PrincipalType type = typeMap.get(principalType);
        if (type == null) {
            throw new IllegalArgumentException(String.format("Unrecognized PrincipalType '%s', expected one of {%s}",
                    principalType, Joiner.on(", ").join(typeMap.keySet())));
        }
        return type;
    }

    // TODO create render type
    @JsonValue
    @Override
    public String toString() {
        return name;
    }
}
