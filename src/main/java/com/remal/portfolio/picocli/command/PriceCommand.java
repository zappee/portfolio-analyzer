package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.downloader.Downloader;
import com.remal.portfolio.downloader.coinbasepro.CoinbaseProDownloader;
import com.remal.portfolio.downloader.yahoo.YahooDownloader;
import com.remal.portfolio.model.ProductPrice;
import com.remal.portfolio.model.Provider;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.util.Calendars;
import com.remal.portfolio.util.Logger;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.remal.portfolio.picocli.arggroup.PriceArgGroup.CoinbaseDataSourceArgGroup;
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
     * The CommandSpec class models a command specification, including the
     * options, positional parameters and subcommands supported by the command,
     * as well as attributes for the version help message and the usage help
     * message of the command.
     */
    @CommandLine.Spec
    CommandLine.Model.CommandSpec commandSpec;

    /**
     * In this mode the log file won't be written to the standard output.
     */
    @CommandLine.Option(names = {"-q", "--quiet"},
            description = "In this mode log wont be shown.")
    boolean quietMode;

    /**
     * Data provider configuration.
     */
    @CommandLine.ArgGroup(
            exclusive = false,
            multiplicity = "1",
            heading = "%nInput:%n")
    private final InputArgGroup inputArgGroup = new InputArgGroup();

    /**
     * Coinbase PRO API configuration.
     */
    @CommandLine.ArgGroup(
            exclusive = false,
            multiplicity = "0",
            heading = "%nCoinbase PRO API:%n")
    private final CoinbaseDataSourceArgGroup coinbaseArgGroup = new CoinbaseDataSourceArgGroup();

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
    private final Map<Provider, Downloader> downloader;

    /**
     * Constructor that initializes the price downloader objects.
     */
    public PriceCommand() {
        var key = coinbaseArgGroup.getKey();
        var passphrase = coinbaseArgGroup.getPassphrase();
        var secret = coinbaseArgGroup.getSecret();
        downloader = new EnumMap<>(Provider.class);
        downloader.put(Provider.COINBASE_PRO, new CoinbaseProDownloader(key, passphrase, secret));
        downloader.put(Provider.YAHOO, new YahooDownloader());
    }

    /**
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        Logger.setSilentMode(quietMode);

        validateCoinbaseDataSourceArgGroup();
        var ticker = inputArgGroup.getTicker();
        var provider = Stream.<Supplier<Provider>>of(
                        () -> inputArgGroup.getProviderArgGroup().getProvider(),
                        () -> getProvider(ticker, inputArgGroup.getProviderArgGroup().getProviderFile()))
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findFirst();
        var productPrice = getPrice(ticker, provider.orElse(null));

        System.out.println(productPrice);
        // read the history file
   //     ProductPriceParser parser = Parser.build(inputArgGroup);
   //     List<ProductPrice> productPrices = parser.parse(outputArgGroup.getPriceHistoryFile());

        // merge
    //    productPrices.add(productPrice, outputArgGroup.getMultiplicity());

        // writer
     //   Writer<ProductPrice> writer = ProductPriceWriter.build(outputArgGroup);
     //   writer.write(outputArgGroup.getWriteMode(), outputArgGroup.getPriceHistoryFile(), productPrices);

        return CommandLine.ExitCode.OK;
    }

    /**
     * Validates the provided CLI parameters.
     *
     * @throws picocli.CommandLine.ParameterException missing input parameters
     */
    private void validateCoinbaseDataSourceArgGroup() {
        var apiKey = coinbaseArgGroup.getKey();
        var apiPassphrase = coinbaseArgGroup.getPassphrase();
        var apiSecret = coinbaseArgGroup.getSecret();

        if (inputArgGroup.getProviderArgGroup().getProvider() == Provider.COINBASE_PRO
                && (Objects.isNull(apiKey) || Objects.isNull(apiPassphrase) || Objects.isNull(apiSecret))) {

            var message = "Error: Missing required argument(s): (-k=<key> -p=<passphrase> -s=<secret>)";
            throw new CommandLine.ParameterException(commandSpec.commandLine(), message);
        }
    }

    /**
     * Get the data provider from the *.properties file.
     *
     * @param ticker the product id that represents the company's stock
     * @param file the configuration file with the provider names
     * @return the selected data provider
     */
    private Provider getProvider(String ticker, String file) {
        Provider provider = null;
        var providerAsString = "";

        try (InputStream inputStream = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(inputStream);

            providerAsString = properties.getProperty(ticker);
            if (Objects.isNull(providerAsString)) {
                var message = "There is no provider definition in the '{}' for ticker '{}'.";
                Logger.logErrorAndExit(message, file, ticker);
            }

            provider = Provider.valueOf(providerAsString);

        } catch (IOException e) {
            var message = "Error while reading the \"{}\" file. Error: {}";
            Logger.logErrorAndExit(message, file, e.toString());
        } catch (IllegalArgumentException e) {
            var message = "Invalid data provider is set for ticker '{}' in the '{}' file, provider: '{}'";
            Logger.logErrorAndExit(message, ticker, file, providerAsString);
        }
        return provider;
    }

    /**
     * Download the price from data provider.
     *
     * @param ticker the company's ticker
     * @param provider trading data provider
     * @return the market price of the company
     */
    private Optional<ProductPrice> getPrice(String ticker, Provider provider) {
        if (Objects.nonNull(provider)) {
            var tradeDate = inputArgGroup.getTradeDate();
            var pattern = inputArgGroup.getDateTimePattern();
            return Objects.isNull(tradeDate)
                    ? downloader.get(provider).getPrice(ticker)
                    : downloader.get(provider).getPrice(ticker, Calendars.fromString(tradeDate, pattern));
        } else {
            Logger.logErrorAndExit("Market price data provider can not be empty.");
            return Optional.empty();
        }
    }
}
