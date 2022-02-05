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
     * Controls how the decimal numbers will be shown in the reports.
     */
    @Setter
    @Getter
    protected String decimalFormat = "%16.8f";

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
        Optional<String> stringValue = getStringValue(value);
        var actualDataWidth = stringValue.orElse("").length();
        var labelWidth = label.getLabel(language).length();
        var previousLength = widths.getOrDefault(label.getId(), 0);
        widths.put(label.getId(), Math.max(previousLength, Math.max(labelWidth, actualDataWidth)));
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
            stringValue = Optional.ofNullable(LocaleDateTimes.toString(zoneId, dateTimePattern, (LocalDateTime) value));

        } else if (value instanceof BigDecimal) {
            stringValue = Optional.of(BigDecimals.toString(decimalFormat, (BigDecimal) value).trim());

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
        return columnsToHide.contains(label.getId())
                ? ""
                : tableSeparator + Strings.leftPad(getStringValue(value).orElse(""), widths.get(label.getId()));
    }

    /**
     * Generates the left allignet cell value for the column title.
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
     * Builds the report header.
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
}
