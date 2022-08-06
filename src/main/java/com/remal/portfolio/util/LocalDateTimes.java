package com.remal.portfolio.util;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Objects;

/**
 * Tool that works with LocaleDateTime objects.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class LocalDateTimes {

    /**
     * Convert ISO formatted timestamp string to a LocalDateTime.
     * ISO pattern: 2022-12-31T22.00.00.000[00]
     *
     * @param datetimeAsString the timestamp as a string
     * @return LocalDateTime the LocalDateTime object
     */
    public static LocalDateTime toLocalDateTime(String datetimeAsString) {
        var formatter = DateTimeFormatter.ISO_INSTANT;
        var dateInstant = Instant.from(formatter.parse(datetimeAsString));
        return LocalDateTime.ofInstant(dateInstant, ZoneId.of(ZoneOffset.UTC.getId()));
    }

    /**
     * Convert string to a LocalDateTime.
     *
     * @param dateTimeAsString timestamp as a string
     * @param dateTimePattern the date/time pattern used for parsing the string
     * @return LocalDateTime the LocalDateTime object
     */
    public static LocalDateTime toLocalDateTime(String dateTimePattern, String dateTimeAsString) {
        LocalDateTime timestamp = null;
        try {
            var formatter = DateTimeFormatter.ofPattern(dateTimePattern);
            if (Objects.nonNull(dateTimeAsString)) {
                timestamp = LocalDateTime.parse(dateTimeAsString, formatter);
            }
        } catch (DateTimeParseException e) {
            Logger.logErrorAndExit(
                    "Error while parsing the \"{}\" string to datetime, pattern: \"{}\".",
                    dateTimeAsString,
                    dateTimePattern);
        }
        return timestamp;
    }

    /**
     * Convert string to a LocalDateTime.
     *
     * @param zone the timezone to convert the date to
     * @param dateTimeAsString timestamp as a string
     * @param dateTimePattern the date/time pattern used for parsing the string
     * @return LocalDateTime the LocalDateTime object
     */
    public static LocalDateTime toLocalDateTime(ZoneId zone, String dateTimePattern, String dateTimeAsString) {
        if (Objects.isNull(dateTimeAsString)) {
            return null;
        } else {
            var dateTime = toLocalDateTime(dateTimePattern, dateTimeAsString);
            return Objects.isNull(dateTime) ? null : dateTime.atZone(zone).toLocalDateTime();
        }
    }

    /**
     * Make a timezone transformation from UTC to client time zone and
     * converts the date/time to a String.
     *
     * @param zone the timezone to convert the date to
     * @param dateTimePattern the date/time pattern
     * @param timestamp the date/time to convert
     * @return the date/time as a string at the provided timezone
     */
    public static String toString(ZoneId zone, String dateTimePattern, LocalDateTime timestamp) {
        if (Objects.nonNull(timestamp) && Objects.nonNull(dateTimePattern)) {
            try {
                var timestampAtLocalZone = timestamp.atZone(ZoneId.systemDefault());
                var formatter = DateTimeFormatter.ofPattern(dateTimePattern);
                return Objects.isNull(zone)
                        ? timestampAtLocalZone.format(formatter)
                        : timestampAtLocalZone.withZoneSameInstant(zone).format(formatter);

            } catch (IllegalArgumentException e) {
                Logger.logErrorAndExit(
                        "An error has occurred while converting date to string. "
                        + "Wrap the filename with single quotas may help. "
                        + "String: \"{}\", Error: {}",
                        dateTimePattern,
                        e.getMessage());
            }
        }
        return "";
    }

    /**
     * Check whether the provided string is valid date or not.
     * If the provided value is invalid then it logs the reason and
     * aborts the program.
     *
     * @param datePattern the pattern to validate the string
     * @param dateTime the datetime String to validate
     */
    public static void validate(String datePattern, String dateTime) {
        try {
            if (Objects.nonNull(dateTime)) {
                DateFormat dateFormat = new SimpleDateFormat(datePattern);
                dateFormat.setLenient(false);
                dateFormat.parse(dateTime);
            }
        } catch (ParseException e) {
            Logger.logErrorAndExit("Invalid date-time string.");
        }
    }

    /**
     * The "to" date-time must be adjusted a little, otherwise
     * filter will hide wrong records.
     * <p>
     * Example:
     *    to = 2022-04-11 21:11:19 --> 2022-04-11 21:11:19.000
     *    transaction.getTradeDate() = 2022-04-11 21:11:19.345
     *
     * Then transaction.getTradeDate().isBefore(to) will hide the
     * transaction above so the last record wil not appear in the
     * report:
     *
     * |portfolio|symbol |type   |trade date          |quantity|price|fee  |currency|
     * |default  |EUR    |DEPOSIT|2022-03-29 22:33:08|15       |   1 |0    |EUR     |
     * |default  |ETH-EUR|BUY    |2022-04-11 21:11:19| 0.35    |2740 |3.836|EUR     |
     *
     * To avoid this situation the value of the "to" must be
     * increased with 999 millisecond this way:
     *    2022-04-11 21:11:19 --> 2022-04-11 21:11:19.999
     * </p>
     * @param dateTimePattern the pattern used to parse the timestamp
     * @param dateTimeAsString the timestamp as a string coming from the command line interface
     * @return the LocalDateTime filter that can be used in the lambda filter as a "to"
     */
    public static LocalDateTime getFilterTo(String dateTimePattern, String dateTimeAsString) {
        var timestamp = toLocalDateTime(dateTimePattern, dateTimeAsString);
        if (Objects.nonNull(timestamp)) {
            var millisecondPart = timestamp.getLong(ChronoField.MILLI_OF_SECOND);
            if (millisecondPart == 0) {
                var millisecond = 999;
                timestamp = timestamp.plus(millisecond, ChronoField.MILLI_OF_SECOND.getBaseUnit());
            }
        }
        return timestamp;
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
