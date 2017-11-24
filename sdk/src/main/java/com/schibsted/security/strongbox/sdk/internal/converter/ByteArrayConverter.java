/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.converter;

/**
 * @author stiankri
 */
public class ByteArrayConverter implements Converter<byte[]> {
    @Override
    public String toString(byte[] value) {
        return Encoder.base64encode(value);
    }

    @Override
    public byte[] fromString(String value) {
        return Encoder.base64decode(value);
    }

    @Override
    public Class<byte[]> getType() {
        return byte[].class;
    }

    @Override
    public Object toObject(byte[] value) {
        return value;
    }

    @Override
    public byte[] fromObject(Object value) {
        return (byte[])value;
    }

    @Override
    public Class<?> getConvertedType() {
        return byte[].class;
    }
}
