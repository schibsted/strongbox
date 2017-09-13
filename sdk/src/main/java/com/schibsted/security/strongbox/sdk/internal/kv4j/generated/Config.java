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

package com.schibsted.security.strongbox.sdk.internal.kv4j.generated;

import com.schibsted.security.strongbox.sdk.internal.converter.ByteArrayConverter;
import com.schibsted.security.strongbox.sdk.internal.converter.LongConverter;
import com.schibsted.security.strongbox.sdk.internal.converter.StringConverter;
import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.RSEF;
import com.schibsted.security.strongbox.sdk.internal.converter.Converters;
import com.schibsted.security.strongbox.sdk.internal.converter.SecretIdentifierConverter;
import com.schibsted.security.strongbox.sdk.internal.converter.StateConverter;
import com.schibsted.security.strongbox.sdk.internal.converter.ZonedDateTimeConverter;
import com.schibsted.security.strongbox.sdk.internal.converter.FormattedTimestamp;
import com.schibsted.security.strongbox.sdk.types.State;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;

import java.time.ZonedDateTime;

/**
 * The idea was to auto-generate this config based on annotations in the data object to be stored.
 *
 * It is more likely that most of the reflection code will be removed in the future.
 *
 * @author stiankri
 */
public class Config {
    public static final int KEY = 1;
    public static final int VERSION = 2;
    public static final int STATE = 3;
    public static final int NOT_BEFORE = 4;
    public static final int NOT_AFTER = 5;
    public static final int VALUE = 6;

    public static RSEF.PartitionKey<RawSecretEntry, SecretIdentifier> name = new RSEF.PartitionKey<>(KEY);
    public static RSEF.SortKey<RawSecretEntry, Long> version = new RSEF.SortKey<>(VERSION);
    public static RSEF.Attribute<RawSecretEntry, State> state = new RSEF.Attribute<>(STATE);
    public static RSEF.OptionalAttribute<RawSecretEntry, ZonedDateTime> notBefore = new RSEF.OptionalAttribute<>(NOT_BEFORE);
    public static RSEF.OptionalAttribute<RawSecretEntry, ZonedDateTime> notAfter = new RSEF.OptionalAttribute<>(NOT_AFTER);

    public static RSEF.AttributeCondition active() {
        ZonedDateTime now = FormattedTimestamp.now();
        return state.eq(State.ENABLED)
                .AND(notBefore.isNotPresent().OR(notBefore.get().le(now)))
                .AND(notAfter.isNotPresent().OR(notAfter.get().ge(now)));
    }

    public static RSEF.AttributeCondition disabled() {
        ZonedDateTime now = FormattedTimestamp.now();
        return state.eq(State.DISABLED)
                // TODO: figure out what DynamoDB does with missing attributes as this is not short circuit and does not make sense right now
                .OR(notBefore.isPresent().AND(notBefore.get().lt(now)))
                .AND(notAfter.isPresent().OR(notAfter.get().gt(now)));
    }

    public static Converters converters = new Converters(
                        new SecretIdentifierConverter(),
                        new LongConverter(),
                        new StringConverter(),
                        new StateConverter(),
                        new ByteArrayConverter(),
                        new ZonedDateTimeConverter());
}
