package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.generator.SummaryGenerator;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.picocli.arggroup.SummaryArgGroup;
import com.remal.portfolio.picocli.arggroup.SummaryInputArgGroup;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.PortfolioNameRenamer;
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
            exclusive = false,
            multiplicity = "1",
            heading = "%nInput:%n")
    private final SummaryInputArgGroup inputArgGroup = new SummaryInputArgGroup();

    /**
     * An argument group definition for the output.
     */
    @CommandLine.ArgGroup(
            heading = "%nOutput:%n",
            exclusive = false)
    final SummaryArgGroup.OutputArgGroup outputArgGroup = new SummaryArgGroup.OutputArgGroup();

    /**
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        Logger.setSilentMode(quietMode);

        // parser
        Parser<Transaction> parser = Parser.build(inputArgGroup);
        var transactions = parser.parse(inputArgGroup.getFile());
        PortfolioNameRenamer.rename(transactions, outputArgGroup.getReplaces());
        transactions = transactions
                .stream()
                .filter(t -> Filter.portfolioNameFilter(inputArgGroup.getPortfolio(), t))
                .filter(t -> Filter.tickerFilter(inputArgGroup.getTickers(), t))
                .toList();

        // generate the report
        var builder = new SummaryGenerator(transactions, inputArgGroup.getPortfolio(), inputArgGroup.getTickers());
        var report = builder.build();

        // writer
        /*var writer = SummaryWriter.build(report, outputGroup.language, sourcesGroup.dateTimePattern);
        writer.setShowTransactions(Boolean.parseBoolean(outputGroup.showTransactions));
        writer.setShowTransactionHistory(Boolean.parseBoolean(outputGroup.showTransactionHistory));

        if (outputGroup.outputFile == null) {
            StdoutWriter.debug(quietMode, writer.printAsMarkdown());
        } else {
            writer.writeToFile(outputGroup.fileWriteMode, outputGroup.outputFile);
        }*/

        return CommandLine.ExitCode.OK;
    }
}
