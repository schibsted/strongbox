/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
