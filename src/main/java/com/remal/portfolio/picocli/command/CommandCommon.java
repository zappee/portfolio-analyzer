package com.remal.portfolio.picocli.command;

import com.remal.portfolio.i18n.Header;
import com.remal.portfolio.model.Language;
import com.remal.portfolio.picocli.converter.HeaderConverter;
import com.remal.portfolio.util.FileWriter;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * Template command-line interface that contains common CLI parameters.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public abstract class CommandCommon {

    /**
     * In this mode the log file won't be written to the standard output.
     */
    @Option(names = {"-q", "--quiet"},
            description = "In this mode log wont be shown.")
    boolean quietMode;

    /**
     * CLI definition: set the list of the portfolio names that will be
     * overridden during the parse.
     */
    @CommandLine.Option(
            names = {"-r", "--replace"},
            description = "Replace portfolio name.%n"
                    + "  E.g.: 'default:coinbase, manual:interactive-brokers'",
            split = ",")
    final List<String> replaces = new ArrayList<>();


    /**
     * An argument group definition for the output.
     */
    @CommandLine.ArgGroup(
            heading = "%nOutput:%n",
            exclusive = false)
    final OutputGroup outputGroup = new OutputGroup();

    /**
     * Option list definition for output.
     */
    public static class OutputGroup {

        /**
         * CLI definition: set the output file name.
         */
        @CommandLine.Option(
                names = {"-o", "--output-filename"},
                description = "Output file, e.g : \"'coinbase-pro_'yyyy-MM-dd'.md'\".%n"
                        + "Accepted extensions: .txt, .md and .xls")
        public String outputFile;

        /**
         *  CLI definition: set the file write mode.
         */
        @CommandLine.Option(
                names = {"-w", "--write-mode"},
                description = "Determines what constitutes a conflict and what the overwrite strategy is."
                        + "%n  Candidates: ${COMPLETION-CANDIDATES}"
                        + "%n  Default: ${DEFAULT-VALUE}",
                defaultValue = "STOP_IF_EXIST")
        public FileWriter.WriteMode fileWriteMode;

        /**
         * CLI definition: set it to true if you want to add header to the
         * report.
         */
        @CommandLine.Option(
                names = {"-h", "--print-header"},
                description = "Print header while exporting the report."
                        + "%n  Candidates: true, false"
                        + "%n  Default: true",
                defaultValue = "true")
        public String printHeader;

        /**
         * CLI definition: set the report language.
         */
        @CommandLine.Option(
                names = {"-l", "--language"},
                description = "Language of the report (ISO 639-1)."
                        + "%n  Candidates: ${COMPLETION-CANDIDATES}"
                        + "%n  Default: ${DEFAULT-VALUE}",
                defaultValue = "EN")
        public Language language;

        /**
         * CLI definition: set the list of the columns that won't be displayed
         * in the reports. The column names comes from the Header class.
         */
        @CommandLine.Option(
                names = {"-d", "--columns-to-hide"},
                description = "Comma separated list of column names that won't be displayed in the report"
                        + "%n  Candidates: ${COMPLETION-CANDIDATES}",
                converter = HeaderConverter.class)
        public final List<Header> columnsToHide = new ArrayList<>();

        /**
         * CLI definition: set the timestamp that used in the reports.
         */
        @Option(
                names = {"-t", "--out-date-pattern"},
                description = "Timestamp pattern used in the reports."
                        + "%n  Default: \"${DEFAULT-VALUE}\"",
                defaultValue = "yyyy-MM-dd HH:mm:ss")
        public String dateTimePattern;
    }
}
