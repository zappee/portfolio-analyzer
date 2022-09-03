package com.remal.portfolio.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The portfolio report POJO.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Getter
public class PortfolioReport {

    /**
     * The date when the report was generated.
     */
    private final LocalDateTime generated = LocalDateTime.now();

    /**
     * The currency of the report.
     */
    private final CurrencyType currency;

    /**
     * The portfolios.
     */
    private final Map<String, Portfolio> portfolios = new LinkedHashMap<>();

    /**
     * Exchange rages, used to exchange the portfolio items to the
     * base currency.
     */
    private final Map<String, BigDecimal> exchangeRates = new LinkedHashMap<>();

    /**
     * The total invested amounts per currency.
     */
    //private Map<String, BigDecimal> invested;

    /**
     * Portfolio market value
     */
    //private Map<String, BigDecimal> marketValue;

    /**
     * Invested amount.
     */
    //private BigDecimal investedAmount;

    /**
     * P/L on portfolio.
     */
    //private BigDecimal profitAndLoss;

    /**
     * Cash in portfolio per currency.
     */
    private final Map<String, BigDecimal> cashInPortfolio = new LinkedHashMap<>();

    /**
     * The total amount of deposits.
     */
    private final Map<String, BigDecimal> deposits = new LinkedHashMap<>();

    /**
     * The total amount of withdrawals.
     */
    private final Map<String, BigDecimal> withdrawals = new LinkedHashMap<>();

    /**
     * The account value, also known as total equity, is the total dollar value of all
     * the holdings of the trading account, not just the securities, but the cash as
     * well.
     */
    //private BigDecimal accountValue;

    /**
     * Constructor.
     *
     * @param currency base currency
     */
    public PortfolioReport(CurrencyType currency) {
        this.currency = currency;
    }

    /**
     * Adds transactions to the portfolio report.
     *
     * @param transactions list of transactions
     */
    public void addTransactions(List<Transaction> transactions) {
        transactions.forEach(this::addTransaction);
    }

    /**
     * Adds a transaction to the portfolio report.
     *
     * @param transaction transaction
     */
    public void addTransaction(Transaction transaction) {
        var portfolioName = transaction.getPortfolio();
        var portfolio = portfolios.computeIfAbsent(portfolioName, x -> new Portfolio(portfolioName));
        portfolio.addTransaction(transaction);
        updateTotals();
    }

    /**
     * Calculates the value of the totals in portfolio.
     */
    private void updateTotals() {
        cashInPortfolio.clear();
        deposits.clear();
        withdrawals.clear();

        portfolios.forEach((name, portfolio) ->
                portfolio.getProducts()
                        .entrySet()
                        .stream()
                        .filter(productEntry -> CurrencyType.isValid(productEntry.getKey()))
                        .forEach(productEntry -> {
                            var cur = productEntry.getKey();

                            // cash in portfolio
                            var cashActual = cashInPortfolio.computeIfAbsent(cur, x -> BigDecimal.ZERO);
                            cashInPortfolio.put(cur, cashActual.add(productEntry.getValue().getQuantity()));

                            // deposits
                            var depositActual = sumQuantities(
                                    TransactionType.DEPOSIT,
                                    productEntry.getValue().getTransactionHistory());
                            deposits.put(cur, depositActual.add(deposits.getOrDefault(cur, BigDecimal.ZERO)));

                            // withdrawals
                            var withdrawalsActual = sumQuantities(
                                    TransactionType.WITHDRAWAL,
                                    productEntry.getValue().getTransactionHistory());
                            withdrawals.put(cur, withdrawalsActual.add(withdrawals.getOrDefault(cur, BigDecimal.ZERO)));
                        })
        );
    }

    /**
     * Summaries the quantities that fits to the condition.
     *
     * @param transactionType condition
     * @param transactions transactions
     * @return the sum
     */
    private BigDecimal sumQuantities(TransactionType transactionType, List<Transaction> transactions) {
        return transactions
                .stream()
                .filter(transaction -> transaction.getType() == transactionType)
                .map(Transaction::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
