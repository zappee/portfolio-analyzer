package com.remal.portfolio.downloader.coinbasepro;

import com.remal.portfolio.downloader.Downloader;
import com.remal.portfolio.model.DataProviderType;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Calendars;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;

/**
 * Product price downloader for Coinbase Pro data provider.
 * API information: <a href="https://docs.cloud.coinbase.com/exchange/docs">Coinbase</a>
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class CoinbaseProDownloader extends CoinbaseProRequestBuilder implements Downloader {

    /**
     * The ID of this provider.
     */
    private static final DataProviderType DATA_PROVIDER = DataProviderType.COINBASE_PRO;

    /**
     * Constructor.
     */
    public CoinbaseProDownloader() {
        super(null, null, null);
    }

    /**
     * Downloads the latest price of a stock.
     * It uses the Coinbase PRO REST API to get the actual price.
     *
     * @param symbol product name
     * @return the latest price
     */
    @Override
    public Optional<Price> getPrice(String symbol) {
        log.debug("< getting the latest price of '{}', provider: '{}'...", symbol, DATA_PROVIDER);

        var apiUrl = "https://api.coinbase.com/v2/prices/%s/spot";
        var uri = String.format(apiUrl, symbol);
        Optional<Price> marketPrice = Optional.empty();

        try {
            var httpClient = HttpClient.newBuilder().build();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var json = response.body();
            if (Objects.isNull(json) || json.isEmpty()) {
                log.warn(SYMBOL_NOT_FOUND, symbol, DATA_PROVIDER);
            } else {
                var jsonObject = (JSONObject) new JSONParser().parse(json);
                var data = (JSONObject) jsonObject.get("data");
                var errors = jsonObject.get("errors");

                if (Objects.nonNull(errors)) {
                    log.warn(SYMBOL_NOT_FOUND, symbol, DATA_PROVIDER);
                } else {
                    var amountAsString = (String) data.get("amount");
                    var now = LocalDateTime.now();
                    marketPrice = Optional.of(Price
                            .builder()
                            .symbol(symbol)
                            .unitPrice(BigDecimals.valueOf(amountAsString))
                            .dataProvider(DATA_PROVIDER)
                            .tradeDate(now)
                            .requestDate(now)
                            .build());
                }
            }
        } catch (IOException | ParseException e) {
            log.warn(DOWNLOAD_ERROR, symbol, DATA_PROVIDER, e.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn(DOWNLOAD_ERROR, symbol, DATA_PROVIDER, e.toString());
        }

        marketPrice.ifPresent(price -> log.info("< {}", price));
        return marketPrice;
    }

    /**
     * Downloads the market price of a product at a specific date in the past.
     * If the price does not exist for the requested date then we are trying to move
     * back in time a little for the first available price.
     *
     * @param symbol the product
     * @param requestedTradeDate the trade date where we need the market price
     * @return the market price
     */
    @Override
    public Optional<Price> getPrice(final String symbol, final Calendar requestedTradeDate) {
        var maxRepetitions = 20;
        var repetitions = 0;

        var actualTradeDate = (Calendar) requestedTradeDate.clone();
        var marketPrice = download(symbol, actualTradeDate);

        // reset the second and try it again
        if (marketPrice.isEmpty()) {
            actualTradeDate.set(Calendar.SECOND, 0);
            actualTradeDate.set(Calendar.MILLISECOND, 0);
            marketPrice = download(symbol, actualTradeDate);
        }

        // trying to move back in time a little for the first available price
        while (marketPrice.isEmpty() && repetitions < maxRepetitions) {
            actualTradeDate.add(Calendar.MINUTE, -1);
            marketPrice = download(symbol, actualTradeDate);
            repetitions++;
        }

        if (marketPrice.isEmpty()) {
            var minusOne = new BigDecimal(-1);
            log.info("the price of the '{}' does not exist thus market price has been set to {}", symbol, minusOne);
            marketPrice = Optional.of(Price
                    .builder()
                    .unitPrice(minusOne)
                    .symbol(symbol)
                    .requestDate(Calendars.toLocalDateTime(requestedTradeDate))
                    .dataProvider(DATA_PROVIDER)
                    .build());
        } else {
            marketPrice.get().setRequestDate(Calendars.toLocalDateTime(requestedTradeDate));
        }

        marketPrice.ifPresent(price -> log.info("< {}", price));
        return marketPrice;
    }

    /**
     * Downloads the price of a stock on a certain date in the past.
     *
     * @param symbol product name
     * @param requestedTradeDate trade date in the past
     * @return the product's market price
     */
    private Optional<Price> download(final String symbol, final Calendar requestedTradeDate) {
        log.debug(
                "< getting the price of \"{}\" on {}, provider: \"{}\"...",
                symbol,
                Calendars.toUtcString(requestedTradeDate),
                DATA_PROVIDER);

        var timestampAsString = Calendars.toIsoString(requestedTradeDate);
        var apiUrl = "https://api.pro.coinbase.com/products/%s/candles?start=%s&end=%s&granularity=60";
        var uri = String.format(apiUrl, symbol, timestampAsString, timestampAsString);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Accept", "application/json")
                .GET()
                .build();
        var httpClient = HttpClient.newBuilder().build();
        Optional<Price> price = Optional.empty();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var json = response.body();
            if (Objects.isNull(json)) {
                log.warn(SYMBOL_NOT_FOUND, symbol, DATA_PROVIDER);
            } else if (json.equals("[]")) {
                var message = "< the price of the \"{}\" on {} does not exist";
                log.warn(message, symbol, Calendars.toUtcString(requestedTradeDate));
            } else {
                if (json.toLowerCase().contains("notfound")) {
                    log.warn(SYMBOL_NOT_FOUND, symbol, DATA_PROVIDER);
                }
                var bucket = json.replace("[", "");
                bucket = bucket.replace("]", "");

                // split and trim in one shot
                var fields = bucket.split("\\s*,\\s*");
                price = Optional.of(Price
                        .builder()
                        .symbol(symbol)
                        .unitPrice(BigDecimals.valueOf(fields[4]))
                        .dataProvider(DATA_PROVIDER)
                        .requestDate(Calendars.toLocalDateTime(requestedTradeDate))
                        .tradeDate(LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(Long.parseLong(fields[0])),
                                ZoneId.systemDefault()))
                        .build());
            }
        } catch (IOException e) {
            log.warn(DOWNLOAD_ERROR, symbol, DATA_PROVIDER, e.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn(DOWNLOAD_ERROR, symbol, DATA_PROVIDER, e.toString());
        }

        return price;
    }
}
