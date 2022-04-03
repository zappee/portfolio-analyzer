package com.remal.portfolio.model;

/**
 * Labels that appear in the reports.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class LabelCollection {

    /**
     * List of the column headers in the transaction report.
     */
    public static final Label[] TRANSACTION_TABLE_HEADERS = {
        Label.PORTFOLIO,
        Label.TICKER,
        Label.TYPE,
        Label.VALUATION,
        Label.TRADE_DATE,
        Label.QUANTITY,
        Label.PRICE,
        Label.FEE,
        Label.CURRENCY,
        Label.ORDER_ID,
        Label.TRADE_ID,
        Label.TRANSFER_ID
    };

    /**
     * List of the column headers in portfolio summary report.
     */
    public static final Label[] PORTFOLIO_SUMMARY_TABLE_HEADER = {
        Label.TICKER,
        Label.QUANTITY,
        Label.AVG_PRICE,
        Label.DEPOSITS,
        Label.WITHDRAWALS,
        Label.NET_COST,
        Label.MARKET_VALUE
    };

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private LabelCollection() {
        throw new UnsupportedOperationException();
    }
}
