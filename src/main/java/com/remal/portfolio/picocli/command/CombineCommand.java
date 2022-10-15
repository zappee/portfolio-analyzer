package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.parser.TransactionParser;
import com.remal.portfolio.picocli.arggroup.CombineInputArgGroup;
import com.remal.portfolio.picocli.arggroup.OutputArgGroup;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.writer.TransactionWriter;
import com.remal.portfolio.writer.Writer;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

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
    private final CombineInputArgGroup inputArgGroup = new CombineInputArgGroup();

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
        log.debug("< overwrite mode: {}", overwrite ? "overwrite" : "skip if exist");
        final List<Transaction> transactions = new ArrayList<>();

        // parser
        var zone = ZoneId.of(outputArgGroup.getZone());
        var parser = TransactionParser.build(inputArgGroup);
        inputArgGroup.getFiles().forEach(filenameTemplate -> {
            var filename = LocalDateTimes.toString(zone, filenameTemplate, LocalDateTime.now());
            combine(parser.parse(filename), transactions, overwrite);
        });

        // writer
        var outFilenameTemplate = outputArgGroup.getOutputFile();
        var outFilename = LocalDateTimes.toString(zone, outFilenameTemplate, LocalDateTime.now());

        Writer<Transaction> writer = TransactionWriter.build(outputArgGroup);
        writer.write(outputArgGroup.getWriteMode(), outFilename, transactions);
        return CommandLine.ExitCode.OK;
    }

    /**
     * Combine source and target transactions.
     *
     * @param source source transactions
     * @param target target transactions
     * @param overwrite set it to true if you want to overwrite the existing transactions in the target side
     */
    private void combine(List<Transaction> source, List<Transaction> target, boolean overwrite) {
        var overwritten = new AtomicLong();
        source.forEach(sourceCurrent -> {
            Optional<Transaction> found = target
                    .stream()
                    .filter(targetCurrent -> Filter.transactionIdFilter(sourceCurrent, targetCurrent))
                    .filter(actual -> Filter.symbolFilter(inputArgGroup.getSymbols(), actual))
                    .findAny();

            if (found.isPresent() && overwrite) {
                overwritten.incrementAndGet();
                var tr = found.get();
                tr.setPortfolio(sourceCurrent.getPortfolio());
                tr.setType(sourceCurrent.getType());
                tr.setTradeDate(sourceCurrent.getTradeDate());
                tr.setQuantity(sourceCurrent.getQuantity());
                tr.setPrice(sourceCurrent.getPrice());
                tr.setFee(sourceCurrent.getFee());
                tr.setCurrency(sourceCurrent.getCurrency());
                tr.setSymbol(sourceCurrent.getSymbol());
                tr.setTransferId(sourceCurrent.getTransferId());
                tr.setTradeId(sourceCurrent.getTradeId());
                tr.setOrderId(sourceCurrent.getOrderId());
            } else if (found.isEmpty()) {
                target.add(sourceCurrent);
            }
        });

        log.info("> number of overwritten transactions: {}", overwritten.get());
    }
}
