package com.remal.portfolio.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Tool that works with LocaleDateTime objects.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class LocalDateTimes {

    /**
     * Make a timezone transformation from UTC to client time zone and
     * converts the date/time to a String.
     *
     * @param zoneTo the timezone to convert the date to, e.g. "GMT+2", "Europe/Budapest"
     * @param dateTimePattern the date/time pattern
     * @param timestamp the date/time to convert
     * @return the date/time as a string at the provided timezone
     */
    public static String toString(String zoneTo, String dateTimePattern, LocalDateTime timestamp) {
        if (Objects.nonNull(timestamp)) {
            try {
                var timestampAtLocalZone = timestamp.atZone(ZoneId.systemDefault());
                var formatter = DateTimeFormatter.ofPattern(dateTimePattern);
                return Objects.isNull(zoneTo)
                        ? timestampAtLocalZone.format(formatter)
                        : timestampAtLocalZone.withZoneSameInstant(ZoneId.of(zoneTo)).format(formatter);

            } catch (IllegalArgumentException e) {
                Logger.logErrorAndExit(
                        "An error has occurred while converting the date/time. Pattern '{}', Error: {}",
                        dateTimePattern,
                        e.getMessage());
            }
        }
        return "";
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private LocalDateTimes() {
        throw new UnsupportedOperationException();
    }
}
