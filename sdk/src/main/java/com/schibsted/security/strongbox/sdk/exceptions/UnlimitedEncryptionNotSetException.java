/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.exceptions;

/**
 * @author stiankri
 */
public class UnlimitedEncryptionNotSetException extends RuntimeException {

    public UnlimitedEncryptionNotSetException() {
        super("Caught an Illegal key size exception. "
                + "This probably means that you need to install the Unlimited Strength Profile for the "
                + "Java Cryptography Extension, which can be found at https://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html");
    }
}
