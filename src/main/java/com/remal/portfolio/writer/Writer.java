package com.remal.portfolio.writer;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.DataProviderType;
import com.remal.portfolio.model.InventoryValuationType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.picocli.arggroup.TransactionParserInputArgGroup;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Common functionalities that is used by the report writers.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
@Setter
public abstract class Writer<T> {

    /**
     * New line character.
     */
    protected static final String NEW_LINE = System.lineSeparator();

    /**
     * Italic style for Markdown.
     */
    protected static final String MARKDOWN_ITALIC = "_";

    /**
     * Log message at the end of the processing.
     */
    protected static final String ITEMS_HAS_BEEN_PROCESSED = "> {} items have been processed by the writer";

    /**
     * Markdown table separator character.
     */
    protected String markdownSeparator = "|";

    /**
     * CSV separator character.
     */
    protected String csvSeparator = ",";

    /**
     * Report language.
     */
    protected String language = "en";

    /**
     * Set it to true to hide the report title.
     */
    protected boolean hideTitle = false;

    /**
     * Set it to true to hide the table headers.
     */
    protected boolean hideHeader = false;

    /**
     * Pattern that is used to display date and times.
     */
    protected String dateTimePattern = "yyyy.MM.dd HH:mm:ss";

    /**
     * Controls how the decimal numbers will be converted to String.
     */
    protected String decimalFormat = "###,###,###,###,###,###.########";

    /**
     * The character used for thousands separator.
     */
    protected char decimalGroupingSeparator = ' ';

    /**
     * List of the columns that won't be displayed in the report.
     */
    protected List<String> columnsToHide = new ArrayList<>();

    /**
     * The list of the portfolio names that will replace to another value.
     */
    protected List<String> portfolioNameReplaces = new ArrayList<>();

    /**
     * Time zone info use to parse the historical data.
     */
    protected ZoneId inputZone;

    /**
     * Time zone info used when writing out the report.
     */
    protected ZoneId outputZone;

    /**
     * Transaction date filter.
     */
    protected LocalDateTime from;

    /**
     * Transaction date filter.
     */
    protected LocalDateTime to;

    /**
     * File write mode.
     */
    protected FileWriter.WriteMode writeMode;

    /**
     * Generate the CSV report.
     *
     * @param items data
     * @return the report content as a String
     */
    protected abstract String buildCsvReport(List<T> items);

    /**
     * Generate the Text/Markdown report.
     *
     * @param items data
     * @return      the report content as a String
     */
    protected abstract String buildMarkdownReport(List<T> items);

    /**
     * Get the history data from file.
     *
     * @param filename data file name
     * @return data from the history file
     */
    protected abstract List<T> getHistoryFromFile(String filename);

    /**
     * Write the report to the output. The output can be a file or the
     * standard output.
     *
     * @param writeMode control the way of open the file
     * @param filename the report file name
     * @param item the report data
     */
    public void write(final FileWriter.WriteMode writeMode, final String filename, final T item) {
        write(writeMode, filename, List.of(item));
    }

    /**
     * Write the report to the output. The output can be a file or the
     * standard output.
     *
     * @param writeMode control the way of open the file
     * @param filename the report file name
     * @param items the report data
     */
    public void write(final FileWriter.WriteMode writeMode, final String filename, final List<T> items) {
        this.writeMode = writeMode;
        var fileType = com.remal.portfolio.util.Files.getFileType(filename);
        List<T> itemContainer = new ArrayList<>();

        if (Objects.nonNull(filename) && Files.exists(Path.of(filename)) && writeMode == FileWriter.WriteMode.APPEND) {
            // keep the items from the history file
            itemContainer.addAll(getHistoryFromFile(filename));
            itemContainer.addAll(items.stream().filter(item -> !itemContainer.contains(item)).toList());
        } else {
            itemContainer.addAll(items);
        }

        showConfiguration();
        switch (fileType) {
            case CSV -> {
                log.debug("> generating the CSV report...");
                decimalFormat = BigDecimals.UNFORMATTED;
                byte[] reportAsBytes = buildCsvReport(itemContainer).getBytes();
                FileWriter.write(writeMode, filename, reportAsBytes);
                log.debug(ITEMS_HAS_BEEN_PROCESSED, itemContainer.size());
            }
            case MARKDOWN -> {
                log.debug("> generating the Markdown report...");
                byte[] reportAsBytes = buildMarkdownReport(itemContainer).getBytes();
                FileWriter.write(writeMode, filename, reportAsBytes);
                log.debug(ITEMS_HAS_BEEN_PROCESSED, itemContainer.size());
            }
            case NOT_DEFINED -> {
                var reportAsString = buildMarkdownReport(itemContainer);
                log.debug(ITEMS_HAS_BEEN_PROCESSED, itemContainer.size());
                StdoutWriter.write(reportAsString);
            }
            default -> Logger.logErrorAndExit("Unsupported output file type: \"{}\"", filename);
        }
    }

