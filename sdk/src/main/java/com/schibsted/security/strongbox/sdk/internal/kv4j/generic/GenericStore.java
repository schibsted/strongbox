/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generic;

import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.KVStream;

import java.util.Set;

/**
 * @author stiankri
 */
public interface GenericStore<T, R> {
    void create(T entry);
    void update(T entry, T existingEntry);
    void delete(R partitionKey);
    Set<R> keySet();

    // TODO: look into lazy stream or use an iterator if the result is too big to fetch in one go
    KVStream<T> stream();
}
