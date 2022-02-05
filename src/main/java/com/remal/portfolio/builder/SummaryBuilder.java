package com.remal.portfolio.builder;

import com.remal.portfolio.model.ProductSummary;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the portfolio summary data based on the given transaction list.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class SummaryBuilder {

    private final List<Transaction> transactions;
    private final String portfolio;
    private final String ticker;

    /**
     * Constructor.
     *
     * @param transactions list of the transactions
     * @param portfolio portfolio name
     * @param ticker product name
     */
    public SummaryBuilder(List<Transaction> transactions, String portfolio, String ticker) {
        this.transactions = transactions;
        this.portfolio = portfolio;
        this.ticker = ticker;
    }

    /**
     * Builds the portfolio summary data.
     *
     * @return the portfolio summary data
     */
    public Map<String, Map<String, ProductSummary>> build() {
        Map<String, Map<String, ProductSummary>> report = new HashMap<>();
        transactions
                .stream()
                .filter(x -> portfolio == null || x.getPortfolio().equals(portfolio))
                .filter(x -> ticker == null || x.getTicker().equals(ticker))
                .forEach(transaction -> addTransactionToReport(report, transaction));
        return report;
    }

    /**
     * Adds a transaction to the portfolio summary.
     *
     * @param report the portfolio summary
     * @param transaction transaction to be added to the summary
     */
    private void addTransactionToReport(Map<String, Map<String, ProductSummary>> report, Transaction transaction) {
        if (TransactionType.DIVIDEND == transaction.getType()) {
            var currency = transaction.getCurrency();
            var productSummary = getProductSummary(report, transaction.getPortfolio(), currency.name());
            productSummary.addTransaction(transaction);
        } else {
            var productSummary = getProductSummary(report, transaction.getPortfolio(), transaction.getTicker());
            productSummary.addTransaction(transaction);
        }
    }

    /**
     * Returns with a product summary.
     * If the requested product summary does not exist then it generates an empty one.
     *
     * @param report the portfolio summary
     * @param portfolio portfolio name
     * @param ticker product name
     * @return the product summmary
     */
    private ProductSummary getProductSummary(Map<String, Map<String, ProductSummary>> report,
                                             String portfolio,
                                             String ticker) {

        var productSummaries = report.computeIfAbsent(portfolio, v -> new HashMap<>());
        return productSummaries.computeIfAbsent(ticker, v -> new ProductSummary(portfolio, ticker));
    }
}
