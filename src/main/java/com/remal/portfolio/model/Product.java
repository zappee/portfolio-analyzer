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
import java.util.concurrent.atomic.AtomicReference;

/**
 * Product summary.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Getter
@Slf4j
public class Product {

    /**
     * Portfolio name.
     */
    private final String portfolio;

    /**
     * Product name.
     */
    private final String symbol;

    /**
     * The currency of the stock price.
     */
    private final CurrencyType currency;

    /**
     * The number of the shares that she owns.
     */
    private BigDecimal quantity = BigDecimal.ZERO;

    /**
     *  The average price is calculated by dividing your cost (execution
     *  price + commission) by the quantity of your position. This value is
     *  used to determine your P&L.
     */
    private BigDecimal averagePrice;

    /**
     *  Market price represents the market valuation per share of a company.
     */
    @Setter
    private Price marketPrice;

    /**
     * The sum of the deposits.
     */
    private BigDecimal deposits;

    /**
     * The sum of the withdrawals.
     */
    private BigDecimal withdrawals;

    /**
     * Trading fee and costs together.
     */
    private BigDecimal costs;

    /**
     * The transactions that are relevant.
     */
    private final List<Transaction> transactions = new ArrayList<>();

    /**
     * The complete list of the transactions.
     */
    private final List<Transaction> transactionHistory = new ArrayList<>();

    /**
     * The actual supply.
     * Map structure:
     *      key:   price
     *      value: quantity
     */
    private final Map<BigDecimal, BigDecimal> supply = new LinkedHashMap<>();

    /**
     * Constructor.
     *
     * @param portfolio portfolio name
     * @param symbol product name
     * @param currency currency for the prices
     */
    public Product(String portfolio, String symbol, CurrencyType currency) {
        this.portfolio = portfolio;
        this.symbol = symbol;
        this.currency = currency;
    }

    /**
     * Adds a new transaction to the summary report.
     * @param transaction the transaction to add
     */
    public void addTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
        transactions.add(transaction);

        updateQuantity(transaction);
        updateSupply();
        updateAveragePrice();
        updateCosts();
        updateDeposits();
        updateWithdrawals();
    }

    /**
     * The current market value of the holding.
     *
     * @return market value of the product
     */
    public BigDecimal getMarketValue() {
        if (Objects.isNull(marketPrice)) {
            return null;
        } else {
            return quantity
                    .multiply(marketPrice.getUnitPrice())
                    .setScale(BigDecimals.SCALE_DEFAULT, BigDecimals.ROUNDING_MODE);
        }
    }

    /**
     * Invested amount.
     *
     * @return value of the invested amount
     */
    public BigDecimal getInvestedAmount() {
        if (isCurrency()) {
            return null;
        }

        var avgPrice = getAveragePrice();
        return Objects.isNull(avgPrice)
                ? null
                : quantity.multiply(avgPrice).setScale(BigDecimals.SCALE_DEFAULT, BigDecimals.ROUNDING_MODE);
    }

    /**
     * Check whether the product is a currency or not.
     *
     * @return true if it is a currency product
     */
    public boolean isCurrency() {
        return CurrencyType.isValid(symbol);
    }

    /**
     * P/L on the investment.
     *
     * @return value of the profit and loss
     */
    public BigDecimal getProfitAndLoss() {
        var investedAmount = getInvestedAmount();
        var marketValue = getMarketValue();
        return Objects.nonNull(investedAmount) && Objects.nonNull(marketValue)
                ? marketValue.subtract(investedAmount)
                : null;
    }

    /**
     * P/L on the investment.
     *
     * @return value of the profit and loss in percent
     */
    public BigDecimal getProfitAndLossPercent() {
        var hundred = BigDecimal.valueOf(100);
        var investedAmount = getInvestedAmount();
        var marketValue = getMarketValue();
        if (Objects.isNull(investedAmount) || Objects.isNull(marketValue)) {
            return null;
        } else {
            return marketValue
                    .divide(investedAmount, MathContext.DECIMAL64)
                    .multiply(hundred)
                    .setScale(BigDecimals.SCALE_DEFAULT, BigDecimals.ROUNDING_MODE);
        }
    }

    /**
     * Updates the quantity value.
     *
     * @param transaction transaction
     */
    private void updateQuantity(Transaction transaction) {
        quantity = switch (transaction.getType()) {
            case DEPOSIT, BUY -> quantity.add(transaction.getQuantity());
            case WITHDRAWAL, SELL, FEE -> quantity.subtract(transaction.getQuantity());
            case DEBIT -> quantity.subtract(transaction.getQuantity().multiply(transaction.getPrice()));
            case CREDIT -> quantity.add(transaction.getQuantity().multiply(transaction.getPrice()));
            default -> quantity;
        };

        if (BigDecimals.isNullOrZero(quantity)) {
            transactions.clear();
        }
    }

    /**
     * Updates the supply list.
     */
    private void updateSupply() {
        supply.clear();

        for (Transaction transaction : transactions) {
            switch (transaction.getType()) {
                case BUY, DEPOSIT, CREDIT -> {
                    var c = transaction.getPriceCurrency().name();
                    var price = CurrencyType.isValid(c) ? BigDecimal.ONE : transaction.getPrice();
                    var volume = supply.getOrDefault(price, BigDecimal.ZERO);
                    supply.put(price, volume.add(transaction.getQuantity()));
                }
                case SELL, WITHDRAWAL -> {
                    if (InventoryValuationType.FIFO == transaction.getInventoryValuation()) {
                            updateSupplyBasedOnFifoSell(supply, transaction);
                        } else {
                            updateSupplyBasedOnLifoSell(supply, transaction);
                        }
                }
                default -> {
                    // do nothing here
                }
            }
        }
    }

    /**
     * Computing the average price based on the supply.
     */
    private void updateAveragePrice() {
        if (supply.isEmpty()) {
            averagePrice = null;
        } else {
            final AtomicReference<BigDecimal> amountInvested = new AtomicReference<>(BigDecimal.ZERO);
            final AtomicReference<BigDecimal> sharesBought = new AtomicReference<>(BigDecimal.ZERO);

            supply.forEach((price, volume) -> {
                amountInvested.set(amountInvested.get().add(price.multiply(volume)));
                sharesBought.set(sharesBought.get().add(volume));
            });

            if (BigDecimals.isNotNullAndNotZero(amountInvested.get())) {
                averagePrice = amountInvested.get().divide(sharesBought.get(), MathContext.DECIMAL64);
            }
        }
    }

    /**
     * Calculates the amount of the total fee.
     */
    private void updateCosts() {
        costs = transactions
                .stream()
                .map(transaction -> Objects.isNull(transaction.getFee()) ? BigDecimal.ZERO : transaction.getFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(BigDecimals.SCALE_DEFAULT, BigDecimals.ROUNDING_MODE);
    }

    /**
     * Calculates the amount of the total deposits.
     */
    private void updateDeposits() {
        deposits = transactions
                .stream()
                .filter(transaction -> transaction.getType() == TransactionType.DEPOSIT)
                .map(Transaction::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the amount of the total withdrawals.
     */
    private void updateWithdrawals() {
        withdrawals = transactions
                .stream()
                .filter(transaction -> transaction.getType() == TransactionType.WITHDRAWAL)
                .map(Transaction::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
}
