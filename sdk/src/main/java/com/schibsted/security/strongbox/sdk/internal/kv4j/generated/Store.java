/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.kv4j.generated;

import com.schibsted.security.strongbox.sdk.internal.kv4j.generic.frontend.KVStream;
import com.schibsted.security.strongbox.sdk.internal.interfaces.ManagedResource;
import com.schibsted.security.strongbox.sdk.types.RawSecretEntry;
import com.schibsted.security.strongbox.sdk.types.SecretIdentifier;

import java.util.Set;

/**
 * @author stiankri
 */
public interface Store extends AutoCloseable, ManagedResource {
    void create(RawSecretEntry entry);
    void update(RawSecretEntry entry, RawSecretEntry existingEntry);
    void delete(SecretIdentifier secretIdentifier);
    Set<SecretIdentifier> keySet();

    @Override
    void close();

    // TODO: look into lazy streams or use an iterator if the result is too big to fetch in one go
    KVStream<RawSecretEntry> stream();

}
