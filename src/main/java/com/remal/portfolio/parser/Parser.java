package com.remal.portfolio.parser;

import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.picocli.arggroup.InputArgGroup;
import com.remal.portfolio.util.Files;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.ZoneIds;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
     * Log message.
     */
    protected static final String LOG_ERROR_GENERAL = "Error while parsing the '{}' file. Error: {}";

    /**
     * Log message.
     */
    protected static final String LOG_ERROR_ARRAY_INDEX = "Error while parsing the '{}' file. "
            + "Can be consider to use '--has-title' and '--has-header' options. Error: {}";

    /**
     * Log message.
     */
    private static final String LOG_BEFORE_EXECUTION = "input < reading the '{}' {} file...";

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
    private List<String> tickers = new ArrayList<>();

    /**
     * Set the name of the missing columns in the report.
     */
    protected List<String> missingColumns;

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
     * @return the parser instance
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
        parser.setTickers(arguments.getTickers());
        return parser;
    }

    /**
     * Parse the file.
     *
     * @param fileNameTemplate path to the data file
     * @return the list of the parsed items
     */
    public List<T> parse(String fileNameTemplate) {
        List<T> items;
        var filename = LocalDateTimes.toString(zone, fileNameTemplate, LocalDateTime.now());
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

        log.debug("input < {} items have been loaded by the parser", items.size());
        return items;
    }

    /**
     * Process a CSV file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    protected abstract List<T> parseCsvFile(String file);

    /**
     * Process an Excel file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    protected abstract List<T> parseExcelFile(String file);

    /**
     * Process a Text/Markdown file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    protected abstract List<T> parseMarkdownFile(String file);

    /**
     * Show the parser configuration.
     */
    private void showConfiguration() {
        log.debug("input < time zone: '{}'", zone.getId());
        log.debug("input < report has title: {}", hasTitle);
        log.debug("input < table has header: {}", hasHeader);
        if (!tickers.isEmpty()) {
            log.debug("input < show only the following tickers: {}", tickers);
        }
    }
}
