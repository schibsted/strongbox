/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal;

import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;

import java.util.Optional;

/**
 * @author stiankri
 */
public class InputValidation {
    public static final int COMMENT_BYTE_LIMIT = 1000;
    public static final String COMMENT_FIELD_NAME = "comment";

    private static Optional<String> verifySizeAndReturn(Optional<String> string, int maxLength, String name) {
        if (string.isPresent() && Encoder.asUTF8(string.get()).length > maxLength) {
            throw new IllegalArgumentException(String.format("%s must be less than %d bytes", name, maxLength));
        }
        return string;
    }

    public static Optional<String> verifyComment(Optional<String> comment) {
        return verifySizeAndReturn(comment, COMMENT_BYTE_LIMIT, COMMENT_FIELD_NAME);
    }
}
