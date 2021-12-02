package com.remal.portfolio.util;

import java.math.BigDecimal;
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
     * @param decimalFormat decimal format, e.g. '%14.8f'
     * @param number the number to format
     * @return the number as a string
     */
    public static String toString(String decimalFormat, BigDecimal number) {
        if (Objects.isNull(number)) {
            return "";
        } else {
            return String.format(decimalFormat, number).replaceFirst("\\.?0*$", "");
        }
    }

    /**
     * Utility classes should not have public constructors.
     */
    private BigDecimals() {
        // do nothing
    }
}
