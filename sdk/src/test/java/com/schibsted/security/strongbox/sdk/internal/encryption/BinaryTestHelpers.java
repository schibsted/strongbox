/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
