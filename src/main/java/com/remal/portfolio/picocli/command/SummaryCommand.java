package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.builder.SummaryBuilder;
import com.remal.portfolio.parser.Parse;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.util.LogLevel;
import com.remal.portfolio.writer.StdoutWriter;
import com.remal.portfolio.writer.SummaryWriter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * Implementation of the 'summary' command.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@CommandLine.Command(
        name = "summary",
        sortOptions = false,
        usageHelpAutoWidth = true,
        description = "Generates portfolio summary report.",
        descriptionHeading = "%n",
        optionListHeading = "%n",
        footerHeading = Main.FOOTER_HEADING,
        footer = Main.FOOTER)
@Slf4j
public class SummaryCommand implements Callable<Integer> {

    /**
     * In this mode the log file won't be written to the standard output.
     */
    @CommandLine.Option(names = {"-q", "--quiet"},
            description = "In this mode log wont be shown.")
    boolean quietMode;

    /**
     * An argument group definition to configure the input.
     */
    @CommandLine.ArgGroup(
            heading = "%nInput file:%n",
            exclusive = false,
            multiplicity = "1")
    private final SourcesGroup sourcesGroup = new SourcesGroup();

    /**
     *  Input data related arguments.
     */
    private static class SourcesGroup {

        /**
         * CLI definition: set the source files.
         */
        @CommandLine.Option(
                names = {"-i", "--input-file"},
                description = "File that contains the transactions.",
                required = true)
        String inputFile;

        /**
         * CLI definition: set the timestamp that used in the reports.
         */
        @CommandLine.Option(
                names = {"-a", "--in-date-pattern"},
                description = "\"Timestamp pattern that is used to parse the input file."
                        + "%n  Default: \"${DEFAULT-VALUE}\"",
                defaultValue = "yyyy-MM-dd HH:mm:ss")
        String dateTimePattern;
    }

    /**
     * Report configuration command line parameters.
     */
    @CommandLine.ArgGroup(
            heading = "%nFilter:%n",
            exclusive = false)
    private final FilterGroup filterGroup = new FilterGroup();

    /**
     * Report configuration parameters.
     */
    private static class FilterGroup {

        /**
         * CLI definition: set the portfolio name filter.
         */
        @CommandLine.Option(
                names = {"-p", "--portfolio"},
                description = "Portfolio name filter.")
        String portfolio;

        /**
         * CLI definition: set the product name filter.
         */
        @CommandLine.Option(
                names = {"-t", "--ticker"},
                description = "Product name (ticker) filter.")
        String ticker;
    }

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
                description = "Output file, e.g : \"'report_'yyyy-MM-dd'.md'\".%n"
                        + "Accepted extensions: .txt, .md")
        public String outputFile;

        /**
         *  CLI definition: set the file write mode.
         */
        @CommandLine.Option(
                names = {"-w", "--write-mode"},
                description = "Used only in case of file output mode. It determines %n"
                        + "what constitutes a conflict and what the overwrite strategy is."
                        + "%n  Candidates: ${COMPLETION-CANDIDATES}"
                        + "%n  Default: ${DEFAULT-VALUE}",
                defaultValue = "STOP_IF_EXIST")
        public FileWriter.WriteMode fileWriteMode;

        /**
         * CLI definition: set the report language.
         */
        @CommandLine.Option(
                names = {"-l", "--language"},
                description = "Language of the report, ISO 639-1 code."
                        + "%n  Candidates: EN, HU, etc."
                        + "%n  Default: ${DEFAULT-VALUE}",
                defaultValue = "EN")
        public String language;

        /**
         * CLI definition: show the relevant transactions in the report.
         */
        @CommandLine.Option(
                names = {"-r", "--show-transactions"},
                description = "Show relevant transactions."
                        + "%n  Candidates: true, false"
                        + "%n  Default: ${DEFAULT-VALUE}",
                defaultValue = "false")
        public String showTransactions;

        /**
         * CLI definition: show the transaction history in the report.
         */
        @CommandLine.Option(
                names = {"-s", "--show-transaction-history"},
                description = "Show transaction history."
                        + "%n  Candidates: true, false"
                        + "%n  Default: ${DEFAULT-VALUE}",
                defaultValue = "false")
        public String showTransactionHistory;
    }

    /**
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        LogLevel.configureLogger(quietMode);

        var transactions = Parse.file(sourcesGroup.inputFile, sourcesGroup.dateTimePattern);
        var builder = new SummaryBuilder(transactions, filterGroup.portfolio, filterGroup.ticker);
        var report = builder.build();

        var writer = SummaryWriter.build(report, outputGroup.language, sourcesGroup.dateTimePattern);
        writer.setShowTransactions(Boolean.parseBoolean(outputGroup.showTransactions));
        writer.setShowTransactionHistory(Boolean.parseBoolean(outputGroup.showTransactionHistory));

        if (outputGroup.outputFile == null) {
            StdoutWriter.debug(quietMode, writer.printAsMarkdown());
        } else {
            writer.writeToFile(outputGroup.fileWriteMode, outputGroup.outputFile);
        }

        return CommandLine.ExitCode.OK;
    }
}
