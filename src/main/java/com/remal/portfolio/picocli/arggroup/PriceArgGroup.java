package com.remal.portfolio.picocli.arggroup;

import com.remal.portfolio.model.MultiplicityType;
import com.remal.portfolio.model.ProviderType;
import com.remal.portfolio.util.FileWriter;
import lombok.Getter;
import picocli.CommandLine;

import java.time.ZoneId;

/**
 * Implementation of the 'price' command.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Getter
public class PriceArgGroup {

    /**
     * Input configuration.
     */
    @Getter
    public static class InputArgGroup {

        /**
         * Set the stock name.
         */
        @CommandLine.Option(
                names = {"-i", "--ticker"},
                required = true,
                description = "The product id that represents the company's stock.")
        private String ticker;

        /**
         * Data provider definition.
         */
        @CommandLine.ArgGroup(
                multiplicity = "1")
        private final ProviderArgGroup providerArgGroup = new ProviderArgGroup();

        /**
         * The price of a stock on a certain date in the past.
         */
        @CommandLine.Option(
                names = {"-c", "--date"},
                description = "The price of a stock on a certain date in the past.")
        private String tradeDate;

        /**
         * Set the date/time format used for parsing the provided date.
         */
        @CommandLine.Option(
                names = {"-t", "--date-pattern"},
                description = "Pattern for parsing the provided date. Default: \"${DEFAULT-VALUE}\".",
                defaultValue = "yyyy-MM-dd HH:mm:ss")
        private String dateTimePattern;
    }

    /**
     * Data provider CLi options.
     */
    @Getter
    public static class ProviderArgGroup {

        /**
         * Set the data provider name.
         */
        @CommandLine.Option(
                names = {"-d", "--provider-id"},
                description = "Retrieve the market price using the provider. Candidates: ${COMPLETION-CANDIDATES}.")
        private ProviderType providerType;

        /**
         * Set the data provider properties file.
         */
        @CommandLine.Option(
                names = {"-f", "--provider-file"},
                description = "Get the provider name used to retrieve the market price from a *.properties file.")
        private String providerFile;
    }

    /**
     * Output configuration.
     */
    @Getter
    public static class OutputArgGroup {

        /**
         * Set the price history file.
         */
        @CommandLine.Option(
                names = {"-F", "--history-file"},
                description = "Storing the price in a file, e.g. \"'price_'yyyy'.md'\". "
                        + "Accepted extensions: .txt, .md, .csv and .xlsx")
        private String priceHistoryFile;

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
         * Set the report language.
         */
        @CommandLine.Option(
                names = {"-L", "--language"},
                description = "Two-letter ISO-639-1 language code that controls the report language. Default: EN.",
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
         * Set the timezone.
         */
        @CommandLine.Option(
                names = {"-Z", "--timezone"},
                description = "The timezone of the dates, e.g. \"GMT+2\", \"Europe/Budapest\" "
                        + "Default: the system default time-zone")
        private String zone = ZoneId.systemDefault().getId();
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private PriceArgGroup() {
        throw new UnsupportedOperationException();
    }
}