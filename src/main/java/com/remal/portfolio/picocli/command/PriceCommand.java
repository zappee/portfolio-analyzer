package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.downloader.Downloader;
import com.remal.portfolio.downloader.coinbasepro.CoinbaseProDownloader;
import com.remal.portfolio.downloader.yahoo.YahooDownloader;
import com.remal.portfolio.model.MultiplicityType;
import com.remal.portfolio.model.ProductPrice;
import com.remal.portfolio.model.ProviderType;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.util.Calendars;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.writer.ProductPriceWriter;
import com.remal.portfolio.writer.Writer;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.remal.portfolio.picocli.arggroup.PriceArgGroup.InputArgGroup;
import static com.remal.portfolio.picocli.arggroup.PriceArgGroup.OutputArgGroup;

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
    private final InputArgGroup inputArgGroup = new InputArgGroup();

    /**
     * Output configuration.
     */
    @CommandLine.ArgGroup(
            heading = "%nOutput:%n",
            exclusive = false)
    private final OutputArgGroup outputArgGroup = new OutputArgGroup();

    /**
     * Supported price downloader modules.
     */
    private final Map<ProviderType, Downloader> downloader;

    /**
     * Constructor that initializes the price downloader objects.
     */
    public PriceCommand() {
        downloader = new EnumMap<>(ProviderType.class);
        downloader.put(ProviderType.COINBASE_PRO, new CoinbaseProDownloader());
        downloader.put(ProviderType.YAHOO, new YahooDownloader());
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
        var provider = Stream.<Supplier<ProviderType>>of(
                        () -> inputArgGroup.getProviderArgGroup().getProviderType(),
                        () -> getProvider(ticker, inputArgGroup.getProviderArgGroup().getProviderFile()))
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findFirst();
        var productPrice = getPrice(ticker, provider.orElse(null));
        List<ProductPrice> productPrices = new ArrayList<>();

        // read the history file
        var historyFile = outputArgGroup.getPriceHistoryFile();
        if (Objects.nonNull(historyFile)) {
            Parser<ProductPrice> parser = Parser.build(outputArgGroup);
            productPrices.addAll(parser.parse(outputArgGroup.getPriceHistoryFile()));
        }

        // merge
        merge(productPrices, productPrice.orElse(null), outputArgGroup.getMultiplicity());

        // writer
        Writer<ProductPrice> writer = ProductPriceWriter.build(outputArgGroup);
        writer.write(outputArgGroup.getWriteMode(), outputArgGroup.getPriceHistoryFile(), productPrices);

        return CommandLine.ExitCode.OK;
    }

    /**
     * Add a new price to the list based on  the provided multiplicity.
     *
     * @param productPrices the product price list
     * @param productPrice the current price to add to the list
     * @param multiplicity controls how to add the price to the list
     */
    private void merge(List<ProductPrice> productPrices, ProductPrice productPrice, MultiplicityType multiplicity) {
        var intervalEnd = LocalDateTime.now().atZone(ZoneId.of(outputArgGroup.getZone())).toLocalDateTime();
        var intervalStart = intervalEnd.minusSeconds(multiplicity.getRangeLengthInSec());
        var itemCount = productPrices
                .stream()
                .filter(x -> Filter.dateBetweenFilter(intervalStart, intervalEnd, x.getDate()))
                .count();

        log.debug("output > multiplicity: {}", multiplicity.name());
        log.debug("output > number of item within the range: {}", itemCount);

        if (multiplicity == MultiplicityType.MANY || itemCount == 0) {
            productPrices.add(productPrice);
        } else {
            log.warn("output > price wont be added to the output because of the multiplicity setting that you use");
        }
    }

    /**
     * Get the data providerType from the *.properties file.
     *
     * @param ticker the product id that represents the company's stock
     * @param file the configuration file with the providerType names
     * @return the selected data providerType
     */
    private ProviderType getProvider(String ticker, String file) {
        ProviderType providerType = null;
        var providerAsString = "";

        try (InputStream inputStream = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(inputStream);

            providerAsString = properties.getProperty(ticker);
            if (Objects.isNull(providerAsString)) {
                var message = "There is no providerType definition in the '{}' for ticker '{}'.";
                Logger.logErrorAndExit(message, file, ticker);
            }

            providerType = ProviderType.valueOf(providerAsString);

        } catch (IOException e) {
            var message = "Error while reading the \"{}\" file. Error: {}";
            Logger.logErrorAndExit(message, file, e.toString());
        } catch (IllegalArgumentException e) {
            var message = "Invalid data provider is set for ticker '{}' in the '{}' file, provider: '{}'";
            Logger.logErrorAndExit(message, ticker, file, providerAsString);
        }
        return providerType;
    }

    /**
     * Download the price from data provider.
     *
     * @param ticker the company's ticker
     * @param providerType trading data provider
     * @return the market price of the company
     */
    private Optional<ProductPrice> getPrice(String ticker, ProviderType providerType) {
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
