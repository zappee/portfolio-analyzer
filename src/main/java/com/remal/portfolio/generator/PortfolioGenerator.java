package com.remal.portfolio.generator;

import com.remal.portfolio.model.ProductSummary;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.util.BigDecimals;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the portfolio summary report based on the given transaction list.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 *
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class PortfolioGenerator {

    /**
     * Generates the portfolio summary.
     *
     * @param transactions list of the transactions
     * @return             the portfolio summary data
     */
    public List<List<ProductSummary>> generate(List<Transaction> transactions) {
        List<List<ProductSummary>> portfolios = new ArrayList<>();
        transactions.forEach(transaction -> addTransactionToPortfolio(portfolios, transaction));
        return portfolios;
    }

    /**
     * Adds a transaction to the portfolio summary.
     *
     * @param portfolios  the product summary
     * @param transaction transaction to be added to the summary
     */
    private void addTransactionToPortfolio(List<List<ProductSummary>> portfolios, Transaction transaction) {
        var portfolio = transaction.getPortfolio();
        var ticker = transaction.getTicker();
        var productSummary = getProductSummary(portfolios, portfolio, ticker);

        productSummary.addTransaction(transaction);

        // generating and adding extra cash transactions for buy, sell, fees and dividends
        if (transaction.getType() == TransactionType.BUY) {
            ticker = transaction.getCurrency().name();
            productSummary = getProductSummary(portfolios, portfolio, ticker);
            productSummary.addTransaction(transaction.toBuilder().type(TransactionType.DEBIT).build());
        } else if (transaction.getType() == TransactionType.SELL || transaction.getType() == TransactionType.DIVIDEND) {
            ticker = transaction.getCurrency().name();
            productSummary = getProductSummary(portfolios, portfolio, ticker);
            productSummary.addTransaction(transaction.toBuilder().type(TransactionType.CREDIT).build());
        }

        if (BigDecimals.isNotZero(transaction.getFee())) {
            ticker = transaction.getCurrency().name();
            productSummary = getProductSummary(portfolios, portfolio, ticker);
            var clonedTransaction = transaction
                    .toBuilder()
                    .type(TransactionType.FEE)//getTypeForAdditionalTransaction(transaction.getType()))
                    .fee(null)
                    .ticker(transaction.getCurrency().name())
                    .quantity(transaction.getFee())
                    .price(BigDecimal.ONE);
            productSummary.addTransaction(clonedTransaction.build());
        }
    }

    /**
     * Returns with a ProductSummary instance. If the belonging instance does not exist
     * then it will be generated as an empty but initialized object.
     *
     * @param portfolios the collection of the ProductSummary items
     * @param portfolio  name of the portfolio
     * @param ticker     name of the product
     * @return           the ProductSummary instances that represents the data for the report
     */
    private ProductSummary getProductSummary(List<List<ProductSummary>> portfolios, String portfolio, String ticker) {
        var selectedPortfolio = portfolios
                .stream()
                .filter(productSummaries ->
                        productSummaries
                                .stream()
                                .anyMatch(productSummary -> productSummary.getPortfolio().equals(portfolio)))
                .findFirst()
                .orElseGet(() -> {
                    var productSummaries = new ArrayList<ProductSummary>();
                    portfolios.add(productSummaries);
                    return productSummaries;
                });

        return selectedPortfolio
                .stream()
                .filter(actualProductSummary -> actualProductSummary.getTicker().equals(ticker))
                .findFirst()
                .orElseGet(() -> {
                    var p = new ProductSummary(portfolio, ticker);
                    selectedPortfolio.add(p);
                    return p;
                });
    }
}
