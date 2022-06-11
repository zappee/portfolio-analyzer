package com.remal.portfolio.util;

import com.remal.portfolio.model.Transaction;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper that renames the portfolio names.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class PortfolioNameRenamer {

    /**
     * Rename the portfolio of the transactions.
     *
     * @param transactions list of the transactions
     * @param replaces list of the from-to pairs
     */
    public static void rename(List<Transaction> transactions, List<String> replaces) {
        Map<String, String> portfolioNameToRename = new HashMap<>();
        try {
            replaces.forEach(replace -> {
                var from = replace.split(":")[0];
                var to = replace.split(":")[1];
                log.debug("> renaming portfolio name from '{}' to '{}'...", from, to);
                portfolioNameToRename.put(from, to);
            });

            portfolioNameToRename.forEach((k, v) -> transactions
                    .stream()
                    .filter(transaction -> transaction.getPortfolio().equals(k))
                    .forEach(transaction -> transaction.setPortfolio(v)));

        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logErrorAndExit("Invalid value provided for the '-map' option.");
        }
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private PortfolioNameRenamer() {
        throw new UnsupportedOperationException();
    }
}
