/*
 * Copyright 2016 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.exceptions;

/**
 * @author kvlees
 */
public class PotentiallyMaliciousDataException extends RuntimeException {
    public PotentiallyMaliciousDataException(String message) {
        super("Data could be malicious and should not be trusted: " + message);
    }
}
