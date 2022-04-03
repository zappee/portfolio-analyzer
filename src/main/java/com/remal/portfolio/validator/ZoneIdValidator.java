package com.remal.portfolio.validator;

import com.remal.portfolio.util.Logger;

import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

/**
 * Time zone validator.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class ZoneIdValidator {

    /**
     * Check whether the provided time zone string is valid or not.
     *
     * @param timezone true if the timezone is valid
     */
    public static void validate(String timezone) {
        var valid = Objects.isNull(timezone) || Set.of(TimeZone.getAvailableIDs()).contains(timezone);
        if (!valid) {
            Logger.logErrorAndExit("Invalid time zone.");
        }
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private ZoneIdValidator() {
        throw new UnsupportedOperationException();
    }
}
