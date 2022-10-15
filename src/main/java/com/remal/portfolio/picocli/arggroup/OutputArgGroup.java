package com.remal.portfolio.picocli.arggroup;

import com.remal.portfolio.picocli.converter.StringToListConverter;
import com.remal.portfolio.util.FileWriter;
import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Command line interface argument group for defining output options.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Getter
@Setter
public class OutputArgGroup {

    /**
     * Set the output file name.
     */
    @CommandLine.Option(
            names = {"-O", "--output-file"},
            description = "Write report to file (i.e. \"'tmp/'yyyy-MM-dd'_report.md'\"). "
                    + "Accepted extensions: .txt, .md and .csv")
    private String outputFile;

    /**
     *  Set the file open mode.
     */
    @CommandLine.Option(
            names = {"-M", "--file-mode"},
            description = "How to write the report to the file. Default: ${DEFAULT-VALUE} "
                    + "Candidates: ${COMPLETION-CANDIDATES}",
            defaultValue = "STOP_IF_EXIST")
    private FileWriter.WriteMode writeMode;

    /**
     * The list of the portfolio names that will replace to another value
     * during the parse.
     */
    @CommandLine.Option(
            names = {"-R", "--replace"},
            description = "Replace the portfolio name. Format: \"from:to, from:to\", e.g. \"default:coinbase\".",
            split = ",")
    private final List<String> replaces = new ArrayList<>();

    /**
     * Use it if you want to hide the report title.
     */
    @CommandLine.Option(
            names = {"-E", "--hide-title"},
            description = "Hide the report title.")
    private boolean hideTitle;

    /**
     * Use it if you want to hide the header of the table.
     */
    @CommandLine.Option(
            names = {"-A", "--hide-header"},
            description = "Hide the table header in the report.")
    private boolean hideHeader;

    /**
     * Set the report language.
     */
    @CommandLine.Option(
            names = {"-L", "--language"},
            description = "Two-letter ISO-639-1 language code that controls the report language. "
                    + "Default: ${DEFAULT-VALUE}",
            defaultValue = "EN")
    private String language;

    /**
     * List of the columns that won't be displayed in the reports.
     */
    @CommandLine.Option(
            names = {"-C", "--columns-to-hide"},
            description = "Comma separated list of column names that won't be displayed in the report. "
                    + "Candidates: PORTFOLIO, SYMBOL, TYPE, VALUATION, TRADE_DATE, QUANTITY, PRICE, FEE, "
                    + "CURRENCY, ORDER_ID, TRADE_ID, TRANSFER_ID",
            converter = StringToListConverter.class)
    private final List<String> columnsToHide = new ArrayList<>();

    /**
     * Set the decimal format that controls the format of numbers in the
     * report.
     */
    @CommandLine.Option(
            names = {"-I", "--decimal-format"},
            description = "Format numbers and decimals in the report. Default: \"${DEFAULT-VALUE}\"",
            defaultValue = "###,###,###,###,###,###.########")
    private String decimalFormat;

    /**
     * Pattern for formatting date and time in the report.
     */
    @CommandLine.Option(
            names = {"-D", "--out-date-pattern"},
            description = "Pattern for formatting date and time in the report. Default: \"${DEFAULT-VALUE}\"",
            defaultValue = "yyyy-MM-dd HH:mm:ss")
    private String dateTimePattern;

    /**
     * Set the timezone of the dates used by the report.
     */
    @CommandLine.Option(
            names = {"-Z", "--out-timezone"},
            description = "The timezone of the dates, e.g. \"GMT+2\", \"Europe/Budapest\" "
                    + "Default: the system default time-zone")
    private String zone = ZoneId.systemDefault().getId();

    /**
     * Set the filter that filters the list of transactions.
     */
    @CommandLine.Option(
            names = {"-F", "--out-from"},
            description = "Filter on trade date, after a specified date. Pattern: \"yyyy-MM-dd HH:mm:ss\"")
    private String from;

    /**
     * Set the filter that filters the list of transactions.
     */
    @CommandLine.Option(
            names = {"-T", "--out-to"},
            description = "Filter on trade date, before a specified date. Pattern: \"yyyy-MM-dd HH:mm:ss\"")
    private String to;
}
