package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.parser.Parse;
import com.remal.portfolio.util.LogLevel;
import com.remal.portfolio.writer.StdoutWriter;
import com.remal.portfolio.writer.TransactionWriter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Implementation of the 'summary' command.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@CommandLine.Command(
        name = "show",
        sortOptions = false,
        usageHelpAutoWidth = true,
        description = "Shows transactions.",
        descriptionHeading = "%n",
        optionListHeading = "%n",
        footerHeading = Main.FOOTER_HEADING,
        footer = Main.FOOTER)
@Slf4j
public class ShowCommand extends CommonCommand implements Callable<Integer> {

    /**
     * An argument group definition to configure the input.
     */
    @CommandLine.ArgGroup(
            heading = "%nInput:%n",
            exclusive = false,
            multiplicity = "1")
    private final SourceGroup sourceGroup = new SourceGroup();

    /**
     *  Input data related arguments.
     */
    private static class SourceGroup {

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
                description = "Timestamp pattern that is used to parse the input file."
                        + "%n  Default: \"${DEFAULT-VALUE}\"",
                defaultValue = "yyyy-MM-dd HH:mm:ss")
        String dateTimePattern;

        /**
         * CLI definition: set the list of the portfolio names that will be
         * overridden during the parse.
         */
        @CommandLine.Option(
                names = {"-r", "--replace"},
                description = "Replace portfolio name.%n"
                        + "  E.g.: \"default:coinbase, manual:interactive-brokers\"",
                split = ",")
        final List<String> replaces = new ArrayList<>();
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
                names = {"-c", "--ticker"},
                description = "Product name (ticker) filter.")
        String ticker;
    }

    /**
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        LogLevel.configureLogger(quietMode);

        var transactions = Parse.file(sourceGroup.inputFile, sourceGroup.dateTimePattern);
        transactions = transactions
                .stream()
                .filter(x -> filterGroup.portfolio == null || x.getPortfolio().equals(filterGroup.portfolio))
                .filter(x -> filterGroup.ticker == null || x.getTicker().equals(filterGroup.ticker))
                .collect(Collectors.toList());

        var writer = TransactionWriter.build(transactions, outputGroup, sourceGroup.replaces);
        if (outputGroup.outputFile == null) {
            StdoutWriter.debug(quietMode, writer.printAsMarkdown());
        } else {
            writer.writeToFile(outputGroup.fileWriteMode, outputGroup.outputFile);
        }

        return CommandLine.ExitCode.OK;
    }
}
