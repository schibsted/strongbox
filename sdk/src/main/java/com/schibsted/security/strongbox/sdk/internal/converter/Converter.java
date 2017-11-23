/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.converter;

/**
 * @author stiankri
 */
public interface Converter<T> {
    String toString(T value);
    T fromString(String value);

    // TODO: remove: only used when throwing away the types in (Converter... converters)
    Class<T> getType();

    Object toObject(T value);
    T fromObject(Object value);

    Class<?> getConvertedType();
}
