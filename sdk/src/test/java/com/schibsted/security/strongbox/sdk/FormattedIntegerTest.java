/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
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
