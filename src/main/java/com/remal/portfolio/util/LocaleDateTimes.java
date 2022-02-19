package com.remal.portfolio.util;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Tool that converts and formats LocaleDateTime objects.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class LocaleDateTimes {

    /**
     * Default time zone info from the operating system.
     */
    private static final ZoneId DEFAULT_TIME_ZONE = ZoneId.systemDefault();

    /**
     * It makes a timezone transformation and then converts the date-time to string.
     *
     * @param dateTime date/time with no timezone information
     * @param dateTimePattern the timestamp pattern for GDAX export CSV file
     * @return the timestamp as a string
     */
    public static String toString(String dateTimePattern, LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        } else {
            try {
                var zonedUtc = dateTime.atZone(ZoneId.of("UTC"));
                var localZone = zonedUtc.withZoneSameInstant(DEFAULT_TIME_ZONE);
                var formatter = DateTimeFormatter.ofPattern(dateTimePattern);
                return localZone.format(formatter);
            } catch (IllegalArgumentException e) {
                log.error("An error has occurred while converting '{}' date-time pattern to string. Error: {}",
                        dateTimePattern, e.toString());
                System.exit(CommandLine.ExitCode.SOFTWARE);
                return null;
            }
        }
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private LocaleDateTimes() {
        throw new UnsupportedOperationException();
    }
}
