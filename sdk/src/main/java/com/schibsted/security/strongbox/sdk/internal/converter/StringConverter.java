/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.converter;

/**
 * @author stiankri
 */
public class StringConverter implements Converter<String> {
    @Override
    public String toString(String value) {
        return value;
    }

    @Override
    public String fromString(String value) {
        return value;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public Object toObject(String value) {
        return value;
    }

    @Override
    public String fromObject(Object value) {
        return (String)value;
    }

    @Override
    public Class<?> getConvertedType() {
        return String.class;
    }
}
