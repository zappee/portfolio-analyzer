package com.remal.portfolio.util;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Stream filters.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class Filter {

    /**
     * Portfolio name filter.
     *
     * @param portfolio portfolio name
     * @param transaction transaction to check
     * @return true if the transaction meets with the conditions
     */
    public static boolean portfolioNameFilter(String portfolio, Transaction transaction) {
        return Objects.isNull(portfolio) || transaction.getPortfolio().equals(portfolio);
    }

    /**
     * Ticker filter.
     *
     * @param tickers ticker names
     * @param transaction transaction to check
     * @return true if the transaction meets with the conditions
     */
    public static boolean tickerFilter(List<String> tickers, Transaction transaction) {
        return tickers.isEmpty() || tickers.contains(transaction.getTicker().trim());
    }

    /**
     * From date filter.
     *
     * @param from timestamp
     * @param transaction transaction to check
     * @return true if the transaction meets with the conditions
     */
    public static boolean dateEqualOrAfterFilter(LocalDateTime from, Transaction transaction) {
        return Objects.isNull(from)
                || transaction.getTradeDate().isEqual(from)
                || transaction.getTradeDate().isAfter(from);
    }

    /**
     * To date filter.
     *
     * @param to timestamp
     * @param transaction transaction to check
     * @return true if the transaction meets with the conditions
     */
    public static boolean dateEqualOrBeforeFilter(LocalDateTime to, Transaction transaction) {
        return Objects.isNull(to)
                || transaction.getTradeDate().isEqual(to)
                || transaction.getTradeDate().isBefore(to);
    }

    /**
     * Columns to hide filter.
     *
     * @param columnsToHide list of column names that must hide
     * @param label column ID
     * @return true if the column meet with the conditions
     */
    public static boolean columnsToHideFilter(List<String> columnsToHide, Label label) {
        return !columnsToHide.contains(label.getId());
    }

    /**
     * Transaction ID filter.
     *
     * @param expected the transaction that contains the expected values
     * @param actual the actual transaction to check
     * @return true if the transaction meet with the conditions
     */
    public static boolean transactionIdFilter(Transaction expected, Transaction actual) {
        return Objects.equals(expected.getTransferId(), actual.getTransferId())
                && Objects.equals(expected.getOrderId(), actual.getOrderId())
                && Objects.equals(expected.getTradeId(), actual.getTradeId());
    }

    /**
     * Base currency filter.
     *
     * @param baseCurrency the base currency of the account at the broker company
     * @param ticker product name/ticker
     * @return true if the transaction meet with the conditions
     */
    public static boolean baseCurrencyFilter(CurrencyType baseCurrency, String ticker) {
        return Objects.isNull(baseCurrency) || ticker.contains(baseCurrency.name());
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    private Filter() {
        throw new UnsupportedOperationException();
    }
}
