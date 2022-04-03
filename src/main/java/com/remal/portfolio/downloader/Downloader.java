package com.remal.portfolio.downloader;

import com.remal.portfolio.model.ProductPrice;

import java.util.Calendar;
import java.util.Optional;

/**
 * Product price downloader interface that providers must implement.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public interface Downloader {

    /**
     * Log message template.
     */
    String GETTING_LATEST_PRICE_MESSAGE = "getting the latest price for '{}' from '{}'...";

    /**
     * Log message template.
     */
    String GETTING_HISTORICAL_PRICE_MESSAGE = "getting the price for '{}' from '{}' at {}...";

    /**
     * Log message template.
     */
    String PRICE_NOT_FOUND_MESSAGE = "market price not found for '{}', provider: '{}'";

    /**
     * Log message template.
     */
    String DOWNLOADING_ERROR_MESSAGE = "error while downloading the price for '{}' from '{}', error: {}";

    /**
     * Downloads the latest price of a stock.
     *
     * @param ticker product name
     * @return the latest price
     */
    Optional<ProductPrice> getPrice(String ticker);

    /**
     * Downloads the price of a stock on a certain date in the past.
     *
     * @param ticker product name
     * @param timestamp date in the past
     * @return the latest price
     */
    Optional<ProductPrice> getPrice(String ticker, Calendar timestamp);
}
