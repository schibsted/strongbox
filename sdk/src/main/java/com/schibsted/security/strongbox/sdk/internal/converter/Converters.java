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
