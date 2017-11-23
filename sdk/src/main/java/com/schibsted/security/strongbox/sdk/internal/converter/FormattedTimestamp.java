/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.sdk.internal.converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static java.util.Locale.ENGLISH;

/**
 * Convenience methods to handle timestamp formatting
 *
 * https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
 *
 * @author stiankri
 */
public class FormattedTimestamp {
    private static final ZoneId utc = ZoneId.of("UTC");
    private static final DateTimeFormatter humanReadableFormatter = DateTimeFormatter.ofPattern("EEE MMM d u HH:mm:ss zzz", ENGLISH);
    private static final DateTimeFormatter comparableFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    private static final DateTimeFormatter localDate = DateTimeFormatter.ISO_LOCAL_DATE;


    public static String nowUTC() {
        ZonedDateTime localNow = ZonedDateTime.now();
        ZonedDateTime utcNow = localNow.withZoneSameInstant(utc);
        return utcNow.format(comparableFormatter);
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now(utc);
    }

    public static String from(ZonedDateTime timestamp) {
        return timestamp.format(comparableFormatter);
    }

    public static ZonedDateTime from(String timestamp) {
        return ZonedDateTime.parse(timestamp, comparableFormatter);
    }

    public static String toHumanReadable(ZonedDateTime timestamp) {
        return timestamp.format(humanReadableFormatter);
    }

    public static String toHumanReadable(Optional<ZonedDateTime> timestamp) {
        return timestamp.map(FormattedTimestamp::toHumanReadable).orElse("");
    }

    public static String toHumanReadable(long timestamp) {
        return toHumanReadable(fromEpoch(timestamp));
    }

    public static ZonedDateTime fromDate(String date) {
        try {
            return LocalDate.parse(date, localDate).atStartOfDay(utc);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(String.format("Unable to parse '%s' as a date, expected it to be on the form 'YYYY-MM-DD'", date));
        }
    }

    public static Long epoch(ZonedDateTime timestamp) {
        return timestamp.withZoneSameInstant(utc).toEpochSecond();
    }

    public static String epochString(ZonedDateTime timestamp) {
        return Long.toUnsignedString(timestamp.withZoneSameInstant(utc).toEpochSecond());
    }

    public static ZonedDateTime fromEpoch(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        return ZonedDateTime.ofInstant(instant, utc);
    }
}
