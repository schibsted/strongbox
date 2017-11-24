/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import java.util.Arrays;

/**
 * @author stiankri
 */
public class BestEffortShredder {
    public static void shred(byte [] bytes) {
        Arrays.fill( bytes, (byte)0 );
    }

    public static void shred(char [] bytes) {
        Arrays.fill( bytes, '\u0000' );
    }
}
