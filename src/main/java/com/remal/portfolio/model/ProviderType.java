package com.remal.portfolio.model;

/**
 * Accepted data providers for getting the price of a stock.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public enum ProviderType {

    /**
     * Yahoo Finance API.
     */
    YAHOO,

    /**
     * Coinbase PRO API.
     */
    COINBASE_PRO;

    /**
     * A null safe valueOf method.
     *
     * @param value the String value of the enum
     * @return the enum value or null if the given input is not parsable
     */
    public static ProviderType getEnum(String value) {
        try {
            return ProviderType.valueOf(value.toUpperCase());
        } catch (NullPointerException e) {
            return null;
        }
    }
}
