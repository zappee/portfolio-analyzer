package com.remal.portfolio.model;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO that holds information for labels appear in the reports.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class LabelCollection {

    /**
     * List of the supported headers in the transaction report.
     */
    private static final List<Label> TRANSACTION_TABLE = new ArrayList<>();

    /**
     * List of the supported headers in portfolio summary report.
     */
    private static final List<Label> PORTFOLIO_SUMMARY_TABLE = new ArrayList<>();

    static {
        // transaction report
        TRANSACTION_TABLE.add(Label.PORTFOLIO);
        TRANSACTION_TABLE.add(Label.TICKER);
        TRANSACTION_TABLE.add(Label.TYPE);
        TRANSACTION_TABLE.add(Label.TRADE_DATE);
        TRANSACTION_TABLE.add(Label.QUANTITY);
        TRANSACTION_TABLE.add(Label.PRICE);
        TRANSACTION_TABLE.add(Label.FEE);
        TRANSACTION_TABLE.add(Label.CURRENCY);
        TRANSACTION_TABLE.add(Label.ORDER_ID);
        TRANSACTION_TABLE.add(Label.TRADE_ID);
        TRANSACTION_TABLE.add(Label.TRANSFER_ID);

        // portfolio summary report
        PORTFOLIO_SUMMARY_TABLE.add(Label.TICKER);
        PORTFOLIO_SUMMARY_TABLE.add(Label.QUANTITY);
        PORTFOLIO_SUMMARY_TABLE.add(Label.AVG_PRICE);
        PORTFOLIO_SUMMARY_TABLE.add(Label.NET_COST);
        PORTFOLIO_SUMMARY_TABLE.add(Label.MARKET_VALUE);
    }

    /**
     * Static getter method.
     *
     * @return list of the column names for transaction table
     */
    public static List<Label> getTransactionTable() {
        return TRANSACTION_TABLE;
    }

    /**
     * Static getter method.
     *
     * @return list of the column names for portfolio summary table
     */
    public static List<Label> getPortfolioSummaryTable() {
        return PORTFOLIO_SUMMARY_TABLE;
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private LabelCollection() {
        throw new UnsupportedOperationException();
    }
}
