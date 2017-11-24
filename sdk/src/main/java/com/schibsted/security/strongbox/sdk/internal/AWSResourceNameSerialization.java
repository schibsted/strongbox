/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal;

/**
 * @author stiankri
 */
public class AWSResourceNameSerialization {
    public static final String GLOBAL_PREFIX = "strongbox";
    public static final String GLOBAL_STRING_DELIMITER = "_";

    // This is due to the limited character set of KMS
    public static String decodeSecretsGroupName(String encoded) {
        return encoded.replace('-', '.');
    }

    public static String encodeSecretsGroupName(String name) {
        return name.replace('.', '-');
    }
}
