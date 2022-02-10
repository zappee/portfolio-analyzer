package com.remal.portfolio.writer;

import com.remal.portfolio.model.Currency;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.LocaleDateTimes;
import com.remal.portfolio.util.Strings;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Common functionalities that is used by the report writers.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public abstract class Writer {

    /**
     * Markdown table separator character.
     */
    @Setter
    @Getter
    protected String tableSeparator = "|";

    /**
     * New line character.
     */
    @Setter
    @Getter
    protected String newLine = System.lineSeparator();

    /**
     * Default language.
     */
    @Setter
    @Getter
    protected String language = "en";

    /**
     * If it set to true then the header of the transaction table will print.
     */
    @Setter
    @Getter
    protected boolean showHeader = true;

    /**
     * Timezone that is used when timestamp is rendered.
     */
    @Getter
    @Setter
    protected String zoneIdAsString = "Europe/London";

    /**
     * Date pattern that is used to show timestamps in the reports.
     */
    @Getter
    @Setter
    protected String dateTimePattern = "yyyy.MM.dd HH:mm:ss";

    /**
     * Controls how the decimal numbers will be converted to String.
     */
    @Setter
    @Getter
    protected String decimalFormat = "###,###,###,###,###,###.########";

    /**
     * The character used for thousands separator.
     */
    @Setter
    @Getter
    protected char decimalGroupingSeparator = ' ';

    /**
     * List of the columns that will be displayed in the report.
     */
    @Setter
    @Getter
    protected List<String> columnsToHide = new ArrayList<>();

    /**
     * Calculates the length of the columns in the reports based on the
     * column title and the length of the cell contents.
     *
     * @param widths the list that stores with of the columns
     * @param label column title
     * @param value cell value
     */
    protected void updateWidth(Map<String, Integer> widths, Label label, final Object value) {
        if (value instanceof BigDecimal) {
            // BigDecimal needs a special alignment:
            //    |label   |
            //    |  1     |
            //    | 12     |
            //    |  1.2   |
            //    | 12.3   |
            //    | 12.34  |
            //    |123.4567|
            var previousWholeWidth = widths.getOrDefault(getWholeWidthKey(label), 0);
            var previousFractionalWidth = widths.getOrDefault(getFractionalWidthKey(label), 0);
            var bigDecimalParts = partsOfBigDecimal((BigDecimal) value);

            var currentWidth = calculateBigDecimalWidth(bigDecimalParts[0].length(), bigDecimalParts[1].length());
            var previousWidth = calculateBigDecimalWidth(previousWholeWidth, previousFractionalWidth);

            var fractalWidth = Math.max(previousFractionalWidth, bigDecimalParts[1].length());
            var labelWidth = label.getLabel(language).length();
            var width = Math.max(currentWidth, previousWidth);
            if (labelWidth > width) {
                var remainder = fractalWidth == 0 ? labelWidth : labelWidth - fractalWidth - 1;
                widths.put(getWholeWidthKey(label), remainder);
            } else {
                widths.put(getWholeWidthKey(label), Math.max(previousWholeWidth, bigDecimalParts[0].length()));
            }

            widths.put(getFractionalWidthKey(label), fractalWidth);
            widths.put(label.getId(), Math.max(labelWidth, Math.max(currentWidth, previousWidth)));

        } else {
            Optional<String> stringValue = getStringValue(value);
            var actualDataWidth = stringValue.orElse("").length();
            var labelWidth = label.getLabel(language).length();
            var previousLength = widths.getOrDefault(label.getId(), 0);
            widths.put(label.getId(), Math.max(previousLength, Math.max(labelWidth, actualDataWidth)));
        }
    }

    /**
     * Converts values to string.
     *
     * @param value the value
     * @return the string representation of the given value
     */
    private Optional<String> getStringValue(final Object value) {
        if (Objects.isNull(value)) {
            return Optional.empty();
        }

        Optional<String> stringValue;
        if (value instanceof String) {
            stringValue = Optional.of(value.toString());

        } else if (value instanceof TransactionType) {
            stringValue = Optional.of(((TransactionType) value).name());

        } else if (value instanceof LocalDateTime) {
            var zoneId = ZoneId.of(zoneIdAsString);
            stringValue = Optional.ofNullable(LocaleDateTimes.toString(
                    zoneId,
                    dateTimePattern,
                    (LocalDateTime) value));

        } else if (value instanceof BigDecimal) {
            stringValue = Optional.of(BigDecimals.toString(
                    decimalFormat,
                    decimalGroupingSeparator,
                    (BigDecimal) value).trim());

        } else if (value instanceof Currency) {
            stringValue = Optional.of(((Currency) value).name());

        } else {
            stringValue = Optional.empty();
        }

        return stringValue;
    }

    /**
     * Generates the left allignet cell value.
     *
     * @param label column title
     * @param value cell value
     * @param widths column width
     * @return the cell value or an empty string if the column is hidded
     */
    protected String getCell(Label label, Object value, Map<String, Integer> widths) {
        if (columnsToHide.contains(label.getId())) {
            return "";
        }

        var wholeWidth = widths.get(getWholeWidthKey(label));
        var fractionalWidth = widths.get(getFractionalWidthKey(label));

        if (value instanceof BigDecimal) {
            var parts = partsOfBigDecimal((BigDecimal) value);
            var valueAsFormattedString = parts[1].isEmpty()
                    ? Strings.rightPad(parts[0], wholeWidth) + " ".repeat(fractionalWidth + 1)
                    : Strings.rightPad(parts[0], wholeWidth) + "." + Strings.leftPad(parts[1], fractionalWidth);
            return tableSeparator + valueAsFormattedString;
        } else {
            var width = widths.get(label.getId());
            return tableSeparator + Strings.rightPad(getStringValue(value).orElse(""), width);
        }
    }

    /**
     * Returns the header label or with an empty string if the collumn is hidded.
     *
     * @param label column title
     * @param value cell value
     * @return the cell title or an empty string if the column is hidded
     */
    protected String getCell(Label label, Object value) {
        return columnsToHide.contains(label.getId())
                ? ""
                : getStringValue(value).orElse("");
    }

    /**
     * Builds the report header with title and timestamp.
     *
     * @param title report title
     * @return the report header
     */
    protected StringBuilder buildReportHeader(String title) {
        var sb = new StringBuilder();
        var zoneId = ZoneId.of(zoneIdAsString);
        sb
                .append("# ")
                .append(title)
                .append(newLine);
        sb
                .append(Label.GENERATED.getLabel(language))
                .append(": ")
                .append(LocaleDateTimes.toString(zoneId, dateTimePattern, LocalDateTime.now()))
                .append(newLine);

        return sb;
    }

    /**
     * Generated the key that is used in Map for BigDecimal types.
     *
     * @param label the column label
     * @return id of the label for the whole part of the BigDecimal type
     */
    private String getWholeWidthKey(Label label) {
        return label.getId() + "w";
    }

    /**
     * Generated the key that is used in Map for BigDecimal types.
     *
     * @param label the column label
     * @return the id of the label for the fractional part of the BigDecimal type
     */
    private String getFractionalWidthKey(Label label) {
        return label.getId() + "f";
    }

    /**
     * Compute the full length of the decimal fields.
     *
     * @param wholeLength the length of the whole part of the decimal number
     * @param fractalLength the length of the fractial part of the decimal number
     * @return the length of the decimal number
     */
    private int calculateBigDecimalWidth(int wholeLength, int fractalLength) {
        return fractalLength == 0
                ? wholeLength
                : wholeLength + fractalLength + 1;
    }

    /**
     * Splits the decimal number to whole and fractal parts.
     *
     * @param value the decimal number
     * @return the parts
     */
    private String[] partsOfBigDecimal(BigDecimal value) {
        if (Objects.isNull(value)) {
            return new String[] {"", ""};
        }

        var decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalFormatSymbols.setGroupingSeparator(decimalGroupingSeparator);

        var formatter = new DecimalFormat(decimalFormat, decimalFormatSymbols);
        var valueAsString = formatter.format(value);

        var decimalSeparator = decimalFormatSymbols.getDecimalSeparator();
        var splittedValue = valueAsString.split(escapeDecimalSeparator(decimalSeparator));

        var parts = new String[2];
        parts[0] = splittedValue[0];
        parts[1] = splittedValue.length == 2 ? splittedValue[1] : "";

        return parts;
    }

    /**
     * Escapes the special character in order to it can be used as a regexp expression.
     *
     * @param charToEscape the special character
     * @return the escaped special character
     */
    private String escapeDecimalSeparator(char charToEscape) {
        if (charToEscape == '.') {
            return "\\" + charToEscape;
        } else {
            return String.valueOf(charToEscape);
        }
    }
}
