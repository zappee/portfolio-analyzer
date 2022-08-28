package com.remal.portfolio.downloader;

import com.remal.portfolio.downloader.coinbasepro.CoinbaseProDownloader;
import com.remal.portfolio.downloader.yahoo.YahooDownloader;
import com.remal.portfolio.model.DataProviderType;
import com.remal.portfolio.model.Price;

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
     * Error log message template.
     */
    String SYMBOL_NOT_FOUND = "symbol \"{}\" not found, provider: \"{}\"";

    /**
     * Error log message template.
     */
    String DOWNLOAD_ERROR = "error while downloading the price of \"{}\", provider: \"{}\", {}";

    /**
     * Downloads the latest price of a stock.
     *
     * @param symbol product name
     * @return the latest price
     */
    Optional<Price> getPrice(String symbol);

    /**
     * Downloads the price of a stock on a certain date in the past.
     *
     * @param symbol product name
     * @param timestamp date in the past
     * @return the latest price
     */
    Optional<Price> getPrice(String symbol, Calendar timestamp);

    /**
     * Initialize the market price downloader instances.
     *
     * @return the market price downloader instances
     */
    static Map<DataProviderType, Downloader> get() {
        Map<DataProviderType, Downloader> downloader = new EnumMap<>(DataProviderType.class);
        downloader.put(DataProviderType.COINBASE_PRO, new CoinbaseProDownloader());
        downloader.put(DataProviderType.YAHOO, new YahooDownloader());
        return downloader;
    }
}
