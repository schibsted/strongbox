/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.exceptions;

/**
 * @author kvlees
 */
public class UnexpectedStateException extends RuntimeException {

    private static String constructMessage(String resource, String expectedState, String currentState,
                                           String description) {
        return String.format("%s. Resource: %s, Expected State: %s, Current State: %s", description,
                             expectedState, currentState, resource);
    }

    public UnexpectedStateException(String resource, String currentState, String expectedState, String description) {
        super(constructMessage(resource, currentState, expectedState, description));
    }

    public UnexpectedStateException(String resource, String expectedState, String currentState,
                                    String description, Throwable cause) {
        super(constructMessage(resource, currentState, expectedState, description), cause);
    }
}
