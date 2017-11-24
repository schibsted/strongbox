/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
