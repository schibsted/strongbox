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
