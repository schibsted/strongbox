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
