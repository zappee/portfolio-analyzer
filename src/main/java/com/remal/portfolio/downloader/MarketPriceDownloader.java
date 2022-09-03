package com.remal.portfolio.downloader;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.DataProviderType;
import com.remal.portfolio.model.PortfolioReport;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.parser.PriceParser;
import com.remal.portfolio.picocli.arggroup.PortfolioArgGroup;
import com.remal.portfolio.picocli.arggroup.PortfolioInputArgGroup;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.writer.PriceWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Market price downloader helper.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class MarketPriceDownloader {

    /**
     * Market data provider name from the command
     * line.
     */
    private final DataProviderType dataProviderFromCli;

    /**
     * Path to the market data provider configuration
     * file.
     */
    private final String dataProviderFile;

    /**
     * Path to the price history file.
     */
    private final String priceHistoryFile;

    /**
     * Time zone info.
     */
    private final ZoneId zone;

    /**
     * Parameters for parsing the price history file.
     */
    private final PortfolioArgGroup.OutputArgGroup priceHistoryWriterPrams;

    /**
     * Builder method.
     *
     * @param priceHistoryFile path to the price history file
     * @param inputArgGroup parameters from the command line
     * @param outputArgGroup parameters from the command line
     * @return the initialized downloader instance
     */
    public static MarketPriceDownloader build(String priceHistoryFile,
                                              PriceArgGroup.InputArgGroup inputArgGroup,
                                              PriceArgGroup.OutputArgGroup outputArgGroup) {

        var zone = ZoneId.of(outputArgGroup.getZone());
        var now = LocalDateTime.now();
        return new MarketPriceDownloader(
                zone,
                inputArgGroup.getDataProviderArgGroup().getDataProvider(),
                LocalDateTimes.toString(zone, inputArgGroup.getDataProviderArgGroup().getDataProviderFile(), now),
                LocalDateTimes.toString(zone, priceHistoryFile, now));
    }

    /**
     * Builder method.
     *
     * @param priceHistoryFile path to the price history file
     * @param inputArgGroup parameters from the command line
     * @param outputArgGroup parameters from the command line
     */
    public MarketPriceDownloader(String priceHistoryFile,
                                 PortfolioInputArgGroup inputArgGroup,
                                 PortfolioArgGroup.OutputArgGroup outputArgGroup) {

        var now = LocalDateTime.now();
        this.zone = ZoneId.of(outputArgGroup.getZone());
        this.dataProviderFromCli = null;
        this.dataProviderFile = LocalDateTimes.toString(zone, inputArgGroup.getDataProviderFile(), now);
        this.priceHistoryFile = LocalDateTimes.toString(zone, priceHistoryFile, now);
        this.priceHistoryWriterPrams = outputArgGroup;
    }

    /**
     * Constructor.
     *
     * @param zone time zone info
     * @param dataProviderFromCli market data provider name from the command line
     * @param dataProviderFile path to the market data provider configuration file
     * @param priceHistoryFile path to the price history file
     */
    public MarketPriceDownloader(ZoneId zone,
                                 DataProviderType dataProviderFromCli,
                                 String dataProviderFile,
                                 String priceHistoryFile) {
        this.zone = zone;
        this.dataProviderFromCli = dataProviderFromCli;
        this.dataProviderFile = dataProviderFile;
        this.priceHistoryFile = priceHistoryFile;
        this.priceHistoryWriterPrams = null;
    }

    /**
     * Set the market prices for currencies and products.
     *
     * @param portfolioReport the portfolio report
     * @param marketPriceAt the date of the market prices
     */
    public void updateMarketPrices(PortfolioReport portfolioReport, LocalDateTime marketPriceAt) {
        updateProductMarketPrice(portfolioReport, marketPriceAt);
        updateExchangeRates(portfolioReport, marketPriceAt);
        portfolioReport.updateProfitLoss();
    }

    /**
     * Update the exchange rates for currencies used in the portfolio report.
     *
     * @param portfolioReport the portfolio report
     * @param marketPriceAt the date of the market prices
     */
    private void updateExchangeRates(PortfolioReport portfolioReport, LocalDateTime marketPriceAt) {
        var baseCurrency = portfolioReport.getCurrency().name();
        portfolioReport.getCashInPortfolio()
                .entrySet()
                .stream()
                .filter(cashInPortfolioEntry -> !cashInPortfolioEntry.getKey().equals(baseCurrency))
                .forEach(cashInPortfolioEntry -> {
                    var exchangeRateSymbol = cashInPortfolioEntry.getKey() + "-" + baseCurrency;
                    var calendar = LocalDateTimes.toCalendar(marketPriceAt);
                    var exchangeRate = getMarketPrice(exchangeRateSymbol, calendar);
                    if (exchangeRate.isPresent()) {
                        portfolioReport.getExchangeRates().put(exchangeRateSymbol, exchangeRate.get().getUnitPrice());
                    } else {
                        Logger.logErrorAndExit("the exchange rate for {} does not exist", exchangeRateSymbol);
                    }
                });
    }

    /**
     * Update the market prices of products.
     *
     * @param portfolioReport the portfolio report
     * @param marketPriceAt the date of the market prices
     */
    private void updateProductMarketPrice(PortfolioReport portfolioReport, LocalDateTime marketPriceAt) {
        portfolioReport.getPortfolios().forEach((portfolioName, portfolio) ->
                portfolio.getProducts().forEach((key, product) -> {
                    if (CurrencyType.isValid(product.getSymbol())) {
                        var marketPrice = Price
                                .builder()
                                .symbol(product.getSymbol())
                                .unitPrice(BigDecimal.ONE)
                                .dataProvider(DataProviderType.NOT_DEFINED)
                                .tradeDate(marketPriceAt)
                                .requestDate(marketPriceAt)
                                .build();
                        product.setMarketPrice(marketPrice);
                    } else {
                        Optional<Price> marketPrice;
                        if (Objects.isNull(marketPriceAt)) {
                            marketPrice = getMarketPrice(product.getSymbol());
                            product.setMarketPrice(marketPrice.orElse(null));
                        } else {
                            marketPrice = getMarketPrice(
                                    product.getSymbol(),
                                    LocalDateTimes.toCalendar(marketPriceAt));
                            product.setMarketPrice(marketPrice.orElse(null));
                        }
                    }
                })
        );
    }

    /**
     * Downloads the market price of a specific product.
     *
     * @param symbol product name
     * @return the market price
     */
    public Optional<Price> getMarketPrice(final String symbol) {
        return getMarketPrice(symbol, null);
    }

    /**
     * Downloads the market price of a specific product at a specific
     * date in the past.
     *
     * @param symbol product name
     * @param requestedTradeDate the date od the market price in the past
     * @return the market price
     */
    public Optional<Price> getMarketPrice(final String symbol, final Calendar requestedTradeDate) {
        var tradeDate = requestedTradeDate;
        if (Objects.isNull(tradeDate)) {
            tradeDate = Calendar.getInstance();
        }

        if (Objects.isNull(priceHistoryFile)) {
            return getPriceFromDataProvider(symbol, tradeDate);
        } else {
            var dataProviderConfiguration = getDataProviderConfiguration(symbol);
            var symbolAliasName = getSymbolAlias(symbol, dataProviderConfiguration);
            var price = getPriceFromHistory(symbolAliasName, tradeDate);
            if (price.isEmpty()) {
                log.info("price does not exists in the history");
                price = getPriceFromDataProvider(symbol, tradeDate);
            } else {
                log.info("price exists in the history: {}", price);
            }

            price.ifPresent(p -> writeToHistoryFile(priceHistoryFile, p));
            return price;
        }
    }

    /**
     * Writes the price to the price history file.
     *
     * @param priceHistoryFile path to the price history file
     * @param price the price will be saved
     */
    private void writeToHistoryFile(String priceHistoryFile, Price price) {
        PriceWriter writer = new PriceWriter();
        writer.setLanguage(priceHistoryWriterPrams.getLanguage());
        writer.setDecimalFormat(priceHistoryWriterPrams.getDecimalFormat());
        writer.setDecimalGroupingSeparator(Character.MIN_VALUE);
        writer.setDateTimePattern(priceHistoryWriterPrams.getDateTimePattern());
        writer.setZone(ZoneId.of(priceHistoryWriterPrams.getZone()));
        writer.setMultiplicity(priceHistoryWriterPrams.getMultiplicity());
        writer.write(priceHistoryWriterPrams.getWriteMode(), priceHistoryFile, price);
    }

    /**
     * Get price from the history file.
     *
     * @param symbol product name
     * @param requestedTradeDate the trade date
     * @return the price if exist in the history file
     */
    private Optional<Price> getPriceFromHistory(final String symbol, final Calendar requestedTradeDate) {
        var parser = new PriceParser();
        parser.setZone(zone);

        var prices = parser.parse(priceHistoryFile);
        var requestedTradeDateAsLocalDateTime = requestedTradeDate
                .toInstant()
                .atZone(requestedTradeDate.getTimeZone().toZoneId())
                .toLocalDateTime();

        return prices
                .stream()
                .filter(price -> price.getSymbol().equals(symbol))
                .filter(price -> price.getRequestDate().isEqual(requestedTradeDateAsLocalDateTime))
                .findFirst();
    }

    /**
     * Downloads the market price from the data provider.
     *
     * @param symbol product price
     * @param requestedTradeDate the trade date
     * @return the price if exist
     */
    private Optional<Price> getPriceFromDataProvider(final String symbol, final Calendar requestedTradeDate) {
        Optional<Price> price = Optional.empty();
        var symbolAliasName = symbol;

        DataProviderType dataProvider = dataProviderFromCli;

        if (Objects.isNull(dataProvider)) {
            var dataProviderConfiguration = getDataProviderConfiguration(symbol);
            dataProvider = getDataProvider(dataProviderConfiguration);
            symbolAliasName = getSymbolAlias(symbol, dataProviderConfiguration);
        }

        var downloader = Downloader.get().get(dataProvider);
        if (Objects.isNull(downloader)) {
            Logger.logErrorAndExit("Unknown market data downloader: '{}'", dataProvider);
        } else {
            price = Objects.isNull(requestedTradeDate)
                    ? downloader.getPrice(symbolAliasName)
                    : downloader.getPrice(symbolAliasName, requestedTradeDate);
        }
        return price;
    }

    /**
     * Parses the market data configuration file and returns with
     * the product symbol alias.
     *
     * @param symbol product name
     * @return the data provider configuration
     */
    private String[] getDataProviderConfiguration(String symbol) {
        /*
          data provider configuration:
             - <SYMBOL>=<PROVIDER-NAME>;<SYMBOL-NAME-AT-PROVIDER>
             - <SYMBOL>=<PROVIDER-NAME>

          for example:
             - OTP=OTP.BD;YAHOO
             - BTC-EUR=COINBASE_PRO
         */
        var dataProviderConfiguration = new String[0];
        try (InputStream inputStream = new FileInputStream(dataProviderFile)) {
            Properties properties = new Properties();
            properties.load(inputStream);

            var configuration = properties.getProperty(symbol.toUpperCase());
            if (Objects.isNull(configuration)) {
                var message = "Missing market data provider. Symbol: \"{}\", File: \"{}\"";
                Logger.logErrorAndExit(message, symbol, dataProviderFile);
            } else {
                dataProviderConfiguration = configuration.split(";");
                if (dataProviderConfiguration.length < 1 || dataProviderConfiguration.length > 2) {
                    var message = "Invalid data provider configuration. Symbol: '{}', Source: '{}'";
                    Logger.logErrorAndExit(message, symbol, dataProviderFile);
                }
            }
        } catch (IOException e) {
            var message = "Error while reading the \"{}\" file. Error: {}";
            Logger.logErrorAndExit(message, dataProviderFile, e.toString());
        }

        return dataProviderConfiguration;
    }

    /**
     * Gets alias name of the product symbol, stored in the data-provider.properties file.
     *
     * @param symbol product symbol
     * @param dataProviderConfiguration data provider configuration string
     * @return product symbol alias name
     */
    private String getSymbolAlias(String symbol, String[] dataProviderConfiguration) {
        return dataProviderConfiguration.length == 1 ? symbol : dataProviderConfiguration[1];
    }

    /**
     * Gets the data provider for a given product.
     *
     * @param dataProviderConfiguration data provider configuration string
     * @return the data provider
     */
    private DataProviderType getDataProvider(String[] dataProviderConfiguration) {
        return DataProviderType.valueOf(dataProviderConfiguration[0]);
    }
}
