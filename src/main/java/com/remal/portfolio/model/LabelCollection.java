package com.remal.portfolio.model;

import java.util.List;

/**
 * Labels that appear in the reports.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class LabelCollection {

    /**
     * List of the column headers in the transaction report.
     */
    public static final List<Label> TRANSACTION_TABLE_HEADERS = List.of(
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
            Label.TRANSFER_ID);

    /**
     * List of the column headers in portfolio summary report.
     */
    public static final List<Label> SUMMARY_TABLE_HEADERS = List.of(
            Label.PORTFOLIO,
            Label.TICKER,
            Label.QUANTITY,
            Label.AVG_PRICE,
            Label.INVESTED_AMOUNT,
            Label.MARKET_UNIT_PRICE,
            Label.MARKET_VALUE,
            Label.PROFIT_LOSS,
            Label.PROFIT_LOSS_PERCENT,
            Label.COST_TOTAL,
            Label.DEPOSIT_TOTAL,
            Label.WITHDRAWAL_TOTAL);

    /**
     * List of the column headers in the price report.
     */
    public static final List<Label> PRODUCT_PRICE_HEADERS = List.of(
            Label.TICKER,
            Label.PRICE,
            Label.DATE,
            Label.DATA_PROVIDER);

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private LabelCollection() {
        throw new UnsupportedOperationException();
    }
}
