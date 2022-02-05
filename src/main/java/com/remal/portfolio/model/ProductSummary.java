package com.remal.portfolio.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Product summary POJO.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Getter
@Slf4j
public class ProductSummary {

    /**
     * Portfolio name.
     */
    private final String portfolio;

    /**
     * Product name.
     */
    private final String ticker;

    /**
     * The relevant transactions that are considered.
     */
    private final List<Transaction> transactions = new ArrayList<>();

    /**
     * The complete list of the transactions.
     */
    private final List<Transaction> transactionHistory = new ArrayList<>();

    /**
     * Average price of the product.
     */
    private BigDecimal averagePrice;

    /**
     * Net cost that was used to buy the product.
     */
    private BigDecimal netCost;

    /**
     * The current market value of the product.
     */
    private BigDecimal marketValue;

    /**
     * The number of the shares that he/she owns.
     */
    private BigDecimal totalShares = BigDecimal.ZERO;

    /**
     * Constructor.
     *
     * @param portfolio portfolio name
     * @param ticker product name
     */
    public ProductSummary(String portfolio, String ticker) {
        this.portfolio = portfolio;
        this.ticker = ticker;
    }

    /**
     * Adds a new transaction to the summay report.
     * @param transaction the transaction to add
     */
    public void addTransaction(Transaction transaction) {
        // transaction history
        transactionHistory.add(transaction);

        // transactions
        // dividend and money deposit/withdraval are not relevant transactions
        var currency = Currency.getEnum(transaction.getTicker());
        if (Currency.UNKNOWN == currency && TransactionType.DIVIDEND != transaction.getType()) {
            transactions.add(transaction);
        }

        setTotalShares(transaction.getType(), transaction.getQuantity(), transaction.getPrice());
        if (totalShares.compareTo(BigDecimal.ZERO) == 0) {
            transactions.clear();
        }
    }

    /**
     * Calculates the quantity of the product.
     *
     * @param transactionType transaction type, e.g. buy, sell
     * @param volume quantity
     * @param price price
     */
    private void setTotalShares(TransactionType transactionType, BigDecimal volume, BigDecimal price) {
        switch (transactionType) {
            case BUY:
            case DEPOSIT:
                totalShares = totalShares.add(volume);
                break;

            case DIVIDEND:
                totalShares = totalShares.add(volume.multiply(price));
                break;

            case SELL:
            case WITHDRAWAL:
            case FEE:
                totalShares = totalShares.subtract(volume);
                break;

            default:
                log.error("Unhandled transaction type: '{}'", transactionType);
                System.exit(CommandLine.ExitCode.SOFTWARE);
        }
    }
}
