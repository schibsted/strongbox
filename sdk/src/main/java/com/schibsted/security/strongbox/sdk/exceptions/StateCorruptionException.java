package com.schibsted.security.strongbox.sdk.exceptions;

/**
 * @author stiankri
 */
public class StateCorruptionException extends RuntimeException {

    public StateCorruptionException(String message) {
        super(message);
    }

    public StateCorruptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
