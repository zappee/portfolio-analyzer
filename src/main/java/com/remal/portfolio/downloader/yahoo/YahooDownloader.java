package com.remal.portfolio.downloader.yahoo;

import com.remal.portfolio.downloader.Downloader;
import com.remal.portfolio.model.DataProviderType;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.util.Calendars;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.Sleep;
import lombok.extern.slf4j.Slf4j;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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
     * @param symbol             product name
     * @param requestedTradeDate trade date in the past
     * @return the product's market price
     */
    @Override
    public Optional<Price> getPrice(String symbol, Calendar requestedTradeDate) {
        var retry = new AtomicInteger();
        var repetitions = 0;
        var delay = 1.0;
        var actualTradeDate = (Calendar) requestedTradeDate.clone();
        Optional<Price> marketPrice = download(symbol, actualTradeDate, retry);

        // reset the second and try it again
        if (marketPrice.isEmpty()) {
            actualTradeDate.set(Calendar.SECOND, 0);
            actualTradeDate.set(Calendar.MILLISECOND, 0);
            Sleep.sleep(SLEEP_IN_MILLISECOND);
            marketPrice = download(symbol, actualTradeDate, retry);
        }

        // trying to move back in time a little for the first available price
        while (marketPrice.isEmpty() && repetitions < MAX_REPETITIONS) {
            delay = delay * MULTIPLICITY;
            var amount = (int)(delay * -1);
            actualTradeDate.add(Calendar.MINUTE, amount);
            Sleep.sleep(SLEEP_IN_MILLISECOND);
            marketPrice = download(symbol, actualTradeDate, retry);
            repetitions++;
        }

        if (marketPrice.isEmpty()) {
            Logger.logErrorAndExit("the price of the '{}' does not exist", symbol);
        } else {
            marketPrice.get().setRequestDate(Calendars.toLocalDateTime(requestedTradeDate));
        }

        return marketPrice;
    }

    /**
     * Downloads the price of a stock on a certain date in the past.
     *
     * @param symbol             product name
     * @param requestedTradeDate trade date in the past
     * @param retry the number of the retrying, only used for logging
     * @return the product's market price
     */
    private Optional<Price> download(final String symbol, final Calendar requestedTradeDate, AtomicInteger retry) {
        log.debug(
                "< {}getting the price of \"{}\" at {}, provider: \"{}\"...",
                retry.incrementAndGet() > 1 ? "(" + retry +") " : "",
                symbol,
                Calendars.toString(requestedTradeDate),
                DATA_PROVIDER);

        Optional<Price> marketPrice = Optional.empty();
        try {
            var stock = YahooFinance.get(symbol);
            if (Objects.isNull(stock)) {
                log.warn(SYMBOL_NOT_FOUND, symbol, DATA_PROVIDER);
            } else {
                // clone is needed because the stupid "stock.getHistory" method call changes
                // the value of the date variable
                var priceHistory = stock.getHistory((Calendar) requestedTradeDate.clone(), Interval.DAILY);

                var historicalQuote = priceHistory.stream().findFirst();
                if (historicalQuote.isPresent()) {
                    marketPrice = Optional.of(Price
                            .builder()
                            .symbol(symbol)
                            .unitPrice(historicalQuote.get().getClose())
                            .dataProvider(DATA_PROVIDER)
                            .requestDate(Calendars.toLocalDateTime(requestedTradeDate))
                            .tradeDate(LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(historicalQuote.get().getDate().getTimeInMillis()),
                                    ZoneId.systemDefault()))
                            .build());
                }
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
