package com.remal.portfolio.picocli.arggroup;

import com.remal.portfolio.model.MultiplicityType;
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
public class PortfolioArgGroup {

    /**
     * Output configuration.
     */
    @Getter
    public static class OutputArgGroup {

        /**
         * Set the ISO 4217 currency code that Coinbase
         * registered for you.
         */
        @CommandLine.Option(
                names = {"-B", "--base-currency"},
                description = "The currency of the portfolio report, e.g. \"EUR\", etc. "
                        + "Default: \"${DEFAULT-VALUE}\"",
                defaultValue = "EUR")
        private String baseCurrency;

        /**
         * Set the output file name.
         */
        @CommandLine.Option(
                names = {"-O", "--output-file"},
                description = "Write report to file (i.e. \"'tmp/'yyyy-MM-dd'_report.md'\"). "
                        + "Accepted extensions: .txt, .md, .csv and .xlsx")
        private String outputFile;

        /**
         * Set the file open mode.
         */
        @CommandLine.Option(
                names = {"-M", "--file-mode"},
                description = "How to write the report to the file. Default: ${DEFAULT-VALUE} "
                        + "Candidates: ${COMPLETION-CANDIDATES}",
                defaultValue = "STOP_IF_EXIST")
        private FileWriter.WriteMode writeMode;

        /**
         * Controls the price export to file.
         */
        @CommandLine.Option(
                names = {"-U", "--multiplicity"},
                description = "Controls the price export to file. Candidates: ${COMPLETION-CANDIDATES}. "
                        + "Default: ONE_HOUR.",
                defaultValue = "ONE_HOUR",
                required = true)
        private MultiplicityType multiplicity;

        /**
         * Show the relevant transactions in the report.
         */
        @CommandLine.Option(
                names = {"-J", "--show-transactions"},
                description = "Show the relevant transactions.")
        public boolean showTransactions;

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
         * Set the decimal format that controls the format of numbers in the
         * report.
         */
        @CommandLine.Option(
                names = {"-I", "--decimal-format"},
                description = "Format numbers and decimals in the report. Default: \"${DEFAULT-VALUE}\"",
                defaultValue = "##################.########")
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
         * List of the columns that won't be displayed in the reports.
         */
        @CommandLine.Option(
                names = {"-C", "--columns-to-hide"},
                description = "Comma separated list of column names that won't be displayed in the report. "
                        + "Candidates: PORTFOLIO, SYMBOL, QUANTITY, AVG_PRICE, INVESTED_AMOUNT, MARKET_UNIT_PRICE, "
                        + "MARKET_VALUE, PROFIT_LOSS, PROFIT_LOSS_PERCENT, COSTS, DEPOSITS, WITHDRAWALS, TOTAL_CASH, "
                        + "TOTAL_EXCHANGE_RATE, TOTAL_DEPOSIT, TOTAL_WITHDRAWAL, TOTAL_INVESTMENT, "
                        + "TOTAL_MARKET_VALUE, TOTAL_PROFIT_LOSS",
                converter = StringToListConverter.class)
        private final List<String> columnsToHide = new ArrayList<>();
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private PortfolioArgGroup() {
        throw new UnsupportedOperationException();
    }
}
