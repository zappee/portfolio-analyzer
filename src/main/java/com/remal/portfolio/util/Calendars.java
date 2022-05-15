package com.remal.portfolio.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

/**
 * Tool that works with java.util.Calendar objects.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class Calendars {

    /**
     * Convert a String to java.util.Calendar.
     *
     * @param timestampAsString the timestamp as a string
     * @param dateTimePattern pattern to parse the timestamp
     * @return the calendar that represents the timestamp
     */
    public static Calendar fromString(String timestampAsString, String dateTimePattern) {
        Calendar calendar = null;
        try {
            calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimePattern);
            calendar.setTime(simpleDateFormat.parse(timestampAsString));
        } catch (ParseException e) {
            Logger.logErrorAndExit(
                    "An error has occurred while converting '{}' to calendar using the '{}' as a pattern, error: {}",
                    timestampAsString,
                    dateTimePattern,
                    e.getMessage());
        }

        return calendar;
    }

    /**
     * Convert java.util.Calendar to String.
     *
     * @param calendar calendar instance
     * @return the string that represents the provided calendar
     */
    public static String toString(Calendar calendar) {
        if (Objects.isNull(calendar)) {
            return "";
        } else {
            return calendar.toInstant().toString();
        }
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    private Calendars() {
        throw new UnsupportedOperationException();
    }
}
