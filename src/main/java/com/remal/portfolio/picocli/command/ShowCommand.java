package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.parser.TransactionParser;
import com.remal.portfolio.picocli.arggroup.OutputArgGroup;
import com.remal.portfolio.picocli.arggroup.TransactionParserInputArgGroup;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.PortfolioNameRenamer;
import com.remal.portfolio.util.ZoneIds;
import com.remal.portfolio.writer.TransactionWriter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Callable;

/**
 * Implementation of the 'show' command.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@CommandLine.Command(
        name = "show",
        sortOptions = false,
        usageHelpAutoWidth = true,
        description = "Show transactions.",
        descriptionHeading = "%n",
        optionListHeading = "%n",
        footerHeading = Main.FOOTER_HEADING,
        footer = Main.FOOTER)
@Slf4j
public class ShowCommand implements Callable<Integer> {

    /**
     * In the silent mode the application performs actions without
     * displaying any details.
     */
    @CommandLine.Option(names = {"-s", "--silent"},
            description = "Perform actions without displaying any details.")
    private boolean silentMode;

    /**
     * Input CLI group.
     */
    @CommandLine.ArgGroup(
            exclusive = false,
            multiplicity = "1",
            heading = "%nInput:%n")
    private final TransactionParserInputArgGroup inputArgGroup = new TransactionParserInputArgGroup();

    /**
     * CLI Group definition for configuring the output.
     */
    @CommandLine.ArgGroup(
            heading = "%nOutput:%n",
            exclusive = false)
    private final OutputArgGroup outputArgGroup = new OutputArgGroup();

    /**
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        Logger.setSilentMode(silentMode);
        log.info("executing the 'show' command...");

        inputArgGroup.setZone(ZoneIds.getDefaultIfEmpty(inputArgGroup.getZone()));
        outputArgGroup.setZone(ZoneIds.getDefaultIfEmpty(outputArgGroup.getZone()));

        Logger.logInput(log, inputArgGroup);
        Logger.logOutput(log, outputArgGroup);

        // parser
        var parser = TransactionParser.build(inputArgGroup);
        var inputZone = ZoneId.of(inputArgGroup.getZone());
        var transactionsFile = LocalDateTimes.toString(inputZone, inputArgGroup.getFile(), LocalDateTime.now());
        var transactions = parser.parse(transactionsFile);
        PortfolioNameRenamer.rename(transactions, outputArgGroup.getReplaces());

        // writer
        var zone = ZoneId.of(outputArgGroup.getZone());
        var filename = LocalDateTimes.toString(zone, outputArgGroup.getOutputFile(), LocalDateTime.now());

        var writer = TransactionWriter.build(outputArgGroup);
        writer.write(outputArgGroup.getWriteMode(), filename, transactions);

        return CommandLine.ExitCode.OK;
    }
}
