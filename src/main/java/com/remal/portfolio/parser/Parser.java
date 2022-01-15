package com.remal.portfolio.parser;

import com.remal.portfolio.model.Transaction;

import java.util.List;

/**
 * Transaction downloader/parser interface that each parser must implement.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public interface Parser {

    /**
     * Transforms trade data come from the source system to list of transactions.
     *
     * @return list of transactions
     */
    List<Transaction> parse();
}
