package com.remal.portfolio.util;

import com.remal.portfolio.model.Transaction;

import java.util.Comparator;
import java.util.List;

/**
 * List sorter tool.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class Sorter {

    /**
     * Sort the list of transaction by created date.
     *
     * @param transactions the list of transaction to sort
     */
    public static void sort(List<Transaction> transactions) {
        var sorted = transactions
                .stream()
                .sorted(Comparator.comparing(Transaction::getTradeDate))
                .toList();

        transactions.clear();
        transactions.addAll(sorted);
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private Sorter() {
        throw new UnsupportedOperationException();
    }
}
