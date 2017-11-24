/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stiankri
 */
public enum Encoding {
    BINARY("binary", (byte)0),
    UTF8("utf8", (byte)1);

    static Map<String, Encoding> nameMap = new HashMap<>();
    static Map<Byte, Encoding> valueMap = new HashMap<>();
    static {
        for (Encoding encoding : values()) {
            valueMap.put(encoding.value, encoding);
            nameMap.put(encoding.name, encoding);
        }
    }

    private final byte value;
    private final String name;

    Encoding(String name, byte value) {
        this.value = value;
        this.name = name;
    }

    public byte asByte() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }

    public Encoding fromString(String encoding) {
        return nameMap.get(encoding);
    }

    public static Encoding fromByte(byte value) {
        return valueMap.get(value);
    }
}
