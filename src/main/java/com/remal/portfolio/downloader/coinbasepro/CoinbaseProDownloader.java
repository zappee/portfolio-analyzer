package com.remal.portfolio.downloader.coinbasepro;

import com.remal.portfolio.downloader.Downloader;
import com.remal.portfolio.model.DataProvider;
import com.remal.portfolio.model.ProductPrice;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Calendars;
import com.remal.portfolio.util.Logger;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
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
 * API information: https://docs.cloud.coinbase.com/exchange/docs
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class CoinbaseProDownloader extends CoinbaseProRequestBuilder implements Downloader {

    /**
     * The ID of this provider.
     */
    private static final DataProvider PROVIDER_ID = DataProvider.COINBASE_PRO;

    /**
     * Coinbase prompt price REST endpoint URL.
     */
    private static final String LATEST_API_URL =
            "https://api.coinbase.com/v2/prices/%s/spot";

    /**
     * Coinbase historical price REST endpoint URL.
     */
    private static final String HISTORICAL_API_URL =
            "https://api.pro.coinbase.com/products/%s/candles?start=%s&end=%s&granularity=60";

    /**
     * Constructor.
     *
     * @param publicKey Coinbase Pro API key as a string
     * @param passphrase Coinbase Pro passphrase
     * @param secret Coinbase Pro secret for the API key
     */
    public CoinbaseProDownloader(String publicKey, String passphrase, String secret) {
        super(publicKey, passphrase, secret);
    }

    /**
     * Downloads the latest price of a stock.
     * It uses the Coinbase PRO REST API to get the actual price.
     *
     * @param ticker product name
     * @return the latest price
     */
    @Override
    public Optional<ProductPrice> getPrice(String ticker) {
        log.debug(Downloader.GETTING_LATEST_PRICE_MESSAGE, ticker, PROVIDER_ID);
        var uri = String.format(LATEST_API_URL, ticker);
        Optional<ProductPrice> marketPrice = Optional.empty();

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
                log.debug(Downloader.PRICE_NOT_FOUND_MESSAGE, ticker, PROVIDER_ID);
            } else {
                var jsonObject = (JSONObject) new JSONParser().parse(json);
                var data = (JSONObject) jsonObject.get("data");
                var errors = jsonObject.get("errors");

                if (Objects.nonNull(errors)) {
                    log.debug(Downloader.PRICE_NOT_FOUND_MESSAGE, ticker, PROVIDER_ID);
                } else {
                    var amountAsString = (String) data.get("amount");
                    marketPrice = Optional.of(ProductPrice
                            .builder()
                            .ticker(ticker)
                            .price(BigDecimals.valueOf(amountAsString))
                            .dataProvider(PROVIDER_ID)
                            .timestamp(LocalDateTime.now())
                            .build());
                }
            }
        } catch (IOException | ParseException e) {
            Logger.logErrorAndExit(Downloader.DOWNLOADING_ERROR_MESSAGE, ticker, PROVIDER_ID, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.logErrorAndExit(Downloader.DOWNLOADING_ERROR_MESSAGE, ticker, PROVIDER_ID, e.getMessage());
        }

        return marketPrice;
    }

    /**
     * Downloads the price of a stock on a certain date in the past.
     *
     * @param ticker product name
     * @param timestamp date in the past
     * @return the latest price
     */
    @Override
    public Optional<ProductPrice> getPrice(String ticker, Calendar timestamp) {
        log.debug(Downloader.GETTING_HISTORICAL_PRICE_MESSAGE, ticker, PROVIDER_ID, Calendars.toString(timestamp));
        var timestampAsString = Calendars.toString(timestamp);
        var uri = String.format(HISTORICAL_API_URL, ticker, timestampAsString, timestampAsString);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Accept", "application/json")
                .GET()
                .build();
        var httpClient = HttpClient.newBuilder().build();
        Optional<ProductPrice> marketPrice = Optional.empty();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var json = response.body();
            if (Objects.isNull(json) || json.isEmpty()) {
                log.debug(Downloader.PRICE_NOT_FOUND_MESSAGE, ticker, PROVIDER_ID);
            } else {
                var bucket = json.replace("[", "");
                bucket = bucket.replace("]", "");

                // split and trim in one shot
                var fields = bucket.split("\\s*,\\s*");
                marketPrice = Optional.of(ProductPrice
                        .builder()
                        .ticker(ticker)
                        .price(BigDecimals.valueOf(fields[4]))
                        .dataProvider(PROVIDER_ID)
                        .timestamp(LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(Long.parseLong(fields[0])),
                                ZoneId.systemDefault()))
                        .build());
            }
        } catch (IOException e) {
            Logger.logErrorAndExit(Downloader.DOWNLOADING_ERROR_MESSAGE, ticker, PROVIDER_ID, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.logErrorAndExit(Downloader.DOWNLOADING_ERROR_MESSAGE, ticker, PROVIDER_ID, e.getMessage());
        }

        return marketPrice;
    }
}
