/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend;

import java.util.Optional;

/**
 * Key Value Stream to build LINQ inspired queries that will be executed in back ends such as DynamoDB and Files
 *
 * @author stiankri
 */
public class KVStream<T> extends SecretEventStream.EntryStreamKey<T> {
    public KVStream(SecretEventStream.Executor<T> executor) {
        super(executor);
    }

    public SecretEventStream.EntryStreamKey<T> filter(RSEF.KeyCondition condition) {
        keyCondition = Optional.of(condition);
        return this;
    }
}
