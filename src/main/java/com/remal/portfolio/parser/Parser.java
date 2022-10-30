package com.remal.portfolio.parser;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.FileType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.picocli.arggroup.InputArgGroup;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Files;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.ZoneIds;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

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
     * Log message.
     */
    protected static final String LOG_ERROR_GENERAL = "Error while parsing the \"{}\" file. {}";

    /**
     * Log message.
     */
    protected static final String LOG_ERROR_ARRAY_INDEX = "Error while parsing the \"{}\" file. "
            + "Can be consider to use '--has-report-title' and '--has-table-header' options. Error: {}";

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
     * Portfolio name filter.
     */
    @Setter
    protected String portfolio;

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
    protected String markdownSeparator = "|";

    /**
     * CSV separator.
     */
    protected String csvSeparator = ",";

    /**
     * Report language.
     */
    @Setter
    protected String language = "en";

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
     * Builds a parser instance.
     *
     * @param <T> the type of the parser
     * @param parserType parser type
     * @param inputArgs parameters from the command line interface
     * @param language report language
     * @param baseCurrency base currency used in the report
     * @return the parser instance
     */
    @SuppressWarnings("unchecked")
    protected static <T> Parser<T> build(Class<T> parserType,
                                         InputArgGroup inputArgs,
                                         String language,
                                         CurrencyType baseCurrency) {

        LocalDateTimes.validate(inputArgs.getDateTimePattern(), inputArgs.getFrom());
        LocalDateTimes.validate(inputArgs.getDateTimePattern(), inputArgs.getTo());
        ZoneIds.validate(inputArgs.getZone());

        Parser<T> parser;
        if (parserType.isAssignableFrom(Transaction.class)) {
            parser = (Parser<T>) new TransactionParser();
        } else {
            parser = (Parser<T>) new PortfolioSummaryParser(baseCurrency);
        }

        var zoneId = ZoneId.of(inputArgs.getZone());

        parser.hasTitle(inputArgs.hasTitle());
        parser.hasHeader(inputArgs.hasHeader());
        parser.setDateTimePattern(inputArgs.getDateTimePattern());
        parser.setZone(zoneId);
        parser.setFrom(LocalDateTimes.toLocalDateTime(zoneId, inputArgs.getDateTimePattern(), inputArgs.getFrom()));
        parser.setTo(LocalDateTimes.toLocalDateTime(zoneId, inputArgs.getDateTimePattern(), inputArgs.getTo()));
        parser.setMissingColumns(inputArgs.getMissingColumns());
        parser.setPortfolio(inputArgs.getPortfolio());
        parser.setSymbols(inputArgs.getSymbols());
        parser.setLanguage(language);
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
            case MARKDOWN -> {
                log.debug(LOG_BEFORE_EXECUTION, filename, "Markdown");
                items = parseMarkdownFile(filename);
            }
            default -> {
                Logger.logErrorAndExit("Unsupported input file type: '{}'", filename);
                items = Collections.emptyList();
            }
        }

        return filterByPortfolioAndSymbols(items);
    }

    /**
     * Apply portfolio name and symbol filters if the list contains Transactions.
     * <p>
     * Java uses type erasure, which means at runtime objects are equivalent. The type of the item information is lost
     * at runtime, and the list only contain Objects. But if the list is not empty, then the item type can be
     * determined.
     * </p>
     *
     * @param items the list
     * @return the reduced list
     */
    private List<T> filterByPortfolioAndSymbols(List<T> items) {
        var reducedList = items;
        log.info("< {} items have been loaded by the parser", reducedList.size());

        var firstItem = items.stream().findFirst();
        if (firstItem.isPresent() && firstItem.get() instanceof Transaction) {
            reducedList = items
                        .stream()
                        .filter(t -> Filter.portfolioNameFilter(getPortfolio(), (Transaction) t))
                        .filter(t -> Filter.symbolFilter(getSymbols(), (Transaction) t))
                        .toList();
            log.info("< items after filtering by portfolio and symbols: {}", reducedList.size());
        }
        return reducedList;
    }

    /**
     * Process a CSV file.
     *
     * @param file path to the data file
     * @return     the list of the parsed items
     */
    protected abstract List<T> parseCsvFile(String file);

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
            case CSV -> hasTitle ? 1 : 0;
            case MARKDOWN -> hasTitle ? 4 : 0;
            default -> 0;
        };

        var headerRows = switch (fileType) {
            case CSV -> hasHeader ? 1 : 0;
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
     * @param index the variable holds the index's value
     * @param fields the parsed line from the input file
     * @param actualColumn column ID
     * @return the next index value
     */
    protected String getString(AtomicInteger index, String[] fields, Label actualColumn) {
        if (Objects.nonNull(actualColumn) && missingColumns.contains(actualColumn.name())) {
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
        if (missingColumns.contains(actualColumn.name())) {
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
        if (missingColumns.contains(Label.HEADER_TRADE_DATE.name())) {
            return null;
        } else {
            return LocalDateTimes.toLocalDateTime(zone, dateTimePattern, fields[index.getAndIncrement()].trim());
        }
    }

    /**
     * Show the parser configuration.
     */
    private void showConfiguration() {
        log.debug("< portfolio name filter: '{}'", portfolio);
        log.debug("< symbol filter: '{}'", symbols);
        log.debug("< time zone: '{}'", Objects.isNull(zone) ? "<not defined>" : zone.getId());
        log.debug(hasTitle ? "< printing the report with title" : "< printing the report without title");
        log.debug(hasHeader ? "< printing table header" : "< skipping to print the table header");
        if (!symbols.isEmpty()) {
            log.debug("< showing only the following symbols: {}", symbols);
        }
    }
}
