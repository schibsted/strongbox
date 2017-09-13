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

package com.schibsted.security.strongbox.sdk;

import com.schibsted.security.strongbox.sdk.internal.converter.FormattedInteger;
import org.testng.annotations.Test;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FormattedIntegerTest {
    @Test
    public void to_padded_zero() {
        String padded = FormattedInteger.paddedInteger(new BigInteger("0"));
        assertThat(padded, is("0000000000000000000"));
    }

    @Test
    public void from_padded_zero() {
        BigInteger number = FormattedInteger.fromPadded("0000000000000000000");
        assertThat(number, is(new BigInteger("0")));
    }

    @Test
    public void to_padded_one() {
        String padded = FormattedInteger.paddedInteger(new BigInteger("1"));
        assertThat(padded, is("0000000000000000001"));
    }

    @Test
    public void from_padded_one() {
        BigInteger number = FormattedInteger.fromPadded("0000000000000000001");
        assertThat(number, is(new BigInteger("1")));
    }

    @Test
    public void to_padded_max_long() {
        String padded = FormattedInteger.paddedInteger(BigInteger.valueOf(Long.MAX_VALUE));
        assertThat(padded, is("9223372036854775807"));
    }

    @Test
    public void from_padded_max_long() {
        BigInteger number = FormattedInteger.fromPadded("9223372036854775807");
        assertThat(number, is(BigInteger.valueOf(Long.MAX_VALUE)));
    }
}
