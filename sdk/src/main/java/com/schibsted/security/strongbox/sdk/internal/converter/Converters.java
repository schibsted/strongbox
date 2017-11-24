/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author stiankri
 */
public class Converters {
    private Map<Class, Converter> map = new HashMap<>();

    public Converters(Converter... converters) {
        for (Converter converter: converters) {
            addConverter(converter.getType(), converter);
        }
    }

    public <T> void addConverter(Class<T> type, Converter<T> converter) {
        map.put(type, converter);
    }

    public <T> String to(T t) {
        if (t instanceof String) {
            return (String)t;
        }

        if (t instanceof Optional) {
            Optional optional = (Optional) t;
            if (optional.isPresent()) {
                Object o = optional.get();
                return map.get(o.getClass()).toString(o);
            } else {
                return null;
            }
        } else {
            return map.get(t.getClass()).toString(t);
        }
    }

    public <T> T from(String value, Class<T> type) {
        if (type.equals(String.class)) {
            return (T)value;
        }
        return ((Converter<T>)map.get(type)).fromString(value);
    }

    public <T> Object toObject(T t) {
        if (t instanceof Optional) {
            Optional optional = (Optional) t;
            if (optional.isPresent()) {
                Object o = optional.get();
                return map.get(o.getClass()).toObject(o);
            } else {
                return null;
            }
        } else {
            return map.get(t.getClass()).toObject(t);
        }
    }

    public <T> T fromObject(Object value, Class<T> type) {
        return ((Converter<T>)map.get(type)).fromObject(value);
    }

    public <T> Optional<T> fromOptionalObject(Object value, Class<T> type) {
        if (value == null) {
            return Optional.empty();
        } else {
            return Optional.of(((Converter<T>)map.get(type)).fromObject(value));
        }
    }

    public <T> Optional<T> fromOptional(String value, Class<T> type) {
        if (value == null) {
            return Optional.empty();
        } else {
            return Optional.of(((Converter<T>)map.get(type)).fromString(value));
        }
    }

    public <T> Class<?> getConvertedType(Class<?> targetType) {
        return map.get(targetType).getConvertedType();
    }
}
