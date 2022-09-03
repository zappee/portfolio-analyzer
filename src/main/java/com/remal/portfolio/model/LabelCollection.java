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
            Label.HEADER_PORTFOLIO,
            Label.HEADER_SYMBOL,
            Label.HEADER_TYPE,
            Label.HEADER_VALUATION,
            Label.HEADER_TRADE_DATE,
            Label.HEADER_QUANTITY,
            Label.HEADER_PRICE,
            Label.HEADER_FEE,
            Label.HEADER_CURRENCY,
            Label.HEADER_ORDER_ID,
            Label.HEADER_TRADE_ID,
            Label.HEADER_TRANSFER_ID);

    /**
     * List of the column headers in portfolio summary report.
     */
    public static final List<Label> PORTFOLIO_TABLE_HEADERS = List.of(
            Label.HEADER_PORTFOLIO,
            Label.HEADER_SYMBOL,
            Label.HEADER_QUANTITY,
            Label.HEADER_AVG_PRICE,
            Label.HEADER_MARKET_UNIT_PRICE,
            Label.HEADER_MARKET_VALUE,
            Label.HEADER_INVESTED_AMOUNT,
            Label.HEADER_PROFIT_LOSS,
            Label.HEADER_PROFIT_LOSS_PERCENT,
            Label.HEADER_COSTS,
            Label.HEADER_DEPOSITS,
            Label.HEADER_WITHDRAWALS);

    /**
     * List of the column headers in the price report.
     */
    public static final List<Label> PRODUCT_PRICE_HEADERS = List.of(
            Label.HEADER_SYMBOL,
            Label.HEADER_PRICE,
            Label.HEADER_TRADE_DATE,
            Label.HEADER_REQUEST_DATE,
            Label.HEADER_DATA_PROVIDER);

    public static final List<Label> PRODUCT_SUMMARY_FOOTER = List.of(
            Label.LABEL_TOTAL_CASH_PER_CURRENCY,
            Label.LABEL_TOTAL_CASH,
            Label.LABEL_TOTAL_EXCHANGE_RATE,
            Label.LABEL_TOTAL_DEPOSIT,
            Label.LABEL_TOTAL_WITHDRAWALS,
            Label.LABEL_TOTAL_INVESTMENT);

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private LabelCollection() {
        throw new UnsupportedOperationException();
    }
}
