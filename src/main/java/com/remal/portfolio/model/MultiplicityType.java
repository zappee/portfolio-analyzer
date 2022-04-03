package com.remal.portfolio.model;

/**
 * Controls the export of the market price to the history file.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public enum MultiplicityType {

    /**
     * Allowed one record per day in the file.
     */
    SINGLE_PER_DAY,

    /**
     * Multiply records are allowed to present per day in the file.
     */
    MULTIPLY;

    /**
     * Default value while using the command line interface.
     */
    public static final String DEFAULT = "SINGLE_PER_DAY";
}
