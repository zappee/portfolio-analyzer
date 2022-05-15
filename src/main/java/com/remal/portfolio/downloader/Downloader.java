package com.remal.portfolio.downloader;

import com.remal.portfolio.model.ProductPrice;

import java.util.Calendar;
import java.util.Optional;

/**
 * Product price downloader interface that providers must implement.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public interface Downloader {

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
