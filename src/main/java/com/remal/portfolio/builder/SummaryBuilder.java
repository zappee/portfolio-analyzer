package com.remal.portfolio.builder;

import com.remal.portfolio.machine.BookkeepingTransactionGenerator;
import com.remal.portfolio.model.ProductSummary;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final List<String> tickers;

    /**
     * Constructor.
     *
     * @param transactions list of the transactions
     * @param portfolio portfolio name
     * @param tickers product name filter
     */
    public SummaryBuilder(List<Transaction> transactions, String portfolio, List<String> tickers) {
        this.transactions = transactions;
        this.portfolio = Objects.isNull(portfolio) ? null : portfolio.toUpperCase();
        this.tickers = tickers;
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
                .filter(x -> portfolio == null || portfolio.equals(x.getPortfolio().toUpperCase()))
                .filter(x -> tickers.isEmpty() || tickers.contains(x.getTicker().toUpperCase()))
                .forEach(transaction -> addTransactionToReport(report, transaction));

        new BookkeepingTransactionGenerator(transactions).updateBookkepingTransactions(report);
        return report;
    }

    /**
     * Adds a transaction to the portfolio summary.
     *
     * @param report the portfolio summary report
     * @param transaction transaction to be added to the summary
     */
    private void addTransactionToReport(Map<String, Map<String, ProductSummary>> report, Transaction transaction) {
        if (TransactionType.DIVIDEND == transaction.getType()) {
            var currency = transaction.getCurrency().name();
            var productSummary = getProductSummary(report, transaction.getPortfolio(), currency);
            productSummary.addTransaction(transaction);

        } else if (TransactionType.EXCHANGE == transaction.getType()) {
            var sourceCurrency = transaction.getTicker();
            var productSummary = getProductSummary(report, transaction.getPortfolio(), sourceCurrency);
            var clonedTransaction = transaction
                    .toBuilder()
                    .fee(null)
                    .build();
            productSummary.addTransaction(clonedTransaction);

            var targetCurrency = transaction.getCurrency().name();
            productSummary = getProductSummary(report, transaction.getPortfolio(), targetCurrency);
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
     * @return the product summary
     */
    private ProductSummary getProductSummary(Map<String, Map<String, ProductSummary>> report,
                                             String portfolio,
                                             String ticker) {

        var productSummaries = report.computeIfAbsent(portfolio, v -> new HashMap<>());
        return productSummaries.computeIfAbsent(ticker, v -> new ProductSummary(portfolio, ticker));
    }
}
