package com.remal.portfolio.model;

/**
 * Type of transaction.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public enum TransactionType {

    /**
     * Buy trade.
     */
    BUY,

    /**
     * Money deposit.
     */
    DEPOSIT,

    /**
     * Fee of the transaction.
     */
    FEE,

    /**
     *Sell trade.
     */
    SELL,

    /**
     * Money withdrawal.
     */
    WITHDRAWAL;

    /**
     * A null safe valueOf method.
     *
     * @param value the String value of the enum
     * @return the enum value or null if the given input is not parsable
     */
    public static TransactionType getEnum(String value) {
        try {
            return TransactionType.valueOf(value.toUpperCase());
        } catch (NullPointerException e) {
            return null;
        }
    }
}
