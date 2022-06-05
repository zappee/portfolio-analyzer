package com.remal.portfolio.generator;

import com.remal.portfolio.downloader.Downloader;
import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.ProductPrice;
import com.remal.portfolio.model.ProductSummary;
import com.remal.portfolio.model.ProviderType;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Logger;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Builds the portfolio summary report based on the given transaction list.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 *
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class PortfolioGenerator {

    /**
     * The market price downloader instances.
     */
    private static final Map<ProviderType, Downloader> DOWNLOADER = Downloader.initializeDownloader();

    /**
     * Set the data provider properties file.
     */
    @Setter
    private String providerFile;

    /**
     * Builder that initializes a new writer instance.
     *
     * @param providerFile the data provider properties file
     * @return the PortfolioGenerator instance
     */
    public static PortfolioGenerator build(String providerFile) {
        PortfolioGenerator generator = new PortfolioGenerator();
        generator.setProviderFile(providerFile);
        return generator;
    }

    /**
     * Generates the portfolio summary.
     *
     * @param transactions list of the transactions
     * @return             the portfolio summary data
     */
    public List<List<ProductSummary>> generate(List<Transaction> transactions) {
        List<List<ProductSummary>> portfolios = new ArrayList<>();
        transactions.forEach(transaction -> addTransactionToPortfolio(portfolios, transaction));

        if (Objects.nonNull(providerFile)) {
            portfolios.forEach(productSummaries -> productSummaries.forEach(this::updateMarketValue));
        }

        return portfolios;
    }

    /**
     * Update the market price and the market value.
     *
     * @param summary the product summary instance
     */
    private void updateMarketValue(ProductSummary summary) {
        var ticker = summary.getTicker();
        var marketUnitPrice = CurrencyType.isValid(ticker) ? null : getUnitMarketPrice(ticker);
        var marketValue = Optional
                .ofNullable(marketUnitPrice)
                .map(x -> x.multiply(summary.getTotalShares()));
        var investedAmount = Optional
                .ofNullable(summary.getAveragePrice())
                .map(x -> x.multiply(summary.getTotalShares()));
        var profit = investedAmount.flatMap(x -> marketValue.map(y -> y.subtract(x)));
        var profitPercent = investedAmount.flatMap(x -> marketValue
                .map(y -> y.divide(x, 4, RoundingMode.HALF_EVEN).movePointRight(2))
                .map(z -> z.subtract(BigDecimal.valueOf(100))));

        summary.setMarketUnitPrice(marketUnitPrice);
        summary.setInvestedAmount(investedAmount.orElse(null));
        summary.setMarketValue(marketValue.orElse(null));
        summary.setProfitLoss(profit.orElse(null));
        summary.setProfitLossPercent(profitPercent.orElse(null));
    }

    /**
     * Get the market price.
     *
     * @param ticker abbreviation used to uniquely identify the traded shares
     * @return       the market price
     */
    private BigDecimal getUnitMarketPrice(String ticker) {
        var providerType = ProviderType.getProvider(ticker, providerFile);
        if (Objects.isNull(providerType)) {
            Logger.logErrorAndExit("Market price data provider not defined in the {} file.", providerFile);
        }

        var translatedTicker = ProviderType.getTicker(ticker, providerFile);
        var price = DOWNLOADER.get(providerType).getPrice(translatedTicker);
        return price.orElse(ProductPrice.builder().price(null).build()).getPrice();
    }

    /**
     * Adds a transaction to the portfolio summary.
     *
     * @param portfolios  the product summary
     * @param transaction transaction to be added to the summary
     */
    private void addTransactionToPortfolio(List<List<ProductSummary>> portfolios, Transaction transaction) {
        var portfolio = transaction.getPortfolio();
        var ticker = transaction.getTicker();
        var productSummary = getProductSummary(portfolios, portfolio, ticker);

        productSummary.addTransaction(transaction);

        // generating and adding extra cash transactions for buy, sell, fees and dividends
        if (transaction.getType() == TransactionType.BUY) {
            ticker = transaction.getCurrency().name();
            productSummary = getProductSummary(portfolios, portfolio, ticker);
            productSummary.addTransaction(transaction.toBuilder().type(TransactionType.DEBIT).build());
        } else if (transaction.getType() == TransactionType.SELL || transaction.getType() == TransactionType.DIVIDEND) {
            ticker = transaction.getCurrency().name();
            productSummary = getProductSummary(portfolios, portfolio, ticker);
            productSummary.addTransaction(transaction.toBuilder().type(TransactionType.CREDIT).build());
        }

        if (BigDecimals.isNotZero(transaction.getFee())) {
            ticker = transaction.getCurrency().name();
            productSummary = getProductSummary(portfolios, portfolio, ticker);
            var clonedTransaction = transaction
                    .toBuilder()
                    .type(TransactionType.FEE)//getTypeForAdditionalTransaction(transaction.getType()))
                    .fee(null)
                    .ticker(transaction.getCurrency().name())
                    .quantity(transaction.getFee())
                    .price(BigDecimal.ONE);
            productSummary.addTransaction(clonedTransaction.build());
        }
    }

    /**
     * Returns with a ProductSummary instance. If the belonging instance does not exist
     * then it will be generated as an empty but initialized object.
     *
     * @param portfolios the collection of the ProductSummary items
     * @param portfolio  name of the portfolio
     * @param ticker     name of the product
     * @return           the ProductSummary instances that represents the data for the report
     */
    private ProductSummary getProductSummary(List<List<ProductSummary>> portfolios, String portfolio, String ticker) {
        var selectedPortfolio = portfolios
                .stream()
                .filter(productSummaries ->
                        productSummaries
                                .stream()
                                .anyMatch(productSummary -> productSummary.getPortfolio().equals(portfolio)))
                .findFirst()
                .orElseGet(() -> {
                    var productSummaries = new ArrayList<ProductSummary>();
                    portfolios.add(productSummaries);
                    return productSummaries;
                });

        return selectedPortfolio
                .stream()
                .filter(actualProductSummary -> actualProductSummary.getTicker().equals(ticker))
                .findFirst()
                .orElseGet(() -> {
                    var p = new ProductSummary(portfolio, ticker);
                    selectedPortfolio.add(p);
                    return p;
                });
    }
}
