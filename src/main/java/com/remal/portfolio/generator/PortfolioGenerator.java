package com.remal.portfolio.generator;

import com.remal.portfolio.downloader.Downloader;
import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.MultiplicityType;
import com.remal.portfolio.model.ProductPrice;
import com.remal.portfolio.model.ProductSummary;
import com.remal.portfolio.model.ProviderType;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.picocli.arggroup.SummaryArgGroup;
import com.remal.portfolio.picocli.arggroup.SummaryInputArgGroup;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.writer.ProductPriceWriter;
import com.remal.portfolio.writer.Writer;
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
     * Date/time pattern that is used for converting string to LocalDateTime.
     */
    @Setter
    private String dateTimePattern = "yyyy-MM-dd HH:mm:ss";

    /**
     * Report language.
     */
    @Setter
    private String language = "en";

    /**
     * Controls how the decimal numbers will be converted to String.
     */
    @Setter
    private String decimalFormat = "###,###,###,###,###,###.########";

    /**
     * If not null then date/time conversions will perform.
     */
    @Setter
    private String zone;

    /**
     * Set the data provider properties file.
     */
    @Setter
    private String providerFile;

    /**
     * Set the price history file.
     */
    @Setter
    private String priceHistoryFile;

    /**
     *  Set the file open mode.
     */
    @Setter
    private FileWriter.WriteMode writeMode;

    /**
     * Controls the price export to file.
     */
    @Setter
    private MultiplicityType multiplicity;

    /**
     * Builder that initializes a new writer instance.
     *
     * @param inputArgGroup  the input CLI group
     * @param outputArgGroup the output CLI group
     * @return               the PortfolioGenerator instance
     */
    public static PortfolioGenerator build(SummaryInputArgGroup inputArgGroup,
                                           SummaryArgGroup.OutputArgGroup outputArgGroup) {
        PortfolioGenerator generator = new PortfolioGenerator();
        generator.setProviderFile(inputArgGroup.getProviderFile());
        generator.setDateTimePattern(outputArgGroup.getDateTimePattern());
        generator.setLanguage(outputArgGroup.getLanguage());
        generator.setDecimalFormat(outputArgGroup.getDecimalFormat());
        generator.setZone(outputArgGroup.getZone());
        generator.setPriceHistoryFile(outputArgGroup.getPriceHistoryFile());
        generator.setMultiplicity(outputArgGroup.getMultiplicity());
        generator.setWriteMode(outputArgGroup.getWriteMode());
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
        var marketUnitPrice = CurrencyType.isValid(ticker) ? null : getProductMarketPrice(ticker);
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
    private BigDecimal getProductMarketPrice(String ticker) {
        var providerType = ProviderType.getProvider(ticker, providerFile);
        if (Objects.isNull(providerType)) {
            Logger.logErrorAndExit("Market price data provider not defined in the {} file.", providerFile);
        }

        var tickerAlias = ProviderType.getTicker(ticker, providerFile);
        var productPrice = DOWNLOADER.get(providerType).getPrice(tickerAlias);
        productPrice.ifPresent(p -> p.setTicker(ticker));

        saveMarketPrice(productPrice.orElse(null));
        return productPrice.map(ProductPrice::getPrice).orElse(null);
    }

    /**
     * Save market price to file.
     *
     * @param productPrice the downloaded market price
     */
    private void saveMarketPrice(ProductPrice productPrice) {
        if (Objects.nonNull(priceHistoryFile) && Objects.nonNull(productPrice)) {
            // read the history file
            PriceArgGroup.OutputArgGroup outputArgGroup = new PriceArgGroup.OutputArgGroup();
            outputArgGroup.setDateTimePattern(dateTimePattern);
            outputArgGroup.setLanguage(language);
            outputArgGroup.setDecimalFormat(decimalFormat);
            outputArgGroup.setZone(zone);

            Parser<ProductPrice> parser = Parser.build(outputArgGroup);
            List<ProductPrice> productPrices = new ArrayList<>(parser.parse(priceHistoryFile));

            // merge
            ProductPrice.merge(productPrices, productPrice, multiplicity, outputArgGroup.getZone());

            // writer
            Writer<ProductPrice> writer = ProductPriceWriter.build(outputArgGroup);
            writer.write(writeMode, priceHistoryFile, productPrices);
        }
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
