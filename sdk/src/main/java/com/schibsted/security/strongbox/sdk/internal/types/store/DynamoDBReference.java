/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.types.store;

import com.google.common.base.Objects;

/**
 * @author stiankri
 */
public class DynamoDBReference extends StorageReference {
    public DynamoDBReference() {
        storageType = StorageType.DYNAMODB;
    }

    @Override
    public String toString() {
        return "dynamodb";
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(storageType);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof DynamoDBReference;
    }
}
