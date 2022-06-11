package com.remal.portfolio.model;

import com.remal.portfolio.util.BigDecimals;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
public class Portfolio {

    /**
     * Portfolio name.
     */
    private final String name;

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
     * The amount that was invested to buy the stock.
     */
    private BigDecimal investedAmount;

    /**
     * The sum of the withdrawals.
     * This field only used for currency, otherwise it is null.
     */
    private BigDecimal withdrawalTotal;

    /**
     * The sum of the deposits.
     * This field only used for currency, otherwise it is null.
     */
    private BigDecimal depositTotal;

    /**
     * Net cost that was used to buy the product.
     */
    private BigDecimal costTotal;

    /**
     * The current market price per unit.
     */
    private BigDecimal marketUnitPrice;

    /**
     * The current market value of the portfolio.
     */
    private BigDecimal marketValue;

    /**
     * The number of the shares that he/she owns.
     */
    private BigDecimal totalShares = BigDecimal.ZERO;

    /**
     * The profit/loss value in currency.
     */
    private BigDecimal profitLoss;

    /**
     * The profit/loss value in percent.
     */
    private BigDecimal profitLossPercent;

    /**
     * Constructor.
     *
     * @param name   portfolio name
     * @param ticker product name
     */
    public Portfolio(String name, String ticker) {
        this.name = name;
        this.ticker = ticker;
    }

    /**
     * Adds a new transaction to the summary report.
     * @param transaction the transaction to add
     */
    public void addTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
        transactions.add(transaction);

        updateTotal(transaction);

        if (BigDecimals.isNullOrZero(totalShares)) {
            transactions.clear();
            totalShares = BigDecimal.ZERO;
            averagePrice = BigDecimal.ZERO;
            costTotal = null;
            marketValue = null;
        }

        updateAveragePrice();
        updateDepositTotal();
        updateWithdrawalTotal();
        updateCostTotal();
    }

    private void updateCostTotal() {
        if (CurrencyType.isValid(this.ticker)) {
            costTotal = transactionHistory
                    .stream()
                    .filter(transaction -> CurrencyType.isValid(transaction.getTicker()))
                    .map(transaction -> Objects.isNull(transaction.getFee()) ? BigDecimal.ZERO : transaction.getFee())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            costTotal = transactionHistory
                    .stream()
                    .map(transaction -> Objects.isNull(transaction.getFee()) ? BigDecimal.ZERO : transaction.getFee())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    private void updateDepositTotal() {
        if (CurrencyType.isValid(this.ticker)) {
            depositTotal = transactionHistory
                    .stream()
                    .filter(transaction -> transaction.getType() == TransactionType.DEPOSIT)
                    .map(Transaction::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    private void updateWithdrawalTotal() {
        if (CurrencyType.isValid(this.ticker)) {
            withdrawalTotal = transactionHistory
                    .stream()
                    .filter(transaction -> transaction.getType() == TransactionType.WITHDRAWAL)
                    .map(Transaction::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    /**
     * Summary the quantity of the product.
     */
    private void updateTotal(Transaction transaction) {
        totalShares = switch (transaction.getType()) {
            case DEPOSIT, BUY -> totalShares.add(transaction.getQuantity());
            case WITHDRAWAL, SELL, FEE -> totalShares.subtract(transaction.getQuantity());
            case DEBIT -> totalShares.subtract(transaction.getQuantity().multiply(transaction.getPrice()));
            case CREDIT -> totalShares.add(transaction.getQuantity().multiply(transaction.getPrice()));
            default -> totalShares;
        };
    }

    /**
     * Compute and set the average price.
     */
    private void updateAveragePrice() {
        Map<BigDecimal, BigDecimal> supply = new LinkedHashMap<>();
        transactions.forEach(transaction -> {
            if (transaction.getType() == TransactionType.BUY) {
                var price = transaction.getPrice();
                var quantity = supply.getOrDefault(price, BigDecimal.ZERO);
                supply.put(price, quantity.add(transaction.getQuantity()));

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
            var investment = price.multiply(quantity);
            totalInvestedAmount = totalInvestedAmount.add(investment);
            totalNumberOfShares = totalNumberOfShares.add(quantity);
        }

        return BigDecimals.isNullOrZero(totalInvestedAmount) && BigDecimals.isNullOrZero(totalNumberOfShares)
                ? null
                : totalInvestedAmount.divide(totalNumberOfShares, MathContext.DECIMAL64);
    }
}
