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
