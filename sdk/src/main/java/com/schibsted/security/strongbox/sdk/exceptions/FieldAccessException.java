/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.exceptions;

/**
 * @author kvlees
 */
public class FieldAccessException extends RuntimeException {
    private static String constructMessage(String fieldName, String className) {
        return String.format("Failed to access field '%s' on object of class '%s'", fieldName, className);
    }

    public FieldAccessException(String fieldName, String className) {
        super(constructMessage(fieldName, className));
    }

    public FieldAccessException(String fieldName, String className, Throwable cause) {
        super(constructMessage(fieldName, className), cause);
    }
}
