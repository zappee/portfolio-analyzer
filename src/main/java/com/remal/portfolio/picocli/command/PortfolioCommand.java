package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.downloader.MarketPriceDownloader;
import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.PortfolioReport;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.parser.TransactionParser;
import com.remal.portfolio.picocli.arggroup.PortfolioArgGroup;
import com.remal.portfolio.picocli.arggroup.PortfolioInputArgGroup;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.PortfolioNameRenamer;
import com.remal.portfolio.util.ZoneIds;
import com.remal.portfolio.writer.PortfolioWriter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Implementation of the 'summary' command.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
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
                    + "Accepted extensions: .txt, .md and .csv")
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
        log.info("executing the 'portfolio' command...");

        inputArgGroup.setZone(ZoneIds.getDefaultIfEmpty(inputArgGroup.getZone()));
        outputArgGroup.setZone(ZoneIds.getDefaultIfEmpty(outputArgGroup.getZone()));

        Logger.logQuietMode(log, quietMode);
        Logger.logPriceHistoryFile(log, priceHistoryFile);
        Logger.logInput(log, inputArgGroup);
        Logger.logOutput(log, outputArgGroup);

        // validating the inputs
        CurrencyType.abortIfInvalid(outputArgGroup.getBaseCurrency());

        // parser
        Parser<Transaction> parser = TransactionParser.build(inputArgGroup);
        var inputZone = ZoneId.of(inputArgGroup.getZone());
        var transactionsFile = LocalDateTimes.toString(inputZone, inputArgGroup.getFile(), LocalDateTime.now());
        var transactions = parser.parse(transactionsFile);
        PortfolioNameRenamer.rename(transactions, outputArgGroup.getReplaces());

        // generate the report
        var currency = CurrencyType.getEnum(outputArgGroup.getBaseCurrency());
        var generated = Objects.isNull(inputArgGroup.getTo())
                ? LocalDateTimes.getNow(ZoneId.of(outputArgGroup.getZone()))
                : LocalDateTimes.toLocalDateTime(inputZone, inputArgGroup.getDateTimePattern(), inputArgGroup.getTo());
        var portfolioReport = new PortfolioReport(currency, generated);
        portfolioReport.addTransactions(transactions);

        // set market prices
        var marketPriceDownloader = new MarketPriceDownloader(priceHistoryFile, inputArgGroup, outputArgGroup);
        var marketPriceAt = LocalDateTimes.toLocalDateTime(
                ZoneId.of(inputArgGroup.getZone()),
                inputArgGroup.getDateTimePattern(),
                inputArgGroup.getTo());

        var zone = ZoneId.of(outputArgGroup.getZone());
        var now = LocalDateTime.now();
        var dataProviderFile = LocalDateTimes.toString(zone, inputArgGroup.getDataProviderFile(), now);
        if (Objects.nonNull(dataProviderFile) && Files.exists(Path.of(dataProviderFile))) {
            marketPriceDownloader.updateMarketPrices(portfolioReport, marketPriceAt);
        } else {
            log.warn("skipping market data price calculation because the data-provider-file is empty or it does not "
                    + "exist.");
        }

        // writer
        var portfolioReportFile = LocalDateTimes.toString(zone, outputArgGroup.getPortfolioReportFile(), now);
        var portfolioSummaryFile = LocalDateTimes.toString(zone, outputArgGroup.getPortfolioSummaryFile(), now);
        var writer = PortfolioWriter.build(inputArgGroup, outputArgGroup);
        writer.write(outputArgGroup.getWriteMode(), portfolioSummaryFile, portfolioReport);
        writer.writePortfolioReport(outputArgGroup.getWriteMode(), portfolioReportFile, portfolioReport);
        return CommandLine.ExitCode.OK;
    }
}
