/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.exceptions;

import com.schibsted.security.strongbox.sdk.types.SecretsGroupIdentifier;

/**
 * @author kvlees
 * @author stiankri
 */
public class SecretsGroupException extends RuntimeException{

    private static String constructMessage(SecretsGroupIdentifier group, String description) {
        return String.format("Error in secret group '%s' [%s]: %s", group.name, group.region.getName(), description);
    }

    public SecretsGroupException(SecretsGroupIdentifier group, String description) {
        super(constructMessage(group, description));
    }

    public SecretsGroupException(SecretsGroupIdentifier group, String description, Throwable cause) {
        super(constructMessage(group, description), cause);
    }
}
