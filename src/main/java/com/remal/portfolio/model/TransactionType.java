package com.remal.portfolio.model;

import lombok.extern.slf4j.Slf4j;

/**
 * Type of transaction.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public enum TransactionType {

    /**
     * Buy trade.
     */
    BUY,

    /**
     *Sell trade.
     */
    SELL,

    /**
     * Money deposit.
     */
    DEPOSIT,

    /**
     * Money withdrawal.
     */
    WITHDRAWAL,

    /**
     * Fees, like monthly account fee, money holding fee, etc.
     */
    FEE,

    /**
     * Distribution of corporate profits to eligible shareholders.
     */
    DIVIDEND,

    /**
     * Transferring assets between brokerage companies.
     */
    TRANSFER_IN,

    /**
     * Transferring assets between brokerage companies.
     */
    TRANSFER_OUT,

    /**
     * Used when the type is undefined or unknown.
     */
    UNKNOWN;

    /**
     * A null safe valueOf method.
     *
     * @param value the String value of the enum
     * @return the enum value or null if the given input is not parsable
     */
    public static TransactionType getEnum(String value) {
        try {
            var correctedValue = value.equals("withdraw") ? "withdrawal" : value;
            return TransactionType.valueOf(correctedValue.toUpperCase());
        } catch (NullPointerException e) {
            return TransactionType.UNKNOWN;
        } catch (IllegalArgumentException e) {
            log.error("Unknown transaction-type: \"{}\"", value);
            throw e;
        }
    }
}
