package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.downloader.Downloader;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.model.ProviderType;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.util.Calendars;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.writer.PriceWriter;
import com.remal.portfolio.writer.Writer;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
     * Supported price downloader modules.
     */
    private final Map<ProviderType, Downloader> downloader;

    /**
     * Constructor that initializes the price downloader objects.
     */
    public PriceCommand() {
        downloader = Downloader.initializeDownloader();
    }

    /**
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        Logger.setSilentMode(quietMode);

        var ticker = inputArgGroup.getTicker();
        var providerFile = inputArgGroup.getProviderArgGroup().getProviderFile();
        var provider = Stream.<Supplier<ProviderType>>of(
                        () -> inputArgGroup.getProviderArgGroup().getProviderType(),
                        () -> ProviderType.getProvider(ticker, providerFile))
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        var tickerAlias = ProviderType.getTicker(ticker, providerFile);
        var productPrice = getPrice(tickerAlias, provider);
        productPrice.ifPresent(p -> p.setTicker(ticker));

        // read the history file
        List<Price> prices = new ArrayList<>();
        var historyFile = outputArgGroup.getPriceHistoryFile();
        if (Objects.nonNull(historyFile)) {
            Parser<Price> parser = Parser.build(outputArgGroup);
            prices.addAll(parser.parse(historyFile));
        }

        // merge
        Price.merge(prices, productPrice.orElse(null), outputArgGroup.getMultiplicity());

        // writer
        Writer<Price> writer = PriceWriter.build(outputArgGroup);
        writer.write(outputArgGroup.getWriteMode(), outputArgGroup.getPriceHistoryFile(), prices);

        return CommandLine.ExitCode.OK;
    }

    /**
     * Download the price from data provider.
     *
     * @param ticker       the company's ticker
     * @param providerType trading data provider
     * @return             the market price of the company
     */
    private Optional<Price> getPrice(String ticker, ProviderType providerType) {
        if (Objects.nonNull(providerType)) {
            var tradeDate = inputArgGroup.getTradeDate();
            var pattern = inputArgGroup.getDateTimePattern();
            return Objects.isNull(tradeDate)
                    ? downloader.get(providerType).getPrice(ticker)
                    : downloader.get(providerType).getPrice(ticker, Calendars.fromString(tradeDate, pattern));
        } else {
            Logger.logErrorAndExit("Market price data provider can not be empty.");
            return Optional.empty();
        }
    }
}
