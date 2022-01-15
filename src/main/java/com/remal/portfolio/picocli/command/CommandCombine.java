package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.parser.OutputProducer;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.parser.csv.CsvParser;
import com.remal.portfolio.parser.excel.ExcelParser;
import com.remal.portfolio.parser.markdown.MarkdownParser;
import com.remal.portfolio.util.Files;
import com.remal.portfolio.util.LogLevel;
import com.remal.portfolio.util.Sorter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Implementation of the 'combine' command.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@CommandLine.Command(
        name = "combine",
        sortOptions = false,
        usageHelpAutoWidth = true,
        description = "Combine transactions coming from different sources.",
        descriptionHeading = "%n",
        optionListHeading = "%n",
        footerHeading = Main.FOOTER_HEADING,
        footer = Main.FOOTER)
@Slf4j
public class CommandCombine extends CommandCommon implements Callable<Integer> {

    /**
     * An argument group definition for the input files.
     */
    @CommandLine.ArgGroup(
            heading = "%nInput file(s):%n",
            exclusive = false,
            multiplicity = "1")
    private final SourcesGroup sourcesGroup = new SourcesGroup();

    /**
     * Option list definition for data sources.
     */
    private static class SourcesGroup {

        /**
         * CLI definition: set the source files.
         */
        @CommandLine.Option(
                names = {"-i", "--input-files"},
                description = "Comma separated list of files with transactions to be combined.",
                split = ",")
        final List<String> filesToCombine = Collections.emptyList();

        /**
         * CLI definition: set the timestamp that used in the reports.
         */
        @CommandLine.Option(
                names = {"-a", "--in-date-pattern"},
                description = "Timestamp pattern used in the input files."
                        + "%n  Default: \"${DEFAULT-VALUE}\"",
                defaultValue = "yyyy-MM-dd HH:mm:ss")
        String dateTimePattern;

        /**
         * CLI definition: set the transaction overwrite mode.
         */
        @CommandLine.Option(
                names = {"-e", "--overwrite"},
                description = "Overwrite the transactions while combining them."
                        + "%n  Candidates: true, false"
                        + "%n  Default: false",
                defaultValue = "false")
        String overwrite;
    }

    /**
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        LogLevel.configureLogger(quietMode);

        // combine transactions from the source with data from another sources
        var overwrite = Boolean.parseBoolean(sourcesGroup.overwrite);
        List<Transaction> transactions = new ArrayList<>();
        sourcesGroup.filesToCombine.forEach(x -> combine(readTransactionsFromFile(x.trim()), transactions, overwrite));

        // rename portfolio name
        OutputProducer.renamePortfolioNames(replaces, transactions);

        // print to output
        Sorter.sort(transactions);
        OutputProducer.writeTransactions(transactions, quietMode, outputGroup);
        return CommandLine.ExitCode.OK;
    }

    /**
     * Extracts the transactions from the given file.
     *
     * @param sourceFile path to the file
     * @return list of the transactions
     */
    private List<Transaction> readTransactionsFromFile(String sourceFile) {
        var filetype = Files.getFileType(sourceFile);
        Parser parser;

        switch (filetype) {
            case TEXT:
                parser = new MarkdownParser(sourceFile, sourcesGroup.dateTimePattern);
                return parser.parse();

            case CSV:
                parser = new CsvParser(sourceFile, sourcesGroup.dateTimePattern);
                return parser.parse();

            case EXCEL:
                parser = new ExcelParser(sourceFile, sourcesGroup.dateTimePattern);
                return parser.parse();

            default:
                log.error("Unsupported input file type: '{}'", sourceFile);
                System.exit(CommandLine.ExitCode.SOFTWARE);
                return Collections.emptyList();
        }
    }

    /**
     * Combine source and target transactions.
     * @param source source transactions
     * @param target target transactions
     * @param overwrite set it to true if you want to overwrite the target transactions
     */
    private void combine(List<Transaction> source, List<Transaction> target, boolean overwrite) {
        source.forEach(sourceTr -> {
            Optional<Transaction> targetTr = target
                    .stream()
                    .filter(x -> Objects.equals(sourceTr.getTransferId(), x.getTransferId())
                            && Objects.equals(sourceTr.getOrderId(), x.getOrderId())
                            && Objects.equals(sourceTr.getTradeId(), x.getTradeId()))
                    .findAny();

            if (targetTr.isPresent() && overwrite) {
                var tr = targetTr.get();
                tr.setPortfolio(sourceTr.getPortfolio());
                tr.setType(sourceTr.getType());
                tr.setCreated(sourceTr.getCreated());
                tr.setVolume(sourceTr.getVolume());
                tr.setPrice(sourceTr.getPrice());
                tr.setFee(sourceTr.getFee());
                tr.setCurrency(sourceTr.getCurrency());
                tr.setTicker(sourceTr.getTicker());
                tr.setTransferId(sourceTr.getTransferId());
                tr.setTradeId(sourceTr.getTradeId());
                tr.setOrderId(sourceTr.getOrderId());
            } else if (targetTr.isEmpty()) {
                target.add(sourceTr);
            }
        });
    }
}
