package com.remal.portfolio.model;

import lombok.Getter;

/**
 * Controls the export of the number of the market prices for a ticker
 * in the history file.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public enum MultiplicityType {

    /**
     * Allowed only one price record in the history file within 1 minutes.
     */
    ONE_MINUTES(60L),

    /**
     * Allowed only one price record in the history file within 5 minutes.
     */
    FIVE_MINUTES(60L * 5),

    /**
     * Allowed only one price record in the history file within 15 minutes.
     */
    FIFTEEN_MINUTES(60L * 15),

    /**
     * Allowed only one price record in the history file within 30 minutes.
     */
    THIRTY_MINUTES(60L * 30),

    /**
     * Allowed only one price record in the history file per hour.
     */
    ONE_HOUR(60L * 60),

    /**
     * Allowed only one price record in the history file per hour.
     */
    FOUR_HOURS(60L * 60 * 4),

    /**
     * Allowed only one price record in the history file per day.
     */
    ONE_DAY(60L * 60 * 24),

    /**
     * Unlimited price record are allowed in the history file.
     */
    MANY(0);

    /**
     * The length of the range in seconds.
     */
    @Getter
    private final long rangeLengthInSec;

    /**
     * Constructor.
     *
     * @param rangeLengthInSec length of the range in seconds
     */
    MultiplicityType(long rangeLengthInSec) {
        this.rangeLengthInSec = rangeLengthInSec;
    }
}
