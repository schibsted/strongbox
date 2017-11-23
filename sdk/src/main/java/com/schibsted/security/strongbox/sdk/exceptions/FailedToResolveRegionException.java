/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.exceptions;

/**
 * @author stiankri
 */
public class FailedToResolveRegionException extends RuntimeException {

    public FailedToResolveRegionException(String message) {
        super(message);
    }

    public FailedToResolveRegionException(String message, Throwable cause) {
        super(message, cause);
    }
}
