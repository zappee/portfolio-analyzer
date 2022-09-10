package com.remal.portfolio.model;

import com.remal.portfolio.util.BigDecimals;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Portfolio POJO.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Getter
public class Portfolio {

    /**
     * Portfolio name.
     */
    private final String name;

    /**
     * Products in the portfolio.
     */
    @EqualsAndHashCode.Exclude
    private final Map<String, Product> products = new LinkedHashMap<>();

    /**
     * Constructor.
     *
     * @param name portfolio name
     */
    public Portfolio(String name) {
        this.name = name;
    }

    /**
     * Adds transaction to the portfolio.
     *
     * @param transaction the transaction to add
     */
    public void addTransaction(Transaction transaction) {
        var portfolio = transaction.getPortfolio();
        var symbol = transaction.getSymbol();
        var currency = transaction.getCurrency();
        var product = products.computeIfAbsent(symbol, v -> new Product(portfolio, symbol, currency));

        product.addTransaction(transaction);
        tradePostProcessing(transaction);
    }

    /**
     * Adds a cash transaction to the portfolio after
     * buying or selling a stock.
     *
     * @param transaction transaction
     */
    private void tradePostProcessing(Transaction transaction) {
        var currency = transaction.getCurrency().name();

        if (transaction.getType() == TransactionType.BUY) {
            var cloned = transaction
                    .toBuilder()
                    .type(TransactionType.DEBIT)
                    .symbol(currency)
                    .price(BigDecimal.ONE)
                    .quantity(transaction.getQuantity().multiply(transaction.getPrice()))
                    .fee(null)
                    .build();
            products.get(currency).addTransaction(cloned);

        } else if (transaction.getType() == TransactionType.SELL || transaction.getType() == TransactionType.DIVIDEND) {
            var clonedTransaction = transaction.toBuilder().type(TransactionType.CREDIT).build();
            products.get(currency).addTransaction(clonedTransaction);
        }

        if (BigDecimals.isNotNullAndNotZero(transaction.getFee())) {
            var cloned = transaction
                    .toBuilder()
                    .type(TransactionType.FEE)
                    .fee(null)
                    .symbol(currency)
                    .quantity(transaction.getFee())
                    .price(BigDecimal.ONE)
                    .build();
            products.get(currency).addTransaction(cloned);
        }
    }
}
