package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.downloader.MarketPriceDownloader;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.util.Calendars;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.ZoneIds;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * Implementation of the 'price' command.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@CommandLine.Command(
        name = "price",
        sortOptions = false,
        usageHelpAutoWidth = true,
        description = "Get the price of a stock.",
        descriptionHeading = "%n",
        optionListHeading = "%n",
        footerHeading = Main.FOOTER_HEADING,
        footer = Main.FOOTER)
@Slf4j
public class PriceCommand implements Callable<Integer> {

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
     * Data providerType configuration.
     */
    @CommandLine.ArgGroup(
            exclusive = false,
            multiplicity = "1",
            heading = "%nInput:%n")
    private final PriceArgGroup.InputArgGroup inputArgGroup = new PriceArgGroup.InputArgGroup();

    /**
     * Output configuration.
     */
    @CommandLine.ArgGroup(
            heading = "%nOutput:%n",
            exclusive = false)
    private final PriceArgGroup.OutputArgGroup outputArgGroup = new PriceArgGroup.OutputArgGroup();

    /**
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        Logger.setSilentMode(quietMode);
        log.info("executing the 'price' command...");
        ZoneIds.validate(outputArgGroup.getZone());

        var priceDownloader = new MarketPriceDownloader(priceHistoryFile, inputArgGroup, outputArgGroup);
        var tradeDate = Calendars.fromString(inputArgGroup.getTradeDate(), inputArgGroup.getDateTimePattern());
        var price = priceDownloader.getMarketPrice(inputArgGroup.getSymbol(), tradeDate);

        if (price.isEmpty()) {
            Logger.logErrorAndExit("Price not found.");
        }

        return CommandLine.ExitCode.OK;
    }
}
