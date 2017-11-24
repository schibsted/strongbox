/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.types;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stiankri
 */
public enum SecretType {
    // TODO: define types
    //ACCESS_TOKEN, PASSPHRASE, PUBLIC, PRIVATE, OPAQUE
    OPAQUE("opaque", (byte)0);

    static Map<String, SecretType> nameMap = new HashMap<>();
    static Map<Byte, SecretType> valueMap = new HashMap<>();
    static {
        for (SecretType type : values()) {
            valueMap.put(type.value, type);
            nameMap.put(type.name, type);
        }
    }

    private final byte value;
    private final String name;

    SecretType(String name, byte value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return name;
    }

    public byte asByte() {
        return value;
    }

    public static SecretType fromByte(byte value) {
        return valueMap.get(value);
    }
}
