package com.remal.portfolio.util;

import com.remal.portfolio.model.PortfolioReport;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.model.Transaction;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Sorting portfolio report.
     *
     * @param portfolioReport the report
     */
    public static void sortPortfolioReport(PortfolioReport portfolioReport) {
        var sortedPortfolios = portfolioReport.getPortfolios()
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        portfolioReport.getPortfolios().clear();
        portfolioReport.getPortfolios().putAll(sortedPortfolios);

        portfolioReport.getPortfolios().forEach((portfolioName, portfolio) -> {
            var products = portfolio.getProducts();
            var sortedProducts = products.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            portfolio.getProducts().clear();
            portfolio.getProducts().putAll(sortedProducts);
        });
    }

    /**
     * Sort the list of transaction by created date.
     *
     * @return the comparator instance
     */
    public static Comparator<Transaction> tradeDateComparator() {
        return Comparator.comparing(Transaction::getTradeDate);
    }

    /**
     * Sort the products by symbol and date.
     *
     * @return the comparator instance
     */
    public static Comparator<Price> priceComparator() {
        return Comparator.comparing(Price::getSymbol).thenComparing(Price::getTradeDate);
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
