package com.remal.portfolio.util;

import com.remal.portfolio.model.Transaction;

import java.util.Comparator;

/**
 * List sorter tool.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class Sorter {

    /**
     * Sort the list of transaction by created date.
     *
     * @return the comparator instance
     */
    public static Comparator<Transaction> tradeDateComparator() {
        return Comparator.comparing(Transaction::getTradeDate);
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
