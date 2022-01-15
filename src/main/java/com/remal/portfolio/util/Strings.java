package com.remal.portfolio.util;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Tool that converts and formats String objects.
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

        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        return str.concat(" ".repeat(pads));
    }

    /**
     * Converts ISO formatted timestamp string to a LocalDateTime.
     * Pattern: 2021-12-31T22.00.00.000[00]
     *
     * @param timeAsString the timestamp as a string
     * @return LocalDateTime converted object
     */
    public static LocalDateTime toLocalDateTime(String timeAsString) {
        var formatter = DateTimeFormatter.ISO_INSTANT;
        var dateInstant = Instant.from(formatter.parse(timeAsString));
        return LocalDateTime.ofInstant(dateInstant, ZoneId.of(ZoneOffset.UTC.getId()));
    }

    /**
     * Converts ISO formatted timestamp string to a LocalDateTime.
     *
     * @param dateTimePattern the date-time format for parsing dates from the Excel
     * @param zoneId time zone info
     * @param timeAsString the timestamp as a string
     * @return LocalDateTime converted object
     */
    public static LocalDateTime toLocalDateTime(String dateTimePattern, ZoneId zoneId, String timeAsString) {
        try {
            var formatter = DateTimeFormatter.ofPattern(dateTimePattern).withZone(zoneId);
            var dateInstant = Instant.from(formatter.parse(timeAsString));
            return LocalDateTime.ofInstant(dateInstant, ZoneId.of(ZoneOffset.UTC.getId()));
        } catch (DateTimeParseException e) {
            log.error("Error while parsing the '{}' string to datetime, pattern: '{}'.", timeAsString, dateTimePattern);
            System.exit(CommandLine.ExitCode.SOFTWARE);
        }
        return null;
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
