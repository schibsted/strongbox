/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.converter;

import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;

/**
 * @author stiankri
 */
public class SecretIdentifierConverter implements Converter<SecretIdentifier> {
    @Override
    public String toString(SecretIdentifier value) {
        return value.toString();
    }

    @Override
    public SecretIdentifier fromString(String value) {
        return new SecretIdentifier(value);
    }

    @Override
    public Class<SecretIdentifier> getType() {
        return SecretIdentifier.class;
    }

    @Override
    public Object toObject(SecretIdentifier value) {
        return toString(value);
    }

    @Override
    public SecretIdentifier fromObject(Object value) {
        return fromString((String)value);
    }

    @Override
    public Class<?> getConvertedType() {
        return String.class;
    }
}
