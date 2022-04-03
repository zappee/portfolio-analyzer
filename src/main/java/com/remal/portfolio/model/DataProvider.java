package com.remal.portfolio.model;

/**
 * Accepted data providers for getting the price of a stock.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public enum DataProvider {

    /**
     * Yahoo Finance API.
     */
    YAHOO,

    /**
     *Coinbase PRO (GDAX) API.
     */
    COINBASE_PRO
}
