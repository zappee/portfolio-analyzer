package com.remal.portfolio.util;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * This is a formatter class that can after the proper initialization to
 * format BigDecimal values to Strings.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class BigDecimalFormatter {

    /**
     * Decimal separator.
     */
    private static final String SPLIT_REGEXP = "\\.";

    /**
     * The longest whole part.
     */
    @Getter
    private final AtomicInteger wholeLength = new AtomicInteger();

    /**
     * The longest fractional part.
     */
    @Getter
    private final AtomicInteger fractionalLength = new AtomicInteger();

    /**
     * Controls how the decimal numbers will be converted to String.
     */
    private final String decimalFormat;

    /**
     * The character used for thousands separator.
     */
    private final char decimalGroupingSeparator;

    /**
     * Constructor.
     *
     * @param decimalFormat controls how the decimal numbers will be converted to String
     * @param decimalGroupingSeparator character used for thousands separator
     */
    public BigDecimalFormatter(String decimalFormat, char decimalGroupingSeparator) {
        this.decimalFormat = decimalFormat;
        this.decimalGroupingSeparator = decimalGroupingSeparator;
    }

    /**
     * Returns with a consumer that can be used in a forEach iteration.
     *
     * @param scale scale of the BigDecimal value to be returned
     * @return the consumer
     */
    public BiConsumer<String, BigDecimal> get(int scale) {
        return (symbol, value) -> {
            var valueAsString = BigDecimals
                    .toString(
                            decimalFormat,
                            decimalGroupingSeparator,
                            value.setScale(scale, BigDecimals.ROUNDING_MODE))
                    .trim();
            var parts = valueAsString.split(SPLIT_REGEXP);
            int actualWhole;
            int actualFractional;
            if (parts.length == 1) {
                actualWhole = parts[0].length();
                actualFractional = 0;
            } else {
                actualWhole = parts[0].length();
                actualFractional = parts[1].length();
            }

            wholeLength.set(Math.max(wholeLength.get(), actualWhole));
            fractionalLength.set(Math.max(fractionalLength.get(), actualFractional));
        };
    }

    /**
     * Converts a decimal number to a formatted string.
     *
     * @param decimal the number to be converted
     * @param scale scale of the BigDecimal value to be returned
     * @return the decimal as a formatted string
     */
    public String format(BigDecimal decimal, int scale) {
        var formattedDecimal = BigDecimals.toString(
                decimalFormat,
                decimalGroupingSeparator,
                decimal.setScale(scale, BigDecimals.ROUNDING_MODE));
        var parts = formattedDecimal.split(SPLIT_REGEXP);
        var sb = new StringBuilder();

        if (parts.length == 1) {
            sb
                    .append(Strings.space(wholeLength.get() - parts[0].length()))
                    .append(parts[0]);
        } else {
            sb
                    .append(Strings.space(wholeLength.get() - parts[0].length()))
                    .append(parts[0])
                    .append(".")
                    .append(parts[1]);
        }

        return sb.toString();
    }
}
