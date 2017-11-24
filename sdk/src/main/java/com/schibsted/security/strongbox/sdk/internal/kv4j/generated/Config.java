/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
