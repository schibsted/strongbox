/*
 * Copyright 2016 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.exceptions;

/**
 * @author kvlees
 */
public class ParseException extends RuntimeException {
    public ParseException(String message) { super(message); }
    public ParseException(String message, Throwable cause) { super(message, cause); }
}
