/*
 * Copyright 2016 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.exceptions;

/**
 * @author kvlees
 */
public class SerializationException extends RuntimeException {
    public SerializationException(String message) { super(message); }
    public SerializationException(String message, Throwable cause) { super(message, cause); }
}
