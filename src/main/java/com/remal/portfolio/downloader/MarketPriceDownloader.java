package com.remal.portfolio.downloader;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.DataProviderType;
import com.remal.portfolio.model.MultiplicityType;
import com.remal.portfolio.model.PortfolioReport;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.parser.PriceParser;
import com.remal.portfolio.picocli.arggroup.PortfolioArgGroup;
import com.remal.portfolio.picocli.arggroup.PortfolioInputArgGroup;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.util.FileWriter;
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
     * The report language.
     */
    private final String language;

    /**
     * The decimal format that controls the format of numbers in the
     * report.
     */
    private final String decimalFormat;

    /**
     * Pattern for formatting date and time in the report.
     */
    private final String dateTimePattern;

    /**
     * Controls the price export to file.
     */
    private final MultiplicityType multiplicity;

    /**
     * The file open mode.
     */
    private final FileWriter.WriteMode writeMode;

    /**
     * Constructor
     *
     * @param priceHistoryFile path to the price history file
     * @param inputArgGroup parameters from the command line
     * @param outputArgGroup parameters from the command line
     */
    public MarketPriceDownloader(String priceHistoryFile,
                                 PriceArgGroup.InputArgGroup inputArgGroup,
                                 PriceArgGroup.OutputArgGroup outputArgGroup) {
        var now = LocalDateTime.now();
        var dataProviderArgGroup = inputArgGroup.getDataProviderArgGroup();

        this.zone = ZoneId.of(outputArgGroup.getZone());
        this.dataProviderFromCli = dataProviderArgGroup.getDataProvider();
        this.dataProviderFile = LocalDateTimes.toString(zone, dataProviderArgGroup.getDataProviderFile(), now);
        this.priceHistoryFile = LocalDateTimes.toString(zone, priceHistoryFile, now);
        this.language = outputArgGroup.getLanguage();
        this.decimalFormat = outputArgGroup.getDecimalFormat();
        this.dateTimePattern = outputArgGroup.getDateTimePattern();
        this.multiplicity = outputArgGroup.getMultiplicity();
        this.writeMode = outputArgGroup.getWriteMode();
    }

    /**
     * Constructor.
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
        this.language = outputArgGroup.getLanguage();
        this.decimalFormat = outputArgGroup.getDecimalFormat();
        this.dateTimePattern = outputArgGroup.getDateTimePattern();
        this.multiplicity = outputArgGroup.getMultiplicity();
        this.writeMode = outputArgGroup.getWriteMode();
    }

    /**
     * Set the market prices for currencies and products.
     *
     * @param portfolioReport portfolio report
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
     * @param portfolioReport portfolio report
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
     * @param portfolioReport portfolio report
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
        var tradeDate = Objects.isNull(requestedTradeDate) ? Calendar.getInstance() : requestedTradeDate;
        var dataProviderConfiguration = getDataProviderConfiguration(symbol);
        var dataProvider = getDataProvider(dataProviderConfiguration);
        var realSymbol = getSymbolAlias(symbol, dataProviderConfiguration);
        var price = getPriceFromHistory(realSymbol, tradeDate);

        if (price.isEmpty()) {
            log.info("price does not exists in the history");
            price = getPriceFromDataProvider(dataProvider, realSymbol, tradeDate);
        } else {
            log.info("price exists in the history: {}", price);
        }

        price.ifPresent(p -> writeToHistoryFile(priceHistoryFile, p));
        return price;
    }

    /**
     * Writes the price to the price history file.
     *
     * @param priceHistoryFile path to the price history file
     * @param price the price will be saved
     */
    private void writeToHistoryFile(String priceHistoryFile, Price price) {
        var writer = new PriceWriter();
        writer.setLanguage(language);
        writer.setDecimalFormat(decimalFormat);
        writer.setDateTimePattern(dateTimePattern);
        writer.setZone(zone);
        writer.setMultiplicity(multiplicity);
        writer.write(writeMode, priceHistoryFile, price);
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

        if (Objects.nonNull(priceHistoryFile)) {
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
        } else {
            return Optional.empty();
        }
    }

    /**
     * Downloads the market price from the data provider.
     *
     * @param dataProvider the market data provider
     * @param symbol product price
     * @param requestedTradeDate the trade date
     * @return the price if exist
     */
    private Optional<Price> getPriceFromDataProvider(final DataProviderType dataProvider,
                                                     final String symbol,
                                                     final Calendar requestedTradeDate) {
        Downloader downloader;
        if (Objects.nonNull(dataProviderFromCli)) {
            downloader = Downloader.get().get(dataProviderFromCli);
        } else {
            downloader = Downloader.get().get(dataProvider);
        }

        Optional<Price> price = Optional.empty();
        if (Objects.isNull(downloader)) {
            Logger.logErrorAndExit("Unknown market data downloader: '{}'", dataProvider);
        } else {
            price = Objects.isNull(requestedTradeDate)
                    ? downloader.getPrice(symbol)
                    : downloader.getPrice(symbol, requestedTradeDate);
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
