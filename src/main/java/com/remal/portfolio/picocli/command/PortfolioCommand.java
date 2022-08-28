package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.downloader.MarketPriceDownloader;
import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.PortfolioReport;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.picocli.arggroup.PortfolioArgGroup;
import com.remal.portfolio.picocli.arggroup.PortfolioInputArgGroup;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.PortfolioNameRenamer;
import com.remal.portfolio.writer.PortfolioWriter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
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
        name = "portfolio",
        sortOptions = false,
        usageHelpAutoWidth = true,
        description = "Generates portfolio summary report.",
        descriptionHeading = "%n",
        optionListHeading = "%n",
        footerHeading = Main.FOOTER_HEADING,
        footer = Main.FOOTER)
@Slf4j
public class PortfolioCommand implements Callable<Integer> {

    /**
     * In this mode the log file won't be written to the standard output.
     */
    @CommandLine.Option(names = {"-q", "--quiet"},
            description = "In this mode log wont be shown.")
    boolean quietMode;

    /**
     * Set the price history file.
     */
    @CommandLine.Option(
            names = {"-P", "--price-history"},
            description = "Storing the price in a file, e.g. \"'price_'yyyy'.md'\". "
                    + "Accepted extensions: .txt, .md, .csv and .xlsx")
    private String priceHistoryFile;

    /**
     * An argument group definition to configure the input.
     */
    @CommandLine.ArgGroup(
            exclusive = false,
            multiplicity = "1",
            heading = "%nInput:%n")
    private final PortfolioInputArgGroup inputArgGroup = new PortfolioInputArgGroup();

    /**
     * An argument group definition for the output.
     */
    @CommandLine.ArgGroup(
            heading = "%nOutput:%n",
            exclusive = false)
    final PortfolioArgGroup.OutputArgGroup outputArgGroup = new PortfolioArgGroup.OutputArgGroup();

    /**
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        Logger.setSilentMode(quietMode);

        // validating the inputs
        CurrencyType.abortIfInvalid(outputArgGroup.getBaseCurrency());

        // parser
        Parser<Transaction> parser = Parser.build(inputArgGroup);
        var inputZone = ZoneId.of(inputArgGroup.getZone());
        var transactionsFile = LocalDateTimes.toString(inputZone, inputArgGroup.getFile(), LocalDateTime.now());
        var transactions = parser.parse(transactionsFile);
        PortfolioNameRenamer.rename(transactions, outputArgGroup.getReplaces());
        transactions = transactions
                .stream()
                .filter(x -> Filter.portfolioNameFilter(inputArgGroup.getPortfolio(), x))
                .toList();

        // generate the report
        var currency = CurrencyType.getEnum(outputArgGroup.getBaseCurrency());
        var portfolioReport = new PortfolioReport(currency);
        portfolioReport.addTransactions(transactions);

        // set market prices
        var marketPriceDownloader = new MarketPriceDownloader(priceHistoryFile, inputArgGroup, outputArgGroup);
        var zoneId = ZoneId.of(inputArgGroup.getZone());
        var marketPriceAt = LocalDateTimes.toLocalDateTime(
                zoneId,
                inputArgGroup.getDateTimePattern(),
                inputArgGroup.getTo());
        marketPriceDownloader.updateMarketPrices(portfolioReport, marketPriceAt);

        // writer
        var templateFilename = outputArgGroup.getOutputFile();
        var zone = ZoneId.of(outputArgGroup.getZone());
        var outFile = LocalDateTimes.toString(zone, templateFilename, LocalDateTime.now());
        var writer = PortfolioWriter.build(inputArgGroup, outputArgGroup);
        writer.write(outputArgGroup.getWriteMode(), outFile, Collections.singletonList(portfolioReport));
        return CommandLine.ExitCode.OK;
    }
}
