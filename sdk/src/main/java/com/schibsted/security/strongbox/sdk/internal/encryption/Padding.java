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
