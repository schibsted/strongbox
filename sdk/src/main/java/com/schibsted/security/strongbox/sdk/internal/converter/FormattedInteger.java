/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.converter;

import com.schibsted.security.strongbox.sdk.exceptions.FormattingException;

import java.math.BigInteger;

/**
 * @author stiankri
 * @author kvlees
 */
public class FormattedInteger {

    public static String paddedInteger(BigInteger number) {
        String base = number.toString();
        String padding = "0000000000000000000";
        if (base.length() > padding.length()) {
            throw new FormattingException(String.format("The number %s is too large to fit a padding of %s",
                    number.toString(), padding.length()));
        }
        return padding.substring(0, padding.length() - base.length()) + base;
    }

    public static BigInteger fromPadded(String number) {
        return new BigInteger(number);
    }
}
