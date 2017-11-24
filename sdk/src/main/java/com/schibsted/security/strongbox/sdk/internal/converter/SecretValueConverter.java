/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.converter;

import com.schibsted.security.strongbox.sdk.internal.encryption.BestEffortShredder;
import com.schibsted.security.strongbox.sdk.types.SecretType;
import com.schibsted.security.strongbox.sdk.types.SecretValue;

import java.util.Arrays;

/**
 * @author stiankri
 */
public class SecretValueConverter {

    public static SecretValue inferEncoding(byte[] value, SecretType secretType) {
        String decoded = Encoder.fromUTF8(value);
        byte[] encoded = Encoder.asUTF8(decoded);

        boolean isUtf8 = Arrays.equals(value, encoded);
        BestEffortShredder.shred(encoded);

        if (isUtf8) {
            return new SecretValue(decoded, secretType);
        } else {
            return new SecretValue(value, secretType);
        }
    }
}
