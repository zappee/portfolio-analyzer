package com.remal.portfolio.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
            if (Objects.nonNull(timestampAsString) && Objects.nonNull(dateTimePattern)) {
                calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimePattern);
                calendar.setTime(simpleDateFormat.parse(timestampAsString));
            }
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
    public static String toUtcString(final Calendar calendar) {
        if (Objects.isNull(calendar)) {
            return "";
        } else {
            return calendar.getTime().toString();
        }
    }

    /**
     * Convert java.util.Calendar to String.
     *
     * @param calendar calendar instance
     * @return the string that represents the provided calendar
     */
    public static String toIsoString(final Calendar calendar) {
        if (Objects.isNull(calendar)) {
            return "";
        } else {
            return calendar.toInstant().toString();
        }
    }

    /**
     * Converts Calendar to LocalDateTime using the system timezone.
     *
     * @param calendar calendar to convert
     * @return the LocalDateTime instance
     */
    public static LocalDateTime toLocalDateTime(final Calendar calendar) {
        return LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Converts a Calendar to LocalDateTime.
     *
     * @param calendar calendar to convert
     * @param zone the time zone information
     * @return the LocalDateTime object
     */
    public static LocalDateTime toLocalDateTime(Calendar calendar, ZoneId zone) {
        return LocalDateTime.ofInstant(calendar.toInstant(), zone);
    }

    /**
     * Returns a string representation of the object.
     *
     * @param calendar the calendar to convert to string
     * @return a string representation of the object
     */
    public static String toString(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(calendar.getTimeZone());
        return dateFormat.format(calendar.getTime());
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
