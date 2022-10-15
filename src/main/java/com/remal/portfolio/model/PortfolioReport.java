package com.remal.portfolio.model;

import com.remal.portfolio.util.BigDecimals;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The portfolio report POJO.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PortfolioReport {

    /**
     * The date when the report was generated.
     */
    @EqualsAndHashCode.Include
    private final LocalDateTime generated;

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
    private final Map<String, BigDecimal> investments = new LinkedHashMap<>();

    /**
     * The total market value per currency.
     */
    private final Map<String, BigDecimal> marketValues = new LinkedHashMap<>();

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
     * The amount of profit and loss on the asset.
     */
    private final Map<String, BigDecimal> profitLoss = new LinkedHashMap<>();

    /**
     * Constructor.
     *
     * @param currency base currency
     * @param generated the last trade date that is considered
     */
    public PortfolioReport(CurrencyType currency, LocalDateTime generated) {
        this.currency = currency;
        this.generated = generated;
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
     * Update profit and loss related values.
     */
    public void updateProfitLoss() {
        investments.clear();
        marketValues.clear();
        portfolios.forEach((name, portfolio) -> portfolio.getProducts()
                .entrySet()
                .stream()
                .filter(productEntry -> BigDecimals.isNotNullAndNotZero(productEntry.getValue().getQuantity()))
                .forEach(productEntry -> {
                    var product = productEntry.getValue();
                    var symbol = productEntry.getValue().getCurrency().name();
                    var investedAmount = product.getInvestedAmount();
                    var marketValue = product.getMarketValue();

                    if (Objects.nonNull(investedAmount)) {
                        investments.put(symbol, investedAmount.add(investments.getOrDefault(symbol, BigDecimal.ZERO)));
                    }

                    marketValues.put(symbol, marketValue.add(marketValues.getOrDefault(symbol, BigDecimal.ZERO)));
                })
        );

        profitLoss.clear();
        portfolios
                .forEach((name, portfolio) -> portfolio.getProducts()
                        .entrySet()
                        .stream()
                        .filter(entry -> !CurrencyType.isValid(entry.getKey()))
                        .filter(entry -> BigDecimals.isNotNullAndNotZero(entry.getValue().getQuantity()))
                        .forEach(entry -> {
                            var product = entry.getValue();
                            var symbol = product.getCurrency().name();
                            var pl = product.getProfitAndLoss();
                            profitLoss.put(symbol, profitLoss.getOrDefault(symbol, BigDecimal.ZERO).add(pl));
                        }));
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
                            var symbol = productEntry.getKey();

                            // cash in portfolio
                            var cashActual = cashInPortfolio.computeIfAbsent(symbol, x -> BigDecimal.ZERO);
                            cashInPortfolio.put(symbol, cashActual.add(productEntry.getValue().getQuantity()));

                            // deposits
                            var depositActual = sumQuantities(
                                    TransactionType.DEPOSIT,
                                    productEntry.getValue().getTransactionHistory());
                            deposits.put(symbol, depositActual.add(deposits.getOrDefault(symbol, BigDecimal.ZERO)));

                            // withdrawals
                            var withdrawalsActual = sumQuantities(
                                    TransactionType.WITHDRAWAL,
                                    productEntry.getValue().getTransactionHistory());
                            withdrawals.put(
                                    symbol,
                                    withdrawalsActual.add(withdrawals.getOrDefault(symbol, BigDecimal.ZERO)));
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
