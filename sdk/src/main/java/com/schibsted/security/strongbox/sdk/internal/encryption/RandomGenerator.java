/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

/**
 * @author stiankri
 */
public interface RandomGenerator {
    byte[] generateRandom(Integer numberOfBytes);
}
