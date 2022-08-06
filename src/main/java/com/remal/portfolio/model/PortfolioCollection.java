package com.remal.portfolio.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Collection of product summaries POJO.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Getter
@Setter
@Builder
public class PortfolioCollection {

    /**
     * The report itself.
     */
    private List<List<Portfolio>> portfolios;

    /**
     * The date when the report is generated.
     */
    private LocalDateTime generated;

    /**
     * The total invested amounts per currency.
     */
    private Map<String, BigDecimal> depositTotal;

    /**
     * Portfolio market value
     */
    private BigDecimal marketValue;

    /**
     * Invested amount.
     */
    private BigDecimal investedAmount;

    /**
     * P/L on portfolio.
     */
    private BigDecimal profitAndLoss;

    /**
     * Cash in portfolio per currency.
     */
    private Map<String, BigDecimal> cashInPortfolio;

    /**
     * The account value, also known as total equity, is the total dollar value of all
     * the holdings of the trading account, not just the securities, but the cash as
     * well.
     */
    private BigDecimal totalEquity;
}
