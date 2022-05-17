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
     * @return true if the date meets with the conditions
     */
    public static boolean tickerFilter(List<String> tickers, Transaction transaction) {
        return tickers.isEmpty() || tickers.contains(transaction.getTicker().trim());
    }

    /**
     * From date filter.
     *
     * @param toCompare the value that will be compared
     * @param compareTo compare the value with this
     * @return true if the date meets with the conditions
     */
    public static boolean dateEqualOrAfterFilter(LocalDateTime toCompare, LocalDateTime compareTo) {
        return Objects.isNull(compareTo) || toCompare.isEqual(compareTo) || toCompare.isAfter(compareTo);
    }

    /**
     * To date filter.
     *
     * @param toCompare the value that will be compared
     * @param compareTo compare the value with this
     * @return true if the transaction meets with the conditions
     */
    public static boolean dateEqualOrBeforeFilter(LocalDateTime toCompare, LocalDateTime compareTo) {
        return Objects.isNull(compareTo) || toCompare.isEqual(compareTo) || toCompare.isBefore(compareTo);
    }

    /**
     * Check whether the date is in the interval or not.
     *
     * @param rangeBegin start of the interval
     * @param rangeEnd end of the interval
     * @param dateToCheck the date to check whether is in the interval or not
     * @return true if the date is between the interval
     */
    public static boolean dateBetweenFilter(LocalDateTime rangeBegin,
                                            LocalDateTime rangeEnd,
                                            LocalDateTime dateToCheck) {
        return !dateToCheck.isBefore(rangeBegin) && !dateToCheck.isAfter(rangeEnd);
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
