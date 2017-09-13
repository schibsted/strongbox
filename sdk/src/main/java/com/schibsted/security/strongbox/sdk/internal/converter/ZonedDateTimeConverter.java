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
