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

package com.schibsted.security.strongbox.cli.viewmodel.types;

import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;
import com.schibsted.security.strongbox.sdk.types.Encoding;
import com.schibsted.security.strongbox.sdk.types.SecretValue;

import java.util.Arrays;

/**
 * @author stiankri
 */
public class BinaryString {
    private final byte[] value;
    public final Encoding encoding;

    public BinaryString(byte[] value) {
        this.value = value;
        this.encoding = Encoding.BINARY;
    }

    public BinaryString(String value) {
        this.value = Encoder.asUTF8(value);
        this.encoding = Encoding.UTF8;
    }

    public static BinaryString makeCopy(byte[] value) {
        return new BinaryString(Arrays.copyOf(value, value.length));
    }

    public static BinaryString from(SecretValue secretValue) {
        return secretValue.encoding == Encoding.UTF8
                ? new BinaryString(secretValue.asString())
                : makeCopy(secretValue.asByteArray());
    }


    public byte[] asByteArray() {
        return this.value;
    }

    public String asString() {
        return Encoder.fromUTF8(value);
    }

    public String getStringOrBase64Encode() {
        return encoding == Encoding.UTF8
                ? Encoder.fromUTF8(this.value)
                : Encoder.base64encode(this.value);
    }
}
