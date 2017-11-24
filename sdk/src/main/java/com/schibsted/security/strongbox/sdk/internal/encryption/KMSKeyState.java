/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stiankri
 */
public enum KMSKeyState {
    ENABLED("Enabled"),
    DISABLED("Disabled"),
    PENDING_DELETION("PendingDeletion");

    private String name;
    private static Map<String, KMSKeyState> keyStateMap = new HashMap<>();
    static {
        for (KMSKeyState state : values()) {
            keyStateMap.put(state.name, state);
        }
    }

    KMSKeyState(String name) {
        this.name = name;
    }

    public static KMSKeyState fromString(String keyState) {
        KMSKeyState state = keyStateMap.get(keyState);
        if (state == null) {
            throw new IllegalArgumentException("Unrecognized keyState '" + state + "', expected one of " + keyStateMap.keySet());
        }
        return state;
    }

    @Override
    public String toString() {
        return name;
    }
}
