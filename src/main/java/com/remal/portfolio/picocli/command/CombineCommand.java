package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.parser.Parse;
import com.remal.portfolio.util.LogLevel;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import com.remal.portfolio.writer.StdoutWriter;
import com.remal.portfolio.writer.TransactionWriter;
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
public class CombineCommand extends CommonCommand implements Callable<Integer> {

    /**
     * An argument group definition for the input files.
     */
    @CommandLine.ArgGroup(
            heading = "%nInput:%n",
            exclusive = false,
            multiplicity = "1")
    private final SourceGroup sourceGroup = new SourceGroup();

    /**
     * Option list definition for data sources.
     */
    private static class SourceGroup {

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
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        LogLevel.configureLogger(quietMode);

        // combine transactions from the source with data from another sources
        var overwrite = Boolean.parseBoolean(sourceGroup.overwrite);
        List<Transaction> transactions = new ArrayList<>();
        sourceGroup.filesToCombine.forEach(x -> combine(
                Parse.file(Strings.patternToString(x.trim()), sourceGroup.dateTimePattern),
                transactions,
                overwrite)
        );

        // print to output
        Sorter.sort(transactions);
        var writer = TransactionWriter.build(transactions, outputGroup, sourceGroup.replaces);
        if (outputGroup.outputFile == null) {
            StdoutWriter.debug(quietMode, writer.printAsMarkdown());
        } else {
            writer.writeToFile(outputGroup.fileWriteMode, outputGroup.outputFile);
        }

        return CommandLine.ExitCode.OK;
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
                tr.setTradeDate(sourceTr.getTradeDate());
                tr.setQuantity(sourceTr.getQuantity());
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
