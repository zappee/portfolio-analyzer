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

    AED, ALL, ARS, AUD, BAM, BGN, BHD, BOB, BRL, BYR,
    CAD, CHF, CLP, CNY, COP, CRC, CSD, CZK, DKK, DOP,
    DZD, EEK, EGP, EUR, GBP, GTQ, HKD, HNL, HRK, HUF,
    IDR, ILS, INR, IQD, ISK, JOD, JPY, KRW, KWD, LBP,
    LTL, LVL, LYD, MAD, MKD, MXN, MYR, NIO, NOK, NZD,
    OMR, PAB, PEN, PHP, PLN, PYG, QAR, RON, RSD, RUB,
    SAR, SDG, SEK, SGD, SKK, SVC, SYP, THB, TND, TRY,
    TWD, UAH, USD, UYU, VEF, VND, YER, ZAR, UNKNOWN;

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
            return Currency.UNKNOWN;
        }
    }
}
