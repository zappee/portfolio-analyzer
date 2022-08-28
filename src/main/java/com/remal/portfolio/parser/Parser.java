package com.remal.portfolio.parser;

import com.remal.portfolio.model.FileType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.picocli.arggroup.InputArgGroup;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Files;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.ZoneIds;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parser common functions and method definitions that all parsers
 * must implement.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
@Setter
@Getter
public abstract class Parser<T> {

    /**
     * DataFormatter contains methods for formatting the value stored
     * in an Excel cell.
     */
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    /**
     * Log message.
     */
    protected static final String LOG_ERROR_GENERAL = "Error while parsing the \"{}\" file. {}";

    /**
     * Log message.
     */
    protected static final String LOG_ERROR_ARRAY_INDEX = "Error while parsing the \"{}\" file. "
            + "Can be consider to use '--has-title' and '--has-header' options. Error: {}";

    /**
     * Log message.
     */
    private static final String LOG_BEFORE_EXECUTION = "< reading the \"{}\" {} file...";

    /**
     *  Use it if the input report file does not contain title.
     */
    @Accessors(fluent = true)
    protected boolean hasTitle = true;

    /**
     *Use it if the table in the report file does not have header.
     */
    @Accessors(fluent = true)
    protected boolean hasHeader = true;

    /**
     * Date/time pattern that is used for converting string to LocalDateTime.
     */
    protected String dateTimePattern = "yyyy-MM-dd HH:mm:ss";

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
     * Product name filter.
     */
    private List<String> symbols = new ArrayList<>();

    /**
     * Set the name of the missing columns in the report.
     */
    protected List<String> missingColumns = new ArrayList<>();

    /**
     * Markdown separator.
     */
    protected String markdownSeparator = "\\|";

    /**
     * CSV separator.
     */
    protected String csvSeparator = ",";

    /**
     * Builder that initializes a new writer instance.
     *
     * @param arguments input arguments
     * @return          the parser instance
     */
    public static Parser<Transaction> build(InputArgGroup arguments) {
        // validating the input params
        LocalDateTimes.validate(arguments.getDateTimePattern(), arguments.getFrom());
        LocalDateTimes.validate(arguments.getDateTimePattern(), arguments.getTo());
        ZoneIds.validate(arguments.getZone());

        // initialize a parser
        var zoneId = ZoneId.of(arguments.getZone());
        Parser<Transaction> parser = new TransactionParser();
        parser.hasTitle(arguments.hasTitle());
        parser.hasHeader(arguments.hasHeader());
        parser.setDateTimePattern(arguments.getDateTimePattern());
        parser.setZone(zoneId);
        parser.setFrom(LocalDateTimes.toLocalDateTime(zoneId, arguments.getDateTimePattern(), arguments.getFrom()));
        parser.setTo(LocalDateTimes.toLocalDateTime(zoneId, arguments.getDateTimePattern(), arguments.getTo()));
        parser.setMissingColumns(arguments.getMissingColumns());
        parser.setSymbols(arguments.getSymbols());
        return parser;
    }

    /**
     * Builder that initializes a new writer instance.
     *
     * @param arguments input arguments
     * @return          the parser instance
     */
    public static Parser<Price> build(PriceArgGroup.OutputArgGroup arguments) {
        var zoneId = ZoneId.of(arguments.getZone());
        Parser<Price> parser = new PriceParser();
        parser.setDateTimePattern(arguments.getDateTimePattern());
        parser.setZone(zoneId);
        return parser;
    }

    /**
     * Parse the file.
     *
     * @param filename path to the data file
     * @return         the list of the parsed items
     */
    public List<T> parse(String filename) {
        List<T> items;
        var fileType = Files.getFileType(filename);

        showConfiguration();
        switch (fileType) {
            case CSV -> {
                log.debug(LOG_BEFORE_EXECUTION, filename, "CSV");
                items = parseCsvFile(filename);
            }
            case EXCEL -> {
                log.debug(LOG_BEFORE_EXECUTION, filename, "Excel");
                items = parseExcelFile(filename);
            }
            case MARKDOWN -> {
                log.debug(LOG_BEFORE_EXECUTION, filename, "Markdown");
                items = parseMarkdownFile(filename);
            }
            default -> {
                Logger.logErrorAndExit("Unsupported input file type: '{}'", filename);
                items = Collections.emptyList();
            }
        }

        log.info("< {} items have been loaded by the parser", items.size());
        return items;
    }

    /**
     * Process a CSV file.
     *
     * @param file path to the data file
     * @return     the list of the parsed items
     */
    protected abstract List<T> parseCsvFile(String file);

