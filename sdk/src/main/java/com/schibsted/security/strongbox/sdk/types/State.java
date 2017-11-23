/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Joiner;
import com.schibsted.security.strongbox.sdk.internal.json.StateDeserializer;
import com.schibsted.security.strongbox.sdk.internal.json.StateSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stiankri
 */
@JsonSerialize(using = StateSerializer.class)
@JsonDeserialize(using = StateDeserializer.class)
public enum State {
    COMPROMISED("compromised", (byte)0),
    DISABLED("disabled", (byte)1),
    ENABLED("enabled", (byte)2);

    static Map<String, State> stateMap = new HashMap<>();
    static Map<Byte, State> valueMap = new HashMap<>();
    static {
        for (State state : values()) {
            stateMap.put(state.name, state);
            valueMap.put(state.value, state);
        }
    }

    private final String name;
    private final byte value;

    State(final String name, byte value) {
        this.name = name;
        this.value = value;
    }

    public static State fromString(String state) {
        State s = stateMap.get(state);
        if (s == null) {
            throw new IllegalArgumentException(String.format("Unrecognized state '%s', expected one of {%s}",
                    state, Joiner.on(", ").join(stateMap.keySet())));
        }
        return s;
    }

    @Override
    public String toString() {
        return name;
    }

    public byte asByte() {
        return value;
    }

    public static State fromByte(byte value) {
        return valueMap.get(value);
    }
}
