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
    @Setter
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
    private final Map<String, BigDecimal> fees = new LinkedHashMap<>();

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
        updateSupply(transaction);
        updateFees(transaction);
        updateDeposits(transaction);
        updateWithdrawals(transaction);
        updateAveragePrice();
    }

    /**
     * The current market value of the holding.
     *
     * @return market value of the product
     */
    public BigDecimal getMarketValue() {
        if (Objects.isNull(marketPrice)) {
            return null;
        }

        var price = marketPrice.getUnitPrice();
        return quantity.multiply(price).setScale(BigDecimals.SCALE_DEFAULT, BigDecimals.ROUNDING_MODE);
    }

    /**
     * Invested amount.
     *
     * @return value of the invested amount
     */
    public BigDecimal getInvestedAmount() {
        var isCurrency = CurrencyType.isValid(symbol);
        var avgPrice = getAveragePrice();
        return (isCurrency || BigDecimals.isNullOrZero(avgPrice))
                ? null
                : quantity.multiply(avgPrice).setScale(BigDecimals.SCALE_DEFAULT, BigDecimals.ROUNDING_MODE);
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
        var boughtPrice = getAveragePrice();
        var currentPrice = getMarketPrice();

        return BigDecimals.isNotNullAndNotZero(boughtPrice)
                && Objects.nonNull(currentPrice) && BigDecimals.isNotNullAndNotZero(currentPrice.getUnitPrice())

                ? currentPrice.getUnitPrice()
                    .subtract(boughtPrice)
                    .divide(boughtPrice, MathContext.DECIMAL64)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(BigDecimals.SCALE_DEFAULT, BigDecimals.ROUNDING_MODE)
                : null;
    }

    /**
     * Updates the quantity value.
     *
     * @param transaction transaction
     */
    private void updateQuantity(Transaction transaction) {
        var qty = Objects.isNull(transaction.getQuantity()) ? BigDecimal.ZERO : transaction.getQuantity();
        quantity = switch (transaction.getType()) {
            case DEPOSIT, BUY, TRANSFER_IN -> quantity.add(qty);
            case WITHDRAWAL, SELL, FEE, TRANSFER_OUT -> quantity.subtract(qty);
            default -> quantity;
        };

        if (BigDecimals.isNullOrZero(quantity)) {
            transactions.clear();
        }
    }

    /**
     * Updates the supply list.
     *
     * @param transaction the transaction to be added
     */
    private void updateSupply(Transaction transaction) {
        switch (transaction.getType()) {
            case BUY, DEPOSIT, TRANSFER_IN -> {
                var price = CurrencyType.isValid(transaction.getSymbol()) ? BigDecimal.ONE : transaction.getPrice();
                var qty = Objects.isNull(transaction.getQuantity()) ? BigDecimal.ZERO : transaction.getQuantity();
                var currentVolume = supply.getOrDefault(price, BigDecimal.ZERO);
                var newVolume = currentVolume.add(qty);
                supply.put(price, newVolume);
            }
            case SELL, WITHDRAWAL, TRANSFER_OUT -> {
                var isFifo = InventoryValuationType.FIFO == transaction.getInventoryValuation();
                if (isFifo) {
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

    /**
     * Calculates the amount of the total fee.
     *
     * @param transaction the transaction to be added
     */
    private void updateFees(Transaction transaction) {
        var feeCurrency = transaction.getFeeCurrency();
        var fee = transaction.getFee();
        if (Objects.nonNull(fee) && Objects.nonNull(feeCurrency)) {
            var feeTotal = fees.computeIfAbsent(feeCurrency.name(), x -> BigDecimal.ZERO);
            fees.put(feeCurrency.name(), feeTotal.add(fee));
        }
    }

    /**
     * Calculates the amount of the total deposits.
     *
     * @param transaction the transaction to be added
     */
    private void updateDeposits(Transaction transaction) {
        if (transaction.getType() == TransactionType.DEPOSIT) {
            var qty = transaction.getQuantity();
            deposits = Objects.isNull(deposits) ? qty : deposits.add(qty);
        }
    }

    /**
     * Calculates the amount of the total withdrawals.
     *
     * @param transaction the transaction to be added
     */
    private void updateWithdrawals(Transaction transaction) {
        if (transaction.getType() == TransactionType.WITHDRAWAL) {
            var qty = transaction.getQuantity();
            withdrawals = Objects.isNull(withdrawals) ? qty : withdrawals.add(qty);
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

            if (BigDecimals.isNotNullAndNotZero(sharesBought.get())) {
                averagePrice = amountInvested.get().divide(sharesBought.get(), MathContext.DECIMAL64);
            }
        }
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
        var isQuantitySet = Objects.nonNull(transaction.getQuantity());
        var quantityToSell = isQuantitySet ? transaction.getQuantity() : BigDecimal.ZERO;

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
