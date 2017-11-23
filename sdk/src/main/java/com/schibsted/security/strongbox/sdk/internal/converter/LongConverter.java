/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.converter;

/**
 * @author stiankri
 */
public class LongConverter implements Converter<Long> {
    @Override
    public String toString(Long value) {
        return value.toString();
    }

    @Override
    public Long fromString(String value) {
        return Long.valueOf(value);
    }

    @Override
    public Class<Long> getType() {
        return Long.class;
    }

    @Override
    public Object toObject(Long value) {
        return value;
    }

    @Override
    public Long fromObject(Object value) {
        return (Long)value;
    }

    @Override
    public Class<?> getConvertedType() {
        return Long.class;
    }
}
