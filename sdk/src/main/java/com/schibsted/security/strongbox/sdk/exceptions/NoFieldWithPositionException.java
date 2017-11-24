/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.exceptions;

/**
 * @author stiankri
 */
public class NoFieldWithPositionException extends RuntimeException {
    public NoFieldWithPositionException(int position, String className) {
        super(String.format("No field with annotation '%d' in class '%s'", position, className));
    }
}
