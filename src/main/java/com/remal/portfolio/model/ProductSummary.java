package com.remal.portfolio.model;

import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Logger;
import lombok.Getter;
import lombok.Setter;
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
@Setter
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
     * The sum of the withdrawals.
     * This field only used for currency, otherwise it is null.
     */
    private BigDecimal withdrawalSummary;

    /**
     * The sum of the deposits.
     * This field only used for currency, otherwise it is null.
     */
    private BigDecimal depositSummary;

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
        var currency = CurrencyType.getEnum(transaction.getTicker());
        if (CurrencyType.UNKNOWN == currency && TransactionType.DIVIDEND != transaction.getType()) {
            transactions.add(transaction);
        }

        updateTotalShares(transaction.getType(), transaction.getQuantity());
        if (BigDecimals.isNullOrZero(totalShares)) {
            transactions.clear();
            averagePrice = null;
            netCost = null;
            marketValue = null;
            totalShares = null;
        } else {
            updateAveragePrice();
        }
    }

    /**
     * Compute and set the average price.
     */
    private void updateAveragePrice() {
        Map<BigDecimal, BigDecimal> supply = new LinkedHashMap<>();
        transactions.forEach(transaction -> {
            if (transaction.getType() == TransactionType.BUY) {
                var key = transaction.getPrice();
                var quantity = supply.getOrDefault(key, BigDecimal.ZERO);
                supply.put(key, quantity.add(transaction.getQuantity()));

            } else if (transaction.getType() == TransactionType.SELL) {
                if (transaction.getInventoryValuation() == InventoryValuationType.FIFO) {
                    updateSupplyBasedOnFifoSell(supply, transaction);
                } else {
                    updateSupplyBasedOnLifoSell(supply, transaction);
                }
            }
        });
        averagePrice = computeAveragePrice(supply);
    }

    /**
     * Updating the transaction list based on FIFO sell.
     *
     * @param supply the relevant transactions for the supply
     * @param transaction the belonging sell transaction
     */
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

    /**
     * Updating the transaction list based on LIFO sell.
     *
     * @param supply the relevant transactions for the supply
     * @param transaction the belonging sell transaction
     */
    private void updateSupplyBasedOnLifoSell(Map<BigDecimal, BigDecimal> supply, Transaction transaction) {
        var iterator = new ArrayList<>(supply.entrySet()).listIterator();
        var endOfLoop = false;
        var quantityToSell = transaction.getQuantity();

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

    /**
     * Computing the average price based on the supply.
     *
     * @param supply the relevant transactions for the supply
     * @return the average price
     */
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
     */
    private void updateTotalShares(TransactionType transactionType, BigDecimal volume) {
        totalShares = switch (transactionType) {
            case BUY -> Objects.isNull(totalShares) ? volume : totalShares.add(volume);
            case SELL -> Objects.isNull(totalShares) ? volume.negate() : totalShares.subtract(volume);
            default -> {
                Logger.logErrorAndExit("Unhandled transaction type: {}", transactionType.name());
                throw new UnsupportedOperationException();
            }
        };
    }
}
