package com.remal.portfolio.model;

import lombok.Getter;

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
    //private Map<String, BigDecimal> cashInPortfolio;

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
    }
}
