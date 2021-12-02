package com.remal.portfolio.model;

/**
 * The currency code (ISO 4217) that is used in the transactions.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public enum Currency {

    /**
     * Currency code for Euro.
     */
    EUR,

    /**
     * Currency code for United States Dollar.
     */
    USD,

    /**
     * It is used to indicate that the provided value is not a
     * valid currency.
     */
    UNKNOWN_CURRENCY;

    /**
     * A null safe valueOf method.
     *
     * @param value the String value of the enum
     * @return the enum value or null if the given input is not parsable
     */
    public static Currency getEnum(String value) {
        try {
            return Currency.valueOf(value.toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            return Currency.UNKNOWN_CURRENCY;
        }
    }

}
