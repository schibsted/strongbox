/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.exceptions;

/**
 * @author kvlees
 */
public class InvalidResourceName extends RuntimeException {

    public InvalidResourceName(String resourceName, String description) {
        super(String.format("Invalid resource name '%s': %s", resourceName, description));
    }
}
