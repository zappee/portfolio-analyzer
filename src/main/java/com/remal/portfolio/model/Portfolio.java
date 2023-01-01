package com.remal.portfolio.model;

import com.remal.portfolio.util.BigDecimals;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
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
        var currency = transaction.getPriceCurrency();
        var product = products.computeIfAbsent(symbol, v -> new Product(portfolio, symbol, currency));

        product.addTransaction(transaction);
        currencyExchangeAdministration(transaction);
        feeAdministration(transaction);
        invertTransactionAdministration(transaction);
    }

    /**
     * It generates the opposite transaction and adds it to the cash balance.
     *
     * @param transaction transaction
     */
    private void invertTransactionAdministration(Transaction transaction) {
        var type = transaction.getType();
        var acceptedTypes = Arrays.asList(TransactionType.BUY, TransactionType.SELL, TransactionType.DIVIDEND);
        var isCurrency = CurrencyType.isValid(transaction.getSymbol());

        if (!isCurrency && acceptedTypes.contains(type)) {
            var clonedTransaction = transaction
                    .toBuilder()
                    .type(invertTransactionType(transaction.getType()))
                    .quantity(transaction.getQuantity().multiply(transaction.getPrice()))
                    .price(BigDecimal.ONE)
                    .build();
            var symbol = transaction.getPriceCurrency();
            var product = getProduct(symbol.name(), symbol);
            product.addTransaction(clonedTransaction);
        }
    }

    /**
     * It generates an additional transaction in case of currency exchange.
     *
     * @param transaction transaction
     */
    private void currencyExchangeAdministration(Transaction transaction) {
        var sourceCurrency = transaction.getSymbol();
        var targetCurrency = transaction.getPriceCurrency();

        if (CurrencyType.isValid(sourceCurrency) && CurrencyType.getEnum(sourceCurrency) != targetCurrency) {
            var product = getProduct(targetCurrency.name(), targetCurrency);
            var sameCurrencies = transaction.getPriceCurrency() == transaction.getFeeCurrency();
            var exchangeTransaction = transaction
                    .toBuilder()
                    .type(invertTransactionType(transaction.getType()))
                    .price(BigDecimal.ONE)
                    .quantity(transaction.getQuantity().multiply(transaction.getPrice()))
                    .fee(sameCurrencies ? transaction.getFee() : null)
                    .build();
            product.addTransaction(exchangeTransaction);
        }
    }

    /**
     * Deducts the fee from the cash balance.
     *
     * @param transaction transaction
     */
    private void feeAdministration(Transaction transaction) {
        if (BigDecimals.isNotNullAndNotZero(transaction.getFee())) {
            var feeCurrency = transaction.getFeeCurrency();
            var product = getProduct(feeCurrency.name(), feeCurrency);
            var fee = transaction.getFee();
            product.setQuantity(product.getQuantity().subtract(fee));
        }
    }

    /**
     * Inverts the transaction type.
     * Rules:
     *    - SELL -> BUY
     *    - BUY  -> SELL
     *
     * @param type the transaction type bo invert
     * @return the inverted transaction type
     */
    private TransactionType invertTransactionType(TransactionType type) {
        return switch (type) {
            case BUY -> TransactionType.SELL;
            case SELL, DIVIDEND -> TransactionType.BUY;
            default -> type;
        };
    }

    /**
     * Returns with the requested product. If it does not exist then it will be
     * generated and added to the product list before returns it.
     *
     * @param symbol product symbol
     * @param currency the underlying currency
     * @return the product object
     */
    private Product getProduct(String symbol, CurrencyType currency) {
        return products.computeIfAbsent(symbol, p -> new Product(name, currency.name(), currency));
    }
}
