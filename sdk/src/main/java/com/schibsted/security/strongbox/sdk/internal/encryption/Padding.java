/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.encryption;

import com.schibsted.security.strongbox.sdk.exceptions.FormattingException;
import com.schibsted.security.strongbox.sdk.internal.converter.FormattedTimestamp;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author stiankri
 */
public class Padding {

    public static String padWithSpaces(String value, int totalLength) {
        if (totalLength < 0) {
            throw new IllegalArgumentException(String.format("The totalLength must be non-negative, but was %d", totalLength));
        }
        if (value.length() > totalLength) {
            throw new IllegalArgumentException(String.format("The length, %d must be smaller that the padding target %d", value.length(), totalLength));
        }

        int paddingLength = totalLength - value.length();

        char[] padding = new char[paddingLength];
        Arrays.fill(padding, ' ');

        return value + new String(padding);
    }

    public static String removeSpacePadding(String paddedValue) {
        return paddedValue.replaceAll(" *$", "");
    }

    public static String padWithZeros(Long number) {
        String base = Long.toUnsignedString(number);
        String padding = "00000000000000000000";
        if (base.length() > padding.length()) {
            throw new FormattingException(String.format("The number %d is too large to fit a padding of %s",
                    number, padding.length()));
        }
        return padding.substring(0, padding.length() - base.length()) + base;
    }

    public static String asOptionalString(Optional<ZonedDateTime> date) {
        if (date.isPresent()) {
            return padWithZeros(FormattedTimestamp.epoch(date.get()));
        } else {
            return padWithZeros(0L);
        }
    }

    public static String isPresent(Optional<ZonedDateTime> date) {
        return date.isPresent() ? "1" : "0";
    }

    public static String singleDigit(byte value) {
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be a single digit in the range 0 to 9, but was " + value);
        }
        return String.valueOf(value);
    }
}