    /**
     * Process an Excel file.
     *
     * @param file path to the data file
     * @return     the list of the parsed items
     */
    protected abstract List<T> parseExcelFile(String file);

    /**
     * Process a Text/Markdown file.
     *
     * @param file path to the data file
     * @return     the list of the parsed items
     */
    protected abstract List<T> parseMarkdownFile(String file);

    /**
     * Calculate the id of the first data row based on the title and header info.
     *
     * @param fileType file type
     * @return         the first line that contains data
     */
    protected int getFirstDataRow(FileType fileType) {
        var titleRows = switch (fileType) {
            case CSV, EXCEL -> hasTitle ? 1 : 0;
            case MARKDOWN -> hasTitle ? 3 : 0;
            default -> 0;
        };

        var headerRows = switch (fileType) {
            case CSV, EXCEL -> hasHeader ? 1 : 0;
            case MARKDOWN -> hasHeader ? 2 : 0;
            default -> 0;
        };

        var skipRows = titleRows + headerRows;
        if (skipRows != 0) {
            log.info("< skipping the first {} lines while reading the file...", skipRows);
        }
        return skipRows;
    }

    /**
     * Get the value based on the missing/hidden columns.
     *
     * @param index        the variable holds the index's value
     * @param fields       the parsed line from the input file
     * @param actualColumn column ID
     * @return             the next index value
     */
    protected String getString(AtomicInteger index, String[] fields, Label actualColumn) {
        if (missingColumns.contains(actualColumn.getId())) {
            return null;
        } else {
            var value = fields[index.getAndIncrement()];
            return value.isBlank() ? null : value.trim();
        }
    }

    /**
     * Get the value based on the missing/hidden columns.
     *
     * @param index        the variable holds the index's value
     * @param fields       the parsed line from the input file
     * @param actualColumn column ID
     * @return             the next index value
     */
    protected BigDecimal getBigDecimal(AtomicInteger index, String[] fields, Label actualColumn) {
        if (missingColumns.contains(actualColumn.getId())) {
            return null;
        } else {
            return BigDecimals.valueOf(fields[index.getAndIncrement()].trim());
        }
    }

    /**
     * Get the value based on the missing/hidden columns.
     *
     * @param index  the variable holds the index's value
     * @param fields the parsed line from the input file
     * @return       the next index value
     */
    protected LocalDateTime getLocalDateTime(AtomicInteger index, String[] fields) {
        if (missingColumns.contains(Label.HEADER_TRADE_DATE.getId())) {
            return null;
        } else {
            return LocalDateTimes.toLocalDateTime(zone, dateTimePattern, fields[index.getAndIncrement()].trim());
        }
    }

    /**
     * Get an excel cell value as a String.
     *
     * @param row      row in the Excel spreadsheet
     * @param colIndex column index within the row
     * @return         the cell value as a String or null in the row and column indexes are invalid
     */
    protected String getCellValueAsString(XSSFRow row, int colIndex) {
        return Objects.isNull(row.getCell(colIndex)) ? null : DATA_FORMATTER.formatCellValue(row.getCell(colIndex));
    }

    /**
     * Get an excel cell value as a BigDecimal.
     *
     * @param row      row in the Excel spreadsheet
     * @param colIndex column index within the row
     * @return         the cell value as a BigDecimal or null in the row and column indexes are invalid
     */
    protected BigDecimal getCellValueAsBigDecimal(XSSFRow row, int colIndex) {
        return row.getCell(colIndex) == null
                ? null
                : new BigDecimal(DATA_FORMATTER.formatCellValue(row.getCell(colIndex)));
    }

    /**
     * Get an excel cell value as a String.
     *
     * @param row      row in the Excel spreadsheet
     * @param colIndex column index
     * @return         the cell value as a String or null in the row and column indexes are invalid
     */
    protected LocalDateTime getCellValueAsLocalDateTime(XSSFRow row, int colIndex) {
        if (Objects.isNull(row.getCell(colIndex))) {
            return null;
        } else {
            return row.getCell(colIndex).getLocalDateTimeCellValue().atZone(zone).toLocalDateTime();
        }
    }

    /**
     * Show the parser configuration.
     */
    private void showConfiguration() {
        log.debug("< time zone: '{}'", Objects.isNull(zone) ? "<not defined>" : zone.getId());
        log.debug(hasTitle ? "< printing the report with title" : "< printing the report without title");
        log.debug(hasHeader ? "< printing table header" : "< skipping to print the table header");
        if (!symbols.isEmpty()) {
            log.debug("< showing only the following symbols: {}", symbols);
        }
    }
}
