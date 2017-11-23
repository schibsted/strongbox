/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.converter;

import java.math.BigInteger;

/**
 * @author stiankri
 */
public class BigIntegerConverter implements Converter<BigInteger> {
    @Override
    public String toString(BigInteger value) {
        return FormattedInteger.paddedInteger(value);
    }

    @Override
    public BigInteger fromString(String value) {
        return FormattedInteger.fromPadded(value);
    }

    @Override
    public Class<BigInteger> getType() {
        return BigInteger.class;
    }

    @Override
    public Object toObject(BigInteger value) {
        return toString(value);
    }

    @Override
    public BigInteger fromObject(Object value) {
        return fromString((String)value);
    }

    @Override
    public Class<?> getConvertedType() {
        return String.class;
    }
}
