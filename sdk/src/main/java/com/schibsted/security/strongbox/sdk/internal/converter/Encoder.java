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

package com.schibsted.security.strongbox.sdk.internal.converter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.amazonaws.util.Base64;

/**
 * @author stiankri
 */
public class Encoder {
    public static Charset charset = StandardCharsets.UTF_8;

    public static byte[] asUTF8(String s) {
        return s.getBytes(charset);
    }

    public static String fromUTF8(byte[] v) {
        return new String(v, 0, v.length, charset);
    }

    public static String base64encode(byte[] value) {
        return Base64.encodeAsString(value);
    }

    public static byte[] base64decode(String value) {
        return Base64.decode(value);
    }

    public static String binaryUTF8Base64Encode(String value) {
        return base64encode(asUTF8(value));
    }

    public static String sha1(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            messageDigest.update(bytes);
            return base64encode(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to compute sha1 of encryption payload", e);
        }
    }
}
