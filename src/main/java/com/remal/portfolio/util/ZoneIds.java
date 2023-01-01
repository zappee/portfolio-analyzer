package com.remal.portfolio.util;

import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.util.Objects;

/**
 * Tool that works with ZoneId objects.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class ZoneIds {

    /**
     * Check whether the provided time zone string is valid or not.
     * If the provided value is invalid then it logs the reason and
     * aborts the program.
     *
     * @param timezone timezone to check
     */
    public static void validate(String timezone) {
        try {
            if (Objects.nonNull(timezone)) {
                var zone = ZoneId.of(timezone);
                log.debug("zone id is valid: \"{}\"", zone);
            }
        } catch (Exception e) {
            Logger.logErrorAndExit("Invalid time zone: '{}'.", timezone);
        }
    }

    /**
     * Returns with the default system zone-if if the provided one is null or
     * empty (empty string or string with only whitespaces).
     *
     * @param zoneId the zone-id to check
     * @return returns with thr system default zone if the provided is empty
     */
    public static String getDefaultIfEmpty(String zoneId) {
        return Objects.isNull(zoneId) || zoneId.isBlank()
                ? ZoneId.systemDefault().getId()
                : zoneId;
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private ZoneIds() {
        throw new UnsupportedOperationException();
    }
}