    /**
     * Calculates the length of the columns in the reports based on the
     * column title and the length of the cell contents.
     *
     * @param widths the list that stores the width of the columns
     * @param label column title
     * @param value cell value
     */
    protected void updateWidth(Map<String, Integer> widths, Label label, final Object value) {
        if (value instanceof BigDecimal x) {
            // BigDecimal needs a special alignment:
            //    |label   |
            //    |  1     |
            //    | 12     |
            //    |  1.2   |
            //    | 12.3   |
            //    | 12.34  |
            //    |123.4567|
            var bigDecimalParts = partsOfBigDecimal(x);
            var previousWholeWidth = widths.getOrDefault(getWholeWidthKey(label), 0);
            var currentWholeWidth = bigDecimalParts[0].length();
            widths.put(getWholeWidthKey(label), Math.max(previousWholeWidth, currentWholeWidth));

            var previousFractionalWidth = widths.getOrDefault(getFractionalWidthKey(label), 0);
            var currentFractionalWidth = bigDecimalParts[1].length();
            widths.put(getFractionalWidthKey(label), Math.max(previousFractionalWidth, currentFractionalWidth));

            var currentFullWidth = calculateBigDecimalWidth(widths, label);
            var labelWidth = label.getLabel(language).length();
            widths.put(label.name(), Math.max(labelWidth, currentFullWidth));

        } else {
            Optional<String> stringValue = getStringValue(value);
            var actualDataWidth = stringValue.orElse("").length();
            var labelWidth = label.getLabel(language).length();
            var previousLength = widths.getOrDefault(label.name(), 0);
            widths.put(label.name(), Math.max(previousLength, Math.max(labelWidth, actualDataWidth)));
        }
    }

    /**
     * Return the value as a string or an empty string if the column is hidden.
     *
     * @param label column ID
     * @param cellValueAsObject cell value
     * @return the value or an empty string
     */
    protected String getCell(Label label, Object cellValueAsObject) {
        return getCell(label, cellValueAsObject, "");
    }

    /**
     * Return the value as a string or an empty string if the column is hidden.
     *
     * @param label column ID
     * @param cellValueAsObject cell value
     * @param separator separator character used in the report
     * @return the value or an empty string
     */
    protected String getCell(Label label, Object cellValueAsObject, String separator) {
        return columnsToHide.contains(label.name())
                ? ""
                : getStringValue(cellValueAsObject).map(value -> value + separator).orElse(separator);
    }

    /**
     * Generate an alignment value as a String.
     *
     * @param label column ID
     * @param value cell value
     * @param widths column width
     * @return the value or an empty String if the column is hidden
     */
    protected String getCell(Label label, Object value, Map<String, Integer> widths) {
        // LABEL_PORTFOLIO -> PORTFOLIO
        if (columnsToHide.contains(label.name().split("_")[1])) {
            return "";
        }

        if (value instanceof BigDecimal x) {
            var fractionalWidth = widths.getOrDefault(getFractionalWidthKey(label), 0);
            var columnWidth = calculateBigDecimalWidth(widths, label);
            var labelWidth = label.getLabel(language).length();
            var wholeWidth = labelWidth > columnWidth
                    ? widths.get(getWholeWidthKey(label)) + labelWidth - columnWidth
                    : widths.getOrDefault(getWholeWidthKey(label), widths.get(label.name()));

            var parts = partsOfBigDecimal(BigDecimals.isNullOrZero(x) ? null : x);
            var spaces = (Objects.isNull(fractionalWidth) || fractionalWidth == 0)
                    ? ""
                    : Strings.space(fractionalWidth + 1);
            var valueAsFormattedString = parts[1].isEmpty()
                    ? Strings.rightPad(parts[0], wholeWidth) + spaces
                    : Strings.rightPad(parts[0], wholeWidth) + "." + Strings.leftPad(parts[1], fractionalWidth);
            return markdownSeparator + valueAsFormattedString;
        } else {
            var width = widths.get(label.name());
            return markdownSeparator + Strings.leftPad(getStringValue(value).orElse(""), width);
        }
    }

