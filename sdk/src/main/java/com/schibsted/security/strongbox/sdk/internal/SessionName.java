/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author stiankri
 */
public class SessionName {
    public static String getSessionName(String prefix){
        try {
            SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");

            return String.format("%s-%s", prefix, rnd.nextLong());
        } catch (NoSuchAlgorithmException e) {
            return prefix;
        }
    }
}
