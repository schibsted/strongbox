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

package com.schibsted.security.strongbox.sdk.internal.encryption;

import com.schibsted.security.strongbox.sdk.internal.converter.Encoder;

import java.nio.ByteBuffer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

/**
 * @author stiankri
 */
public class BinaryTestHelpers {

    public static int moduloPadding(int val, int mod) {
        return mod - (val % mod);
    }

    public static void assertByte(String reason, byte target, ByteBuffer byteBuffer) {
        byte serializedByte = byteBuffer.get();
        assertThat(reason, serializedByte, is(target));
    }

    public static void assertLong(String reason, long target, ByteBuffer byteBuffer) {
        long serializedLong = byteBuffer.getLong();
        assertThat(reason, serializedLong, is(target));
    }

    public static void assertInt(String reason, int target, ByteBuffer byteBuffer) {
        int serializedInt = byteBuffer.getInt();
        assertThat(reason, serializedInt, is(target));
    }

    public static int assertIntInRange(String reason, int minTarget, int maxTarget, ByteBuffer byteBuffer) {
        int serializedInt = byteBuffer.getInt();

        assertThat(reason, serializedInt, greaterThanOrEqualTo(minTarget));
        assertThat(reason, serializedInt, lessThanOrEqualTo(maxTarget));
        return serializedInt;
    }

    public static void assertString(String reason, String target, ByteBuffer byteBuffer) {
        byte[] targetBuffer = Encoder.asUTF8(target);
        byte[] buffer = new byte[targetBuffer.length];
        byteBuffer.get(buffer, 0, targetBuffer.length);
        assertThat(reason, Encoder.fromUTF8(buffer), is(target));
    }

    public static void assertByteArray(String reason, byte[] target, ByteBuffer byteBuffer) {
        byte[] buffer = new byte[target.length];
        byteBuffer.get(buffer, 0, target.length);
        assertThat(reason, buffer, is(target));
    }
}