    /**
     * Generate a string that is used as a key in the Map that keeps the length of
     * BigDecimal fields.
     *
     * @param label the column ID
     * @return the key for the Map
     */
    protected String getWholeWidthKey(Label label) {
        return label.name() + "-W";
    }

    /**
     * Generate a string that is used as a key in the Map that keeps the length of
     * BigDecimal fields.
     *
     * @param label the column ID
     * @return the key for the Map
     */
    protected String getFractionalWidthKey(Label label) {
        return label.name() + "-F";
    }

    /**
     * Convert an object to String.
     *
     * @param value the value
     * @return the string representation of the object
     */
    protected Optional<String> getStringValue(final Object value) {
        if (Objects.isNull(value)) {
            return Optional.empty();
        }

        Optional<String> stringValue;
        if (value instanceof String x) {
            stringValue = Optional.of(x);

        } else if (value instanceof TransactionType x) {
            stringValue = Optional.of(x.name());

        } else if (value instanceof InventoryValuationType x) {
            stringValue = Optional.of(x.name());

        } else if (value instanceof LocalDateTime x) {
            var valueInOutTimezone = LocalDateTimes.convertBetweenTimezones(x, inputZone, outputZone);
            stringValue = Optional.of(LocalDateTimes.toNullSafeString(null, dateTimePattern, valueInOutTimezone));

        } else if (value instanceof BigDecimal x) {
            stringValue = Optional.of(BigDecimals.toString(decimalFormat, decimalGroupingSeparator, x).trim());

        } else if (value instanceof DataProviderType x) {
            stringValue = Optional.of(x.name());

        } else if (value instanceof CurrencyType x) {
            stringValue = Optional.of(x.name());

        } else {
            stringValue = Optional.empty();
        }

        return stringValue;
    }

    /**
     * Build a new object instance.
     *
     * @param filename filename parameter
     * @return the initialized object
     */
    protected TransactionParserInputArgGroup buildTransactionParserInputArgGroup(String filename) {
        TransactionParserInputArgGroup inputArgGroup = new TransactionParserInputArgGroup();
        inputArgGroup.setFile(filename);
        inputArgGroup.setDateTimePattern(dateTimePattern);
        inputArgGroup.setZone(inputZone.getId());
        inputArgGroup.setMissingColumns(columnsToHide);
        inputArgGroup.hasTitle(!hideTitle);
        inputArgGroup.hasHeader(!hideHeader);
        return inputArgGroup;
    }

    /**
     * Compute the full length of a decimal field.
     *
     * @param widths the list that keeps info about the columns
     * @param label column ID
     * @return the length of the decimal number
     */
    private int calculateBigDecimalWidth(Map<String, Integer> widths, Label label) {
        var wholeWidth = widths.getOrDefault(getWholeWidthKey(label), widths.get(label.name()));
        var fractionalWidth = widths.get(getFractionalWidthKey(label));
        var hasDecimalPoint = Objects.nonNull(fractionalWidth) && fractionalWidth != 0;
        return hasDecimalPoint ? wholeWidth + fractionalWidth + 1 : wholeWidth;
    }

    /**
     * Split the decimal number to whole and fractal parts.
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
        var splitValue = valueAsString.split(Pattern.quote(String.valueOf(decimalSeparator)));

        var parts = new String[2];
        parts[0] = splitValue[0];
        parts[1] = splitValue.length == 2 ? splitValue[1] : "";

        return parts;
    }

    /**
     * Show the writer configuration.
     */
    private void showConfiguration() {
        log.debug("> input time zone: '{}'", inputZone.getId());
        log.debug("> output time zone: '{}'", outputZone.getId());
        log.debug(hideTitle ? "< printing the report without title" : "< printing the report with title");
        log.debug(hideHeader ? "< skipping to print the table header" : "< printing table header");
    }
}
