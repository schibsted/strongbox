/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk;

import com.schibsted.security.strongbox.sdk.internal.converter.FormattedTimestamp;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author stiankri
 */
public class FormattedTimestampTest {
    @Test
    public void parse_date() {
        ZonedDateTime date = FormattedTimestamp.fromDate("2016-06-24");
        assertThat(date, is(ZonedDateTime.of(2016,6,24,0,0,0,0, ZoneId.of("UTC"))));
    }

    @Test
    public void human_readable() {
        ZonedDateTime time = FormattedTimestamp.from("2016-06-21T15:22:24.7Z[UTC]");
        assertThat(FormattedTimestamp.toHumanReadable(time), is("Tue Jun 21 2016 15:22:24 UTC"));
    }
}
