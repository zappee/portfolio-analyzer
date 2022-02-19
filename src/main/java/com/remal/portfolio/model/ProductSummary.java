package com.remal.portfolio.model;

import com.remal.portfolio.util.BigDecimals;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
     * Adds a new transaction to the summary report.
     * @param transaction the transaction to add
     */
    public void addTransaction(Transaction transaction) {
        // transaction history
        transactionHistory.add(transaction);

        // transactions
        // dividend and money deposit/withdrawal are not relevant transactions
        var currency = Currency.getEnum(transaction.getTicker());
        if (Currency.UNKNOWN == currency && TransactionType.DIVIDEND != transaction.getType()) {
            transactions.add(transaction);
        }

        setTotalShares(transaction.getType(), transaction.getQuantity(), transaction.getPrice());
        if (BigDecimals.isNullOrZero(totalShares)) {
            transactions.clear();
            averagePrice = null;
            netCost = null;
            marketValue = null;
            totalShares = null;
        } else {
            setAveragePrice();
        }
    }

    private void setAveragePrice() {
        Map<BigDecimal, BigDecimal> supply = new LinkedHashMap<>();

        transactions.forEach(transaction -> {

            if (transaction.getType() == TransactionType.BUY) {
                var key = transaction.getPrice();
                var quantity = supply.getOrDefault(key, BigDecimal.ZERO);
                supply.put(key, quantity.add(transaction.getQuantity()));

            } else if (transaction.getType() == TransactionType.SELL) {
                if (transaction.getInventoryValuation() == InventoryValuation.FIFO) {
                    updateSupplyBasedOnFifoSell(supply, transaction);
                } else {
                    updateSupplyBasedOnLifoSell(supply, transaction);
                }
                averagePrice = computeAveragePrice(supply);
            } else {
                log.warn("unhandled transaction type while calculating the average price: {}", transaction.getType());
            }
        });

        averagePrice = computeAveragePrice(supply);
    }

    private void updateSupplyBasedOnFifoSell(Map<BigDecimal, BigDecimal> supply, Transaction transaction) {
        var iterator = new ArrayList<>(supply.entrySet()).listIterator(supply.size());
        var endOfLoop = false;
        var quantityToSell = transaction.getQuantity();

        // reverse loop
        while (iterator.hasPrevious() && ! endOfLoop) {
            var entry = iterator.previous();
            var remainQuantity = entry.getValue().subtract(quantityToSell);
            if (BigDecimals.isNonNegative(remainQuantity)) {
                entry.setValue(remainQuantity);
                endOfLoop = true;
            } else {
                entry.setValue(BigDecimal.ZERO);
                quantityToSell = remainQuantity.abs();
            }
        }
    }

    private void updateSupplyBasedOnLifoSell(Map<BigDecimal, BigDecimal> supply, Transaction transaction) {
        var iterator = new ArrayList<>(supply.entrySet()).listIterator();
        var endOfLoop = false;
        var quantityToSell = transaction.getQuantity();

        // reverse loop
        while (iterator.hasNext() && ! endOfLoop) {
            var entry = iterator.next();
            var remainQuantity = entry.getValue().subtract(quantityToSell);
            if (BigDecimals.isNonNegative(remainQuantity)) {
                entry.setValue(remainQuantity);
                endOfLoop = true;
            } else {
                entry.setValue(BigDecimal.ZERO);
                quantityToSell = remainQuantity.abs();
            }
        }
    }

    private BigDecimal computeAveragePrice(Map<BigDecimal, BigDecimal> supply) {
        var totalInvestedAmount = BigDecimal.ZERO;
        var totalNumberOfShares = BigDecimal.ZERO;
        for (var entry : supply.entrySet()) {
            var price = entry.getKey();
            var quantity = entry.getValue();
            var investedAmount = price.multiply(quantity);
            totalInvestedAmount = totalInvestedAmount.add(investedAmount);
            totalNumberOfShares = totalNumberOfShares.add(quantity);
        }

        return BigDecimals.isNullOrZero(totalInvestedAmount) && BigDecimals.isNullOrZero(totalNumberOfShares)
                ? null
                : totalInvestedAmount.divide(totalNumberOfShares, MathContext.DECIMAL64);
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
                totalShares = Objects.isNull(totalShares)
                        ? volume
                        : totalShares.add(volume);
                break;

            case DIVIDEND:
                totalShares = Objects.isNull(totalShares)
                        ? volume.multiply(price)
                        : totalShares.add(volume.multiply(price));
                break;

            case SELL:
            case WITHDRAWAL:
            case FEE:
                totalShares = Objects.isNull(totalShares)
                        ? volume.negate()
                        : totalShares.subtract(volume);
                break;

            default:
                log.error("Unhandled transaction type: '{}'", transactionType);
                System.exit(CommandLine.ExitCode.SOFTWARE);
        }
    }
}
