package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.picocli.arggroup.CombineArgGroup;
import com.remal.portfolio.picocli.arggroup.OutputArgGroup;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.writer.TransactionWriter;
import com.remal.portfolio.writer.Writer;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Implementation of the 'combine' command.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
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
public class CombineCommand implements Callable<Integer> {

    /**
     * In this mode the log file won't be written to the standard output.
     */
    @CommandLine.Option(names = {"-q", "--quiet"},
            description = "In this mode log wont be shown.")
    private boolean quietMode;

    /**
     * Input CLI group.
     */
    @CommandLine.ArgGroup(
            exclusive = false,
            multiplicity = "1",
            heading = "%nInput:%n")
    private final CombineArgGroup inputArgGroup = new CombineArgGroup();

    /**
     * CLI Group definition for configuring the output.
     */
    @CommandLine.ArgGroup(
            heading = "%nOutput:%n",
            exclusive = false)
    final OutputArgGroup outputArgGroup = new OutputArgGroup();

    /**
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        Logger.setSilentMode(quietMode);

        var overwrite = inputArgGroup.isOverwrite();
        log.debug("overwrite mode: {}", overwrite ? "overwrite" : "skip if exist");
        final List<Transaction> transactions = new ArrayList<>();

        // parser
        var parser = Parser.build(inputArgGroup);
        inputArgGroup.getFiles().forEach(file -> combine(
                parser.parse(file),
                transactions,
                overwrite)
        );

        // writer
        Writer<Transaction> writer = TransactionWriter.build(outputArgGroup);
        writer.write(outputArgGroup.getWriteMode(), outputArgGroup.getOutputFile(), transactions);
        return CommandLine.ExitCode.OK;
    }

    /**
     * Combine source and target transactions.
     *
     * @param source source transactions
     * @param target target transactions
     * @param overwrite set it to true if you want to overwrite the target transactions
     */
    private void combine(List<Transaction> source, List<Transaction> target, boolean overwrite) {
        source.forEach(sourceTr -> {
            Optional<Transaction> targetTr = target
                    .stream()
                    .filter(actual -> Filter.transactionIdFilter(sourceTr, actual))
                    .filter(actual -> Filter.tickerFilter(inputArgGroup.getTickers(), actual))
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
