package com.remal.portfolio.picocli.command;

import com.remal.portfolio.picocli.converter.StringToListConverter;
import com.remal.portfolio.util.FileWriter;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Command line interface group for defining output options.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public abstract class OutputCommandGroup {

    /**
     * CLI Group definition for configuring the output.
     */
    @CommandLine.ArgGroup(
            heading = "%nOutput:%n",
            exclusive = false)
    final OutputArgGroup outputArgGroup = new OutputArgGroup();

    /**
     * Output CLI parameters.
     */
    public static class OutputArgGroup {

        /**
         * Set the output file name.
         */
        @CommandLine.Option(
                names = {"-o", "--output-file"},
                description = "Write report to file (i.e. \"'report_'yyyy-MM-dd'.md'\"). "
                        + "Accepted extensions: .txt, .md, .csv and .xlsx")
        public String outputFile;

        /**
         *  Set the file open mode.
         */
        @CommandLine.Option(
                names = {"-w", "--file-mode"},
                description = "How to write the report to the file. Default: ${DEFAULT-VALUE}"
                        + "%n  Candidates: ${COMPLETION-CANDIDATES}",
                defaultValue = "STOP_IF_EXIST")
        public FileWriter.WriteMode writeMode;

        /**
         * Set it to true if you want to hide the report title.
         */
        @CommandLine.Option(
                names = {"-m", "--hide-title"},
                description = "Hide the report title.")
        public boolean hideTitle;

        /**
         * Set it to true if you want to hide the header of the table.
         */
        @CommandLine.Option(
                names = {"-n", "--hide-header"},
                description = "Hide the table header in the report.")
        public boolean hideHeader;

        /**
         * Set the report language.
         */
        @CommandLine.Option(
                names = {"-l", "--language"},
                description = "Two-letter ISO-639-1 language code that controls the report language. "
                        + "Default: ${DEFAULT-VALUE}",
                defaultValue = "EN")
        public String language;

        /**
         * List of the columns that won't be displayed in the reports.
         */
        @CommandLine.Option(
                names = {"-h", "--columns-to-hide"},
                description = "Comma separated list of column names that won't be displayed in the report."
                        + "%n  Candidates: PORTFOLIO, TICKER, TYPE, VALUATION, TRADE_DATE, QUANTITY, PRICE, FEE, "
                        + "CURRENCY, ORDER_ID, TRADE_ID, TRANSFER_ID",
                converter = StringToListConverter.class)
        public final List<String> columnsToHide = new ArrayList<>();
    }
}
