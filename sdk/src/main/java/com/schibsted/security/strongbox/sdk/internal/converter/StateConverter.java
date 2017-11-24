/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.converter;

import com.schibsted.security.strongbox.sdk.types.State;

/**
 * @author stiankri
 */
public class StateConverter implements Converter<State> {
    @Override
    public String toString(State value) {
        return value.toString();
    }

    @Override
    public State fromString(String value) {
        return State.fromString(value);
    }

    @Override
    public Class<State> getType() {
        return State.class;
    }

    @Override
    public Object toObject(State value) {
        return value.asByte();
    }

    @Override
    public State fromObject(Object value) {
        return State.fromByte((Byte)value);
    }

    @Override
    public Class<?> getConvertedType() {
        return Byte.class;
    }
}
