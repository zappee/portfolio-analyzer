package com.remal.portfolio.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

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
     * Generate a fixed length space.
     *
     * @param repeat number of times to repeat whitespace
     * @return the space
     */
    public static String space(final int repeat) {
        return leftPad(null, repeat);
    }

    /**
     * Generate a fixed length space.
     *
     * @param ch character to repeat
     * @param repeat number of times to repeat char, negative treated as zero
     * @return the space
     */
    public static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return "";
        }

        var buf = new char[repeat];
        Arrays.fill(buf, ch);
        return new String(buf);
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
