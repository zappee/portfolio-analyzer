package com.remal.portfolio.downloader.coinbasepro;

import com.remal.portfolio.downloader.Downloader;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.model.ProviderType;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Calendars;
import com.remal.portfolio.util.Logger;
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
    private static final ProviderType PROVIDER_TYPE = ProviderType.COINBASE_PRO;

    /**
     * Error log message template.
     */
    private static final String PRICE_NOT_FOUND = "ticker '{}' not found, provider: '{}'";

    /**
     * Error log message template.
     */
    private static final String DOWNLOAD_ERROR = "error while downloading the price of '{}', provider: '{}', error: {}";

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
     * @param ticker product name
     * @return       the latest price
     */
    @Override
    public Optional<Price> getPrice(String ticker) {
        log.debug("< getting the latest price of '{}', provider: '{}'...", ticker, PROVIDER_TYPE);

        var apiUrl = "https://api.coinbase.com/v2/prices/%s/spot";
        var uri = String.format(apiUrl, ticker);
        Optional<Price> price = Optional.empty();

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
                Logger.logErrorAndExit(PRICE_NOT_FOUND, ticker, PROVIDER_TYPE);
            } else {
                var jsonObject = (JSONObject) new JSONParser().parse(json);
                var data = (JSONObject) jsonObject.get("data");
                var errors = jsonObject.get("errors");

                if (Objects.nonNull(errors)) {
                    Logger.logErrorAndExit(PRICE_NOT_FOUND, ticker, PROVIDER_TYPE);
                } else {
                    var amountAsString = (String) data.get("amount");
                    price = Optional.of(Price
                            .builder()
                            .ticker(ticker)
                            .unitPrice(BigDecimals.valueOf(amountAsString))
                            .providerType(PROVIDER_TYPE)
                            .date(LocalDateTime.now())
                            .build());
                }
            }
        } catch (IOException | ParseException e) {
            Logger.logErrorAndExit(DOWNLOAD_ERROR, ticker, PROVIDER_TYPE, e.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.logErrorAndExit(DOWNLOAD_ERROR, ticker, PROVIDER_TYPE, e.toString());
        }

        logResult(ticker, price.orElse(null));
        return price;
    }

    /**
     * Downloads the price of a stock on a certain date in the past.
     *
     * @param ticker    product name
     * @param timestamp date in the past
     * @return          the latest price
     */
    @Override
    public Optional<Price> getPrice(String ticker, Calendar timestamp) {
        var message = "< getting the price of '{}' at {}, provider: '{}'...";
        log.debug(message, ticker, PROVIDER_TYPE, Calendars.toString(timestamp));

        var timestampAsString = Calendars.toString(timestamp);
        var apiUrl = "https://api.pro.coinbase.com/products/%s/candles?start=%s&end=%s&granularity=60";
        var uri = String.format(apiUrl, ticker, timestampAsString, timestampAsString);
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
                Logger.logErrorAndExit(PRICE_NOT_FOUND, ticker, PROVIDER_TYPE);
            } else if (json.equals("[]")) {
                var marketPrice = BigDecimal.ONE;
                log.warn("the '{}' ticker exists at '{}' provider, but the price of the stock on {} does not exist",
                        ticker, PROVIDER_TYPE, Calendars.toString(timestamp));
                log.info("the price of the '{}' does not exist thus market price has been set to {}",
                        ticker, marketPrice);
                return Optional.of(Price
                        .builder()
                        .unitPrice(marketPrice)
                        .ticker(ticker)
                        .date(Calendars.toLocalDateTime(timestamp))
                        .providerType(PROVIDER_TYPE)
                        .build());
            } else {
                if (json.toLowerCase().contains("notfound")) {
                    Logger.logErrorAndExit(PRICE_NOT_FOUND, ticker, PROVIDER_TYPE);
                }
                var bucket = json.replace("[", "");
                bucket = bucket.replace("]", "");

                // split and trim in one shot
                var fields = bucket.split("\\s*,\\s*");
                price = Optional.of(Price
                        .builder()
                        .ticker(ticker)
                        .unitPrice(BigDecimals.valueOf(fields[4]))
                        .providerType(PROVIDER_TYPE)
                        .date(LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(Long.parseLong(fields[0])),
                                ZoneId.systemDefault()))
                        .build());
            }
        } catch (IOException e) {
            Logger.logErrorAndExit(DOWNLOAD_ERROR, ticker, PROVIDER_TYPE, e.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.logErrorAndExit(DOWNLOAD_ERROR, ticker, PROVIDER_TYPE, e.toString());
        }

        logResult(ticker, price.orElse(null));
        return price;
    }

    /**
     * Log the result of the price downloader task.
     *
     * @param ticker product name
     * @param price  the result
     */
    private void logResult(String ticker, Price price) {
        if (Objects.isNull(price)) {
            Logger.logErrorAndExit("< invalid ticker: {}", ticker);
        } else {
            log.info("< {}", price);
        }
    }
}
