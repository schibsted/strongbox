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

package com.schibsted.security.strongbox.sdk.types;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author stiankri
 */
public class SecretValueTest {

    @Test
    public void testEquals() {
        byte[] value1 = {1, 2, 3, 4};
        byte[] value2 = {1, 2, 3, 4};
        byte[] value3 = {1, 2, 3, 5};

        SecretValue secretValue1 = new SecretValue(value1, SecretType.OPAQUE);
        SecretValue secretValue2 = new SecretValue(value2, SecretType.OPAQUE);
        SecretValue secretValue3 = new SecretValue(value3, SecretType.OPAQUE);

        assertTrue(secretValue1.equals(secretValue2));
        assertFalse(secretValue1.equals(secretValue3));
    }
}
