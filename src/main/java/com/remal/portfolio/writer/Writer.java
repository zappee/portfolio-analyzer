package com.remal.portfolio.writer;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.InventoryValuationType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.ProviderType;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.util.Files;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import java.util.concurrent.atomic.AtomicInteger;

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
     * Log message at the end of the processing.
     */
    protected static final String ITEMS_HAS_BEEN_PROCESSED = "output > {} items have been processed by the writer";

    /**
     * Markdown table separator character.
     */
    protected String markdownSeparator = "|";

    /**
     * CSV separator character.
     */
    protected String csvSeparator = ",";

    /**
     * Default language.
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
     * If not null then date/time conversions will perform.
     */
    protected ZoneId zone;

    /**
     * Transaction date filter.
     */
    protected LocalDateTime from;

    /**
     * Transaction date filter.
     */
    protected LocalDateTime to;

    /**
     * Generate the CSV report.
     *
     * @param items data
     * @return the report content as a String
     */
    protected abstract String buildCsvReport(List<T> items);

    /**
     * Generate the Excel report.
     *
     * @param items data
     * @return the report content as bytes
     */
    protected abstract byte[] buildExcelReport(List<T> items);

    /**
     * Generate the Text/Markdown report.
     *
     * @param items data
     * @return the report content as a String
     */
    protected abstract String buildMarkdownReport(List<T> items);

    /**
     * Write the report to the output. The output can be a file ot the
     * standard output.
     *
     * @param writeMode control the way of open the file
     * @param fileNameTemplate the report file name, can contain date/time patterns too
     * @param items source of the report
     * @throws java.lang.UnsupportedOperationException unsupported file format was requested
     */
    public void write(FileWriter.WriteMode writeMode, String fileNameTemplate, List<T> items) {
        byte[] reportAsBytes;
        String filename;
        var fileType = Files.getFileType(fileNameTemplate);

        showConfiguration();
        switch (fileType) {
            case CSV -> {
                log.debug("output > generating the CSV report...");
                reportAsBytes = buildCsvReport(items).getBytes();
                filename = LocalDateTimes.toString(zone, fileNameTemplate, LocalDateTime.now());
                FileWriter.write(writeMode, filename, reportAsBytes);
                log.debug(ITEMS_HAS_BEEN_PROCESSED, items.size());
            }
            case EXCEL -> {
                if (writeMode == FileWriter.WriteMode.APPEND) {
                    var message = "The {} file mode is not supported in case of Excel output file format.";
                    Logger.logErrorAndExit(message, FileWriter.WriteMode.APPEND);
                }

                log.debug("output > generating the Excel report...");
                reportAsBytes = buildExcelReport(items);
                filename = LocalDateTimes.toString(zone, fileNameTemplate, LocalDateTime.now());
                FileWriter.write(writeMode, filename, reportAsBytes);
                log.debug(ITEMS_HAS_BEEN_PROCESSED, items.size());
            }
            case MARKDOWN -> {
                log.debug("output > generating the Markdown report...");
                reportAsBytes = buildMarkdownReport(items).getBytes();
                filename = LocalDateTimes.toString(zone, fileNameTemplate, LocalDateTime.now());
                FileWriter.write(writeMode, filename, reportAsBytes);
                log.debug(ITEMS_HAS_BEEN_PROCESSED, items.size());
            }
            case NOT_DEFINED -> {
                var reportAsString = buildMarkdownReport(items);
                log.debug(ITEMS_HAS_BEEN_PROCESSED, items.size());
                StdoutWriter.write(reportAsString);
            }
            default -> Logger.logErrorAndExit("Unsupported output file type: '{}'", fileNameTemplate);
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
            widths.put(label.getId(), Math.max(labelWidth, currentFullWidth));

        } else {
            Optional<String> stringValue = getStringValue(value);
            var actualDataWidth = stringValue.orElse("").length();
            var labelWidth = label.getLabel(language).length();
            var previousLength = widths.getOrDefault(label.getId(), 0);
            widths.put(label.getId(), Math.max(previousLength, Math.max(labelWidth, actualDataWidth)));
        }
    }

    /**
     * Return the value as a string or an empty string if the column is hidden.
     *
     * @param label column ID
     * @param value cell value
     * @return the value or an empty string
     */
    protected String getCell(Label label, Object value) {
        return columnsToHide.contains(label.getId()) ? "" : getStringValue(value).orElse("");
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
        if (columnsToHide.contains(label.getId())) {
            return "";
        }

        if (value instanceof BigDecimal x) {
            var fractionalWidth = widths.get(getFractionalWidthKey(label));
            var columnWidth = calculateBigDecimalWidth(widths, label);
            var labelWidth = label.getLabel(language).length();
            var wholeWidth = labelWidth > columnWidth
                    ? widths.get(getWholeWidthKey(label)) + labelWidth - columnWidth
                    : widths.get(getWholeWidthKey(label));

            var parts = partsOfBigDecimal(x);
            var spaces = (fractionalWidth == 0) ? "" : " ".repeat(fractionalWidth + 1);
            var valueAsFormattedString = parts[1].isEmpty()
                    ? Strings.rightPad(parts[0], wholeWidth) + spaces
                    : Strings.rightPad(parts[0], wholeWidth) + "." + Strings.leftPad(parts[1], fractionalWidth);
            return markdownSeparator + valueAsFormattedString;
        } else {
            var width = widths.get(label.getId());
            return markdownSeparator + Strings.leftPad(getStringValue(value).orElse(""), width);
        }
    }

    /**
     * Convert an object to String.
     *
     * @param value the value
     * @return the string representation of the object
     */
    private Optional<String> getStringValue(final Object value) {
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
            stringValue = Optional.of(LocalDateTimes.toString(zone, dateTimePattern, x));

        } else if (value instanceof BigDecimal x) {
            stringValue = Optional.of(BigDecimals.toString(decimalFormat, decimalGroupingSeparator, x).trim());

        } else if (value instanceof ProviderType x) {
            stringValue = Optional.of(x.name());

        } else if (value instanceof CurrencyType x) {
            stringValue = Optional.of(x.name());

        } else {
            stringValue = Optional.empty();
        }

        return stringValue;
    }

    /**
     * Generate a string that is used as a key in the Map that keeps the length of
     * BigDecimal fields.
     *
     * @param label the column ID
     * @return the key for the Map
     */
    private String getWholeWidthKey(Label label) {
        return label.getId() + "-W";
    }

    /**
     * Generate a string that is used as a key in the Map that keeps the length of
     * BigDecimal fields.
     *
     * @param label the column ID
     * @return the key for the Map
     */
    private String getFractionalWidthKey(Label label) {
        return label.getId() + "-F";
    }

    /**
     * Compute the full length of a decimal field.
     *
     * @param widths the list that keeps info about the columns
     * @param label column ID
     * @return the length of the decimal number
     */
    private int calculateBigDecimalWidth(Map<String, Integer> widths, Label label) {
        var wholeWidth = widths.get(getWholeWidthKey(label));
        var fractionalWidth = widths.get(getFractionalWidthKey(label));
        var hasDecimalPoint = fractionalWidth != 0;
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
        var splitValue = valueAsString.split(escapeDecimalSeparator(decimalSeparator));

        var parts = new String[2];
        parts[0] = splitValue[0];
        parts[1] = splitValue.length == 2 ? splitValue[1] : "";

        return parts;
    }

    /**
     * Escape the special character in order to it can be used as a regexp expression.
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

    /**
     * Write Excel spreadsheet to a byte array.
     *
     * @param workbook the Excel spreadsheet
     * @return the Excel file as a byte array
     */
    protected byte[] workbookToBytes(XSSFWorkbook workbook) {
        try (var outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            Logger.logErrorAndExit("Error while saving the Excel file, error: {}", e.toString());
        }
        return new byte[0];
    }

    /**
     * Set the cell value if the object is not null, otherwise skip
     * the set operation.
     *
     * @param row row in the Excel spreadsheet
     * @param columnIndex column index within the row
     * @param obj the value to be set as a cell value
     */
    protected void skipIfNullOrSet(XSSFWorkbook workbook, XSSFRow row, AtomicInteger columnIndex, Object obj) {
        if (Objects.isNull(obj)) {
            columnIndex.incrementAndGet();
        } else {
            if (obj instanceof BigDecimal x) {
                getNextCell(row, columnIndex).setCellValue(BigDecimals.valueOf(x));
            } else if (obj instanceof String x) {
                getNextCell(row, columnIndex).setCellValue(x);
            } else if (obj instanceof LocalDateTime x) {
                var cell = getNextCell(row, columnIndex);
                CellStyle dateCellStyle = workbook.createCellStyle();
                CreationHelper creationHelper = workbook.getCreationHelper();
                dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat(dateTimePattern));

                cell.setCellValue(x);
                cell.setCellStyle(dateCellStyle);

            } else {
                columnIndex.incrementAndGet();
                log.warn("Unhandled type: {}", obj.getClass().getSimpleName());
            }
        }
    }

    /**
     * Show the writer configuration.
     */
    private void showConfiguration() {
        log.debug("output > time zone: '{}'", zone.getId());
        log.debug("output > report has title: {}", !hideTitle);
        log.debug("output > table has header: {}", !hideHeader);
    }

    /**
     * Get the next cell in the row.
     *
     * @param row row in the Excel spreadsheet
     * @param columnIndex column index within the row
     * @return the next cell in the row
     */
    private XSSFCell getNextCell(XSSFRow row, AtomicInteger columnIndex) {
        return row.createCell(columnIndex.incrementAndGet());
    }
}
