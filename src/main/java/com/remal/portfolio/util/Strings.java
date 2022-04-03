package com.remal.portfolio.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Tool that works with String objects.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class Strings {

    /**
     * Left pad a String with spaces (' ').
     *
     * @param str the String to pad out, may be null
     * @param size the size to pad to
     * @return left padded String or original String if no padding is necessary, null if null String input
     */
    public static String leftPad(String str, int size) {
        if (str == null) {
            return " ".repeat(size);
        }

        var pattern = "%-" + size + "s";
        return String.format(pattern, str);
    }

    /**
     * Right pad a String with a specified String.
     *
     * @param str the String to pad out, may be null
     * @param size the size to pad to
     * @return right padded String or original String if no padding is necessary
     */
    public static String rightPad(final String str, final int size) {
        if (str == null) {
            return " ".repeat(size);
        }

        var pattern = "%" + size + "s";
        return String.format(pattern, str);
    }

    /**
     * Convert ISO formatted timestamp string to a LocalDateTime.
     * ISO pattern: 2021-12-31T22.00.00.000[00]
     *
     * @param timeAsString the timestamp as a string
     * @return LocalDateTime the LocalDateTime object
     */
    public static LocalDateTime toLocalDateTime(String timeAsString) {
        var formatter = DateTimeFormatter.ISO_INSTANT;
        var dateInstant = Instant.from(formatter.parse(timeAsString));
        return LocalDateTime.ofInstant(dateInstant, ZoneId.of(ZoneOffset.UTC.getId()));
    }

    /**
     * Convert string to a LocalDateTime.
     *
     * @param dateTimePattern the date/time pattern used for parsing the string
     * @param timestampAsString timestamp as a string
     * @return LocalDateTime the LocalDateTime object
     */
    public static LocalDateTime toLocalDateTime(String dateTimePattern, String timestampAsString) {
        LocalDateTime timestamp = null;
        try {
            var formatter = DateTimeFormatter.ofPattern(dateTimePattern);
            timestamp = LocalDateTime.parse(timestampAsString, formatter);
        } catch (DateTimeParseException e) {
            Logger.logErrorAndExit(
                    "Error while parsing the '{}' string to datetime, pattern: '{}'.",
                    timestampAsString,
                    dateTimePattern);
        }
        return timestamp;
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private Strings() {
        throw new UnsupportedOperationException();
    }
}
