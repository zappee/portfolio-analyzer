package com.remal.portfolio.downloader;

import com.remal.portfolio.downloader.coinbasepro.CoinbaseProDownloader;
import com.remal.portfolio.downloader.yahoo.YahooDownloader;
import com.remal.portfolio.model.ProductPrice;
import com.remal.portfolio.model.ProviderType;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;
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
     * @return       the latest price
     */
    Optional<ProductPrice> getPrice(String ticker);

    /**
     * Downloads the price of a stock on a certain date in the past.
     *
     * @param ticker    product name
     * @param timestamp date in the past
     * @return          the latest price
     */
    Optional<ProductPrice> getPrice(String ticker, Calendar timestamp);

    /**
     * Initialize the market price downloader instances.
     *
     * @return the market price downloader instances
     */
    static Map<ProviderType, Downloader> initializeDownloader() {
        Map<ProviderType, Downloader> downloader = new EnumMap<>(ProviderType.class);
        downloader.put(ProviderType.COINBASE_PRO, new CoinbaseProDownloader());
        downloader.put(ProviderType.YAHOO, new YahooDownloader());
        return downloader;
    }
}
