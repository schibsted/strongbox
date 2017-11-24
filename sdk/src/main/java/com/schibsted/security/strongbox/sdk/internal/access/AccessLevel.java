/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.access;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stiankri
 */
public enum AccessLevel {
    ADMIN("admin"),
    READONLY("readonly");

    static Map<String, AccessLevel> accessLevelMap = new HashMap<>();
    static {
        for (AccessLevel accessLevel : values()) {
            accessLevelMap.put(accessLevel.name, accessLevel);
        }
    }

    private final String name;
    AccessLevel(final String name) {
        this.name = name;
    }

    public static AccessLevel fromString(String accessLevel) {
        AccessLevel al = accessLevelMap.get(accessLevel);
        if (al == null) {
            throw new IllegalArgumentException("Unrecognized accessLevel '" + accessLevel + "', expected one of " + accessLevelMap.keySet());
        }
        return al;
    }

    @Override
    public String toString() {
        return name;
    }
}
