package com.remal.portfolio.util;

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
public class LocaleDateTimes {

    /**
     * It makes a timezone transformation and then converts the date-time to string.
     *
     * @param dateTime date/time with no timezone information
     * @param zoneId time zone that is used to show dates and times in reports
     * @param dateTimePattern the timestamp pattern for GDAX export CSV file
     * @return the timestamp as a string
     */
    public static String toString(ZoneId zoneId, String dateTimePattern, LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        } else {
            var zonedUtc = dateTime.atZone(ZoneId.of("UTC")); // setting UTC as the timezone
            var zoned = zonedUtc.withZoneSameInstant(zoneId); // converting to the selected zone
            var formatter = DateTimeFormatter.ofPattern(dateTimePattern);
            return zoned.format(formatter);
        }
    }

    /**
     * Utility classes should not have public constructors.
     */
    private LocaleDateTimes() {
    }
}
