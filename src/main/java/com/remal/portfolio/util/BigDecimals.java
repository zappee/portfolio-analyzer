package com.remal.portfolio.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Objects;

/**
 * Tool that works with BigDecimal objects.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class BigDecimals {

    /**
     * Scale to round the numbers in the report.
     */
    public static final int SCALE_DEFAULT = 2;

    /**
     * Scale to round the numbers in the report.
     */
    public static final int SCALE_FOR_CURRENCY = 6;

    /**
     * Rounding mode used to show decimal numbers in the report.
     */
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /**
     * Convert decimal number to a formatted String.
     *
     * @param decimalFormat decimal format, e.g. '###,###.###'
     * @param decimalGroupingSeparator the character used for thousands separator
     * @param number the number to format
     * @return the number as a formatted string
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
     * Convert a String to BigDecimal. This method can be used to convert
     * formatted decimals to BigDecimal, e.g. "123 456.78".
     *
     * @param s the decimal value as a string
     * @return the BigDecimal object
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
     * Convert a BigDecimal to double.
     *
     * @param decimal the BigDecimal to convert
     * @return the double value
     */
    public static double valueOf(BigDecimal decimal) {
        Objects.requireNonNull(decimal, "The decimal value can not be null.");
        return decimal.doubleValue();
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
     * Returns a default value if the object passed is {@code null}.
     *
     * @param object  the value to test, may be {@code null}
     * @return {@code object} if it is not {@code null}, zero otherwise
     */
    public static BigDecimal nullToZero(final BigDecimal object) {
        return object != null ? object : BigDecimal.ZERO;
    }

    /**
     * Compute of the percent of the given two decimals.
     *
     * @param d1 first decimal value
     * @param d2 second decimal value
     * @param scale scale of the result
     * @return the result in percent
     */
    public static BigDecimal percentOf(BigDecimal d1, BigDecimal d2, int scale) {
        var hundred = new BigDecimal(100);
        return BigDecimals.isNullOrZero(d1)
                ? null
                : hundred.multiply(d1).divide(d2, scale, RoundingMode.CEILING).subtract(hundred);
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
