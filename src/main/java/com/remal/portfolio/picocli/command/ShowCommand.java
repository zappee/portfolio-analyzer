package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.picocli.arggroup.OutputArgGroup;
import com.remal.portfolio.picocli.arggroup.TransactionParserInputArgGroup;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.PortfolioNameRenamer;
import com.remal.portfolio.writer.TransactionWriter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

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

        // parser
        var parser = Parser.build(inputArgGroup);
        var transactions = parser.parse(inputArgGroup.getFile());
        PortfolioNameRenamer.rename(transactions, outputArgGroup.getReplaces());
        transactions = transactions
                .stream()
                .filter(t -> Filter.portfolioNameFilter(inputArgGroup.getPortfolio(), t))
                .filter(t -> Filter.tickerFilter(inputArgGroup.getTickers(), t))
                .toList();

        // writer
        var writer = TransactionWriter.build(outputArgGroup);
        writer.write(outputArgGroup.getWriteMode(), outputArgGroup.getOutputFile(), transactions);
        return CommandLine.ExitCode.OK;
    }
}
