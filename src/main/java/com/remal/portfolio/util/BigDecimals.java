package com.remal.portfolio.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Objects;

/**
 * Tool that converts and formats BigDecimal objects.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class BigDecimals {

    /**
     * Converts decimal number to String based on the provided format.
     *
     * @param decimalFormat decimal format, e.g. '###,###.###'
     * @param decimalGroupingSeparator the character used for thousands separator
     * @param number the number to format
     * @return the number as a string
     */
    public static String toString(String decimalFormat, char decimalGroupingSeparator, BigDecimal number) {
        if (Objects.isNull(number)) {
            return "";
        } else {
            DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
            decimalFormatSymbols.setGroupingSeparator(decimalGroupingSeparator);

            DecimalFormat formatter = new DecimalFormat(decimalFormat, decimalFormatSymbols);
            return formatter.format(number);
        }
    }

    /**
     * Converts string to BigDecimal.
     * This method can be user to convert formatted decimals to BigDecimal like "123 456.78".
     *
     * @param s the string that represents the decimal value
     * @return the BigDecimal
     */
    public static BigDecimal valueOf(String s) {
        if (Objects.isNull(s)) {
            return null;
        } else {
            // remove spaces
            return new BigDecimal(s.replaceAll("\\s+",""));
        }
    }

    /**
     * Greater than or equal zero.
     *
     * @param decimal decimal value to check
     * @return true if the decimal value is greater than or equal zero
     */
    public static boolean isNonNegative(BigDecimal decimal) {
        return (decimal.compareTo(BigDecimal.ZERO) >= 0);
    }

    /**
     * Is null or zero.
     *
     * @param decimal decimal value to check
     * @return true if the decimal is null or equals with zero
     */
    public static boolean isNullOrZero(BigDecimal decimal) {
        return Objects.isNull(decimal) || (decimal.compareTo(BigDecimal.ZERO) == 0);
    }

    /**
     * Is not zero.
     *
     * @param decimal decimal value to check
     * @return true if the decimal is not equals with zero
     */
    public static boolean isNotZero(BigDecimal decimal) {
        return ! isNullOrZero(decimal);
    }

    /**
     * Not null and not zero.
     *
     * @param decimal decimal value to check
     * @return true if the decimal is not null and not equals with zero
     */
    public static boolean isNotNullAndNotZero(BigDecimal decimal) {
        return Objects.nonNull(decimal) && (decimal.compareTo(BigDecimal.ZERO) != 0);
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private BigDecimals() {
        throw new UnsupportedOperationException();
    }
}
