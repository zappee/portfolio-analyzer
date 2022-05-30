package com.remal.portfolio.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Tool that works with String objects.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class Strings {

    /**
     * Left pad a String with spaces (' ').
     *
     * @param str the String to pad out, may be null
     * @param size the size to pad to
     * @return left padded String or original String if no padding is necessary, null if null String input
     */
    public static String leftPad(String str, int size) {
        if (str == null || size == 0) {
            return " ".repeat(size);
        }

        var pattern = "%-" + size + "s";
        return String.format(pattern, str);
    }

    /**
     * Right pad a String with a specified String.
     *
     * @param str the String to pad out, may be null
     * @param size the size to pad to
     * @return right padded String or original String if no padding is necessary
     */
    public static String rightPad(final String str, final int size) {
        if (str == null) {
            return " ".repeat(size);
        }

        var pattern = "%" + size + "s";
        return String.format(pattern, str);
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private Strings() {
        throw new UnsupportedOperationException();
    }
}
