package com.remal.portfolio.machine;

import com.remal.portfolio.model.Currency;
import com.remal.portfolio.model.ProductSummary;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Sorter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * <p>Debits and credits (withdrawals or deposits) are used in bookkeeping in
 * order for its books to balance. Debits increase asset or expense accounts
 * and decrease liability, revenue, or equity accounts. Credits do the reverse.
 * When making a BUY or SELL transaction on a stock market then a corresponding
 * debit/credit entry must appear under the dollar account in order to keep the
 * dollar balance synchronized with the buy/sell transaction.</p>
 * <p>
 * This class generates the missing bookkeeping transactions this way:
 *
 * BUY transaction:                    quantity: 10
 *                                     unit price: 5 EUR
 *                                     fee: 0.05 EUR
 * Generated bookkeeping transactions: WITHDRAWAL > 10 * 5 = 50.00 EUR
 *                                     WITHDRAWAL >           0.05 EUR
 *
 * SELL transaction:                   quantity: 10
 *                                     unit price: 5 EUR
 *                                     fee: 0.05 EUR
 * Generated bookkeeping transactions: DEPOSIT    > 10 * 5 = 50.00 EUR
 *                                     WITHDRAWAL >           0.05 EUR
 * </p>
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class BookkeepingTransactionGenerator {

    /**
     * The transaction list that contains the souce data.
     */
    private final List<Transaction> transactions;

    /**
     * Product summary that contains only currencies.
     */
    private final List<ProductSummary> currencySummaries = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param transactions the transaction list
     */
    public BookkeepingTransactionGenerator(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    /**
     * Updates the portfolio summary report.
     *
     * @param report the portfolio summary report
     */
    public void updateBookkepingTransactions(Map<String, Map<String, ProductSummary>> report) {
        currencySummaries.clear();
        report.forEach((portfolio, portfolioReport) ->
                portfolioReport.forEach((ticker, productSummary) -> {
                    if (Currency.getEnum(ticker) != Currency.UNKNOWN) {
                        // this is a money summary and not stock summary
                        currencySummaries.add(productSummary);
                    }
                })
        );

        generateMissingBookkeepingTransactions();
        setAveragePrices();
    }

    /**
     * It generates the missing deposit and withdrawal transactions
     * for a currency.
     */
    private void generateMissingBookkeepingTransactions() {
        currencySummaries.forEach(currencySummary -> {
            var portfolio = currencySummary.getPortfolio();
            var currency = currencySummary.getTicker();
            transactions
                    .stream()
                    .filter(t -> t.getPortfolio().equals(portfolio))
                    .filter(t -> t.getCurrency() == Currency.valueOf(currency))
                    .filter(t -> isStockTransaction(t.getType()))
                    .forEach(transaction -> {
                        if (transaction.getType() != TransactionType.EXCHANGE) {
                            var clonedTransaction = transaction
                                    .toBuilder()
                                    .fee(null)
                                    .build();
                            currencySummary.addTransaction(clonedTransaction);
                        }

                        if (BigDecimals.isNotZero(transaction.getFee())) {
                            var clonedTransaction = transaction
                                    .toBuilder()
                                    .type(TransactionType.FEE)
                                    .quantity(transaction.getFee())
                                    .price(BigDecimal.ONE)
                                    .fee(null)
                                    .build();
                            currencySummary.addTransaction(clonedTransaction);
                        }
                    });
            Sorter.sort(currencySummary.getTransactionHistory());
        });
    }

    /**
     * Calculates and set the avarage prices.
     */
    private void setAveragePrices() {
        currencySummaries.forEach(currencySummary -> {
            Function<Transaction, BigDecimal> totalMapper = lambdaFunctionForAvaragePriceCalculation(currencySummary);
            BigDecimal sum = currencySummary
                    .getTransactionHistory()
                    .stream()
                    .map(totalMapper)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            currencySummary.setAveragePrice(sum.setScale(2, RoundingMode.HALF_EVEN));
            currencySummary.setMarketValue(sum.setScale(2, RoundingMode.HALF_EVEN));
            currencySummary.setTotalShares(sum.setScale(2, RoundingMode.HALF_EVEN));
        });

    }

    /**
     * The lambda function that summarizes the values.
     *
     * @param currencySummary product summary for a currency.
     * @return the lambda function to calculate the sum
     */
    private Function<Transaction, BigDecimal> lambdaFunctionForAvaragePriceCalculation(ProductSummary currencySummary) {
        return transaction -> {
            switch (transaction.getType()) {
                case SELL:
                case DEPOSIT:
                case DIVIDEND:
                    return transaction.getQuantity().multiply(transaction.getPrice());

                case FEE:
                case WITHDRAWAL:
                case BUY:
                    return transaction.getQuantity().multiply(transaction.getPrice()).negate();

                case EXCHANGE:
                    return transaction.getTicker().equals(currencySummary.getTicker())
                        ? transaction.getQuantity().negate()
                        : transaction.getQuantity().multiply(transaction.getPrice());

                default:
                    return BigDecimal.ZERO;
            }
        };
    }

    /**
     * Check whether the given transaction type is a stock or money related.
     *
     * @param transactionType the tranzaction type to check
     * @return true if the transaction type is related to a stock transaction
     */
    private boolean isStockTransaction(TransactionType transactionType) {
        return TransactionType.BUY == transactionType
                || TransactionType.SELL == transactionType
                || TransactionType.EXCHANGE == transactionType;
    }
}
