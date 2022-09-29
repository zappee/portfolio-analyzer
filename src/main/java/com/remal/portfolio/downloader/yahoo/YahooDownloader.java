package com.remal.portfolio.downloader.yahoo;

import com.remal.portfolio.downloader.Downloader;
import com.remal.portfolio.model.DataProviderType;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.util.Calendars;
import lombok.extern.slf4j.Slf4j;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;

/**
 * Product price downloader implementation for Yahoo Finance data providerType.
 * API information: <a href="https://financequotes-api.com">financequotes-api.com</a>
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class YahooDownloader implements Downloader {

    /**
     * The ID of this providerType.
     */
    private static final DataProviderType DATA_PROVIDER = DataProviderType.YAHOO;

    /**
     * Downloads the latest price of a stock. It uses the Yahoo REST API
     * to get the actual price.
     *
     * @param symbol product name
     * @return the latest price
     */
    @Override
    public Optional<Price> getPrice(String symbol) {
        log.debug("< getting the latest price of '{}', provider: '{}'...", symbol, DATA_PROVIDER);
        Optional<Price> marketPrice = Optional.empty();

        try {
            Stock stock = YahooFinance.get(symbol);
            if (stock.isValid()) {
                var tradeDate = Calendars.toLocalDateTime(
                        stock.getQuote().getLastTradeTime(),
                        stock.getQuote().getTimeZone().toZoneId());

                marketPrice = Optional.of(Price
                        .builder()
                        .symbol(symbol)
                        .unitPrice(stock.getQuote().getPrice())
                        .dataProvider(DATA_PROVIDER)
                        .tradeDate(tradeDate.atZone(ZoneOffset.UTC).toLocalDateTime())
                        .requestDate(LocalDateTime.now())
                        .build());
            }
        } catch (IOException e) {
            log.warn(DOWNLOAD_ERROR, symbol, DATA_PROVIDER, e.toString());
        } catch (NullPointerException e) {
            log.warn(SYMBOL_NOT_FOUND, symbol, DATA_PROVIDER.name());
        }

        marketPrice.ifPresent(price -> log.info("< {}", price));
        return marketPrice;
    }

    /**
     * Downloads the price of a stock on a certain date in the past.
     * It uses the Yahoo REST API to get the actual price.
     *
     * @param symbol product name
     * @param requestedTradeDate trade date in the past
     * @return the product's market price
     */
    @Override
    public Optional<Price> getPrice(String symbol, Calendar requestedTradeDate) {
        var message = "< getting the price of \"{}\" at {}, provider: \"{}\"...";
        log.debug(message, symbol, Calendars.toUtcString(requestedTradeDate), DATA_PROVIDER);

        Optional<Price> marketPrice = Optional.empty();
        try {
            var stock = YahooFinance.get(symbol);
            if (Objects.isNull(stock)) {
                log.warn(SYMBOL_NOT_FOUND, symbol, DATA_PROVIDER);
            } else {
                marketPrice = Optional.of(Price
                        .builder()
                        .symbol(symbol)
                        .unitPrice(stock.getQuote().getPrice())
                        .dataProvider(DATA_PROVIDER)
                        .requestDate(Calendars.toLocalDateTime(requestedTradeDate))
                        .tradeDate(LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(stock.getQuote().getLastTradeTime().getTimeInMillis()),
                                ZoneId.systemDefault()))
                        .build());
            }
        } catch (IOException e) {
            log.warn(DOWNLOAD_ERROR, symbol, DATA_PROVIDER, e.toString());
        } catch (NullPointerException e) {
            log.warn(SYMBOL_NOT_FOUND, symbol, DATA_PROVIDER);
        }

        marketPrice.ifPresent(price -> log.info("< {}", price));
        return marketPrice;
    }
}
