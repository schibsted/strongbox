/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.converter;

import java.time.ZonedDateTime;

/**
 * @author stiankri
 */
public class ZonedDateTimeConverter implements Converter<ZonedDateTime> {
    @Override
    public String toString(ZonedDateTime value) {
        return FormattedTimestamp.from(value);
    }

    @Override
    public ZonedDateTime fromString(String value) {
        return FormattedTimestamp.from(value);
    }

    @Override
    public Class<ZonedDateTime> getType() {
        return ZonedDateTime.class;
    }

    @Override
    public Object toObject(ZonedDateTime value) {
        return FormattedTimestamp.epoch(value);
    }

    @Override
    public ZonedDateTime fromObject(Object value) {
        return FormattedTimestamp.fromEpoch((Long)value);
    }

    @Override
    public Class<?> getConvertedType() {
        return Long.class;
    }
}
