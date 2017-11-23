/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.exceptions;

/**
 * @author kvlees
 */
public class NoFieldMatchingAnnotationException extends RuntimeException {

    public NoFieldMatchingAnnotationException(String annotation, String className) {
        super(String.format("No field with annotation '%s' in class '%s'", annotation, className));
    }
}
