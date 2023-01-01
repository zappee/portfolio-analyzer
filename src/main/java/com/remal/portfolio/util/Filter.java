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
        return Objects.isNull(portfolio) || portfolio.equals("*") || transaction.getPortfolio().equals(portfolio);
    }

    /**
     * Symbol filter.
     *
     * @param symbols product names
     * @param transaction transaction to check
     * @return true if the date meets with the conditions
     */
    public static boolean symbolFilter(List<String> symbols, Transaction transaction) {
        return symbols.isEmpty() || symbols.contains(transaction.getSymbol().trim());
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
     * Columns to hide filter.
     * The filter converts the alias name before the use: "LABEL_PORTFOLIO" -> "PORTFOLIO"
     *
     * @param columnsToHide list of column names that must hide
     * @param label column ID
     * @return true if the column meet with the conditions
     */
    public static boolean columnsToHideFilter(List<String> columnsToHide, Label label) {
        return !columnsToHide.contains(label.name().split("_")[1]);
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
     * @param symbol product name
     * @return true if the transaction meet with the conditions
     */
    public static boolean baseCurrencyFilter(CurrencyType baseCurrency, String symbol) {
        return Objects.isNull(baseCurrency) || symbol.contains(baseCurrency.name());
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
