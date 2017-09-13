/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Schibsted Products & Technology AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
