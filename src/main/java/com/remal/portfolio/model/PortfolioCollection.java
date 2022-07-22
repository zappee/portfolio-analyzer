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
     * The total invested amount.
     */
    private BigDecimal depositTotal;

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
     * Cash in portfolio.
     */
    private Map<String, BigDecimal> cashInPortfolio;
}
