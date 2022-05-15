package com.remal.portfolio.picocli.arggroup;

import com.remal.portfolio.picocli.converter.StringToListConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import picocli.CommandLine;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Command line interface argument group for defining input options.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Getter
@Setter
public class InputArgGroup {

    /**
     * Use it if the report file has a title.
     */
    @CommandLine.Option(
            order = 100,
            names = {"-e", "--has-title"},
            description = "The report file contains title.")
    @Accessors(fluent = true)
    private boolean hasTitle;

    /**
     * Use it if the table in the report has header.
     */
    @CommandLine.Option(
            order = 100,
            names = {"-a", "--has-header"},
            description = "The table has a header in the report.")
    @Accessors(fluent = true)
    private boolean hasHeader;

    /**
     * Set the portfolio name filter.
     */
    @CommandLine.Option(
            order = 100,
            names = {"-p", "--portfolio"},
            description = "Portfolio name filter.")
    private String portfolio;

    /**
     * Set the product name filter.
     */
    @CommandLine.Option(
            order = 100,
            names = {"-c", "--ticker"},
            description = "Product filter, that is a comma separated list with tickers, e.g. \"BTC-EUR, AMZN\".",
            converter = StringToListConverter.class)
    private List<String> tickers = new ArrayList<>();

    /**
     * Pattern for parsing date and time.
     */
    @CommandLine.Option(
            order = 100,
            names = {"-d", "--in-date-pattern"},
            description = "Pattern for parsing date and time. Default: \"${DEFAULT-VALUE}\"",
            defaultValue = "yyyy-MM-dd HH:mm:ss")
    private String dateTimePattern;

    /**
     * Set the timezone of the input data.
     */
    @CommandLine.Option(
            order = 100,
            names = {"-z", "--in-timezone"},
            description = "The timezone of the dates, e.g. \"GMT+2\", \"Europe/Budapest\" "
                    + "Default: the system default time-zone")
    private String zone = ZoneId.systemDefault().getId();

    /**
     * Set the filter that filters the list of transactions.
     */
    @CommandLine.Option(
            order = 100,
            names = {"-f", "--in-from"},
            description = "Filter on trade date, after a specified date. Pattern: \"yyyy-MM-dd HH:mm:ss\"")
    private String from;

    /**
     * Set the filter that filters the list of transactions.
     */
    @CommandLine.Option(
            order = 100,
            names = {"-t", "--in-to"},
            description = "Filter on trade date, before a specified date. Pattern: \"yyyy-MM-dd HH:mm:ss\"")
    private String to;

    /**
     * Set the name of the missing columns in the report.
     */
    @CommandLine.Option(
            order = 100,
            names = {"-m", "--missing-columns"},
            description = "Comma separated list to set the missing columns in the report. "
                    + "Use with the '-columns-to-hide' option.",
            converter = StringToListConverter.class)
    private List<String> missingColumns = new ArrayList<>();
}

