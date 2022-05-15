package com.remal.portfolio.model;

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
     * Allowed only one price record in the history file per hour.
     */
    ONE_PRICE_PER_HOUR,

    /**
     * Allowed only one price record in the history file per day.
     */
    ONE_PRICE_PER_DAY,

    /**
     * Unlimited price record are allowed in the history file.
     */
    MANY
}
