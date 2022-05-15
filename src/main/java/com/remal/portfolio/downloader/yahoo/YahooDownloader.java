package com.remal.portfolio.downloader.yahoo;

import com.remal.portfolio.downloader.Downloader;
import com.remal.portfolio.model.ProductPrice;
import com.remal.portfolio.model.Provider;
import com.remal.portfolio.util.Calendars;
import com.remal.portfolio.util.Logger;
import lombok.extern.slf4j.Slf4j;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Product price downloader implementation for Yahoo Finance data provider.
 * API information: https://financequotes-api.com
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class YahooDownloader implements Downloader {

    /**
     * The ID of this provider.
     */
    private static final Provider PROVIDER = Provider.YAHOO;

    /**
     * Error log message template.
     */
    private static final String PRICE_NOT_FOUND = "market price of '{}' not found, provider: '{}'";

    /**
     * Error log message template.
     */
    private static final String DOWNLOAD_ERROR = "error while downloading the price of '{}', provider: '{}', error: {}";

    /**
     * Downloads the latest price of a stock. It uses the Yahoo REST API
     * to get the actual price.
     *
     * @param ticker product name
     * @return the latest price
     */
    @Override
    public Optional<ProductPrice> getPrice(String ticker) {
        log.debug("input < getting the latest price of '{}', provider: '{}'...", ticker, PROVIDER);
        Optional<ProductPrice> marketPrice = Optional.empty();

        try {
            Stock stock = YahooFinance.get(ticker);
            var price = stock.getQuote().getPrice();
            if (Objects.nonNull(price)) {
                marketPrice = Optional.of(ProductPrice
                        .builder()
                        .ticker(ticker)
                        .price(price)
                        .provider(PROVIDER)
                        .timestamp(LocalDateTime.now())
                        .build());
            }
        } catch (IOException e) {
            Logger.logErrorAndExit(DOWNLOAD_ERROR, ticker, PROVIDER, e.toString());
        }
        return marketPrice;
    }

    /**
     * Downloads the price of a stock on a certain date in the past.
     * It uses the Yahoo REST API to get the actual price.
     *
     * @param ticker product name
     * @param timestamp date in the past
     * @return the latest price
     */
    @Override
    public Optional<ProductPrice> getPrice(String ticker, Calendar timestamp) {
        var message = "input < getting the price of '{}' at {}, provider: '{}'...";
        log.debug(message, ticker, PROVIDER, Calendars.toString(timestamp));

        Optional<ProductPrice> marketPrice = Optional.empty();
        try {
            Stock stock = YahooFinance.get(ticker);
            List<HistoricalQuote> historicalQuotes = stock.getHistory(timestamp, timestamp, Interval.DAILY);
            if (historicalQuotes.isEmpty()) {
                log.error(PRICE_NOT_FOUND, ticker, PROVIDER);
            } else {
                var historicalQuote = historicalQuotes.get(0);
                marketPrice = Optional.of(ProductPrice
                        .builder()
                        .ticker(ticker)
                        .price(historicalQuote.getClose())
                        .provider(PROVIDER)
                        .timestamp(LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(historicalQuote.getDate().getTimeInMillis()),
                                ZoneId.systemDefault()))
                        .build());
            }
        } catch (IOException e) {
            Logger.logErrorAndExit(DOWNLOAD_ERROR, ticker, PROVIDER, e.toString());
        }

        return marketPrice;
    }
}
