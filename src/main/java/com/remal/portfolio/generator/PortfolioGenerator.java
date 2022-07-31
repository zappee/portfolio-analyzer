package com.remal.portfolio.generator;

import com.remal.portfolio.downloader.Downloader;
import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.MultiplicityType;
import com.remal.portfolio.model.Portfolio;
import com.remal.portfolio.model.PortfolioCollection;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.model.ProviderType;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.picocli.arggroup.PortfolioArgGroup;
import com.remal.portfolio.picocli.arggroup.PortfolioInputArgGroup;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Calendars;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.writer.PriceWriter;
import com.remal.portfolio.writer.Writer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
     * The currency of the portfolio report.
     */
    @Setter
    private CurrencyType baseCurrency;

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
    private ZoneId zone;

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
     * Trade date, used for download the market price.
     */
    @Setter
    private String tradeDate;

    /**
     * Builder that initializes a new writer instance.
     *
     * @param inputArgGroup  the input CLI group
     * @param outputArgGroup the output CLI group
     * @return               the PortfolioGenerator instance
     */
    public static PortfolioGenerator build(PortfolioInputArgGroup inputArgGroup,
                                           PortfolioArgGroup.OutputArgGroup outputArgGroup) {
        var zone = ZoneId.of(outputArgGroup.getZone());
        var priceHistoryFilenameTemplate = outputArgGroup.getPriceHistoryFile();
        var priceHistoryFilename = Objects.isNull(priceHistoryFilenameTemplate)
                ? null
                : LocalDateTimes.toString(zone, priceHistoryFilenameTemplate, LocalDateTime.now());
        var providerFile = LocalDateTimes.toString(zone, inputArgGroup.getProviderFile(), LocalDateTime.now());

        PortfolioGenerator generator = new PortfolioGenerator();
        generator.setBaseCurrency(CurrencyType.getEnum(outputArgGroup.getBaseCurrency()));
        generator.setProviderFile(providerFile);
        generator.setDateTimePattern(outputArgGroup.getDateTimePattern());
        generator.setLanguage(outputArgGroup.getLanguage());
        generator.setDecimalFormat(outputArgGroup.getDecimalFormat());
        generator.setZone(zone);
        generator.setPriceHistoryFile(priceHistoryFilename);
        generator.setMultiplicity(outputArgGroup.getMultiplicity());
        generator.setWriteMode(outputArgGroup.getWriteMode());
        generator.setTradeDate(inputArgGroup.getTo());
        return generator;
    }

    /**
     * Generates the portfolio summary.
     *
     * @param transactions list of the transactions
     * @return             the portfolio summary collection
     */
    public PortfolioCollection generate(List<Transaction> transactions) {
        List<List<Portfolio>> portfolios = new ArrayList<>();
        transactions.forEach(transaction -> addTransactionToPortfolio(portfolios, transaction));

        if (Objects.nonNull(providerFile)) {
            portfolios.forEach(productSummaries -> productSummaries.forEach(this::updateMarketValue));
        }

        portfolios.forEach(p -> p.sort(Comparator.comparing(Portfolio::getTicker)));

        var tradeDateAsDate = Objects.isNull(tradeDate)
                ? LocalDateTime.now().atZone(zone).toLocalDateTime()
                : LocalDateTimes.toLocalDateTime(zone, dateTimePattern, tradeDate);

        var portfolioCollection = PortfolioCollection
                .builder()
                .generated(tradeDateAsDate)
                .portfolios(portfolios)
                .build();

        updateTotals(portfolioCollection);
        updateTotalEquity(portfolioCollection);
        return portfolioCollection;
    }

    /**
     * Updates the value of the totals.
     *
     * @param portfolioCollection the portfolio
     */
    private void updateTotals(PortfolioCollection portfolioCollection) {
        portfolioCollection.setDepositTotal(BigDecimal.ZERO);
        portfolioCollection.setMarketValue(BigDecimal.ZERO);
        portfolioCollection.setInvestedAmount(BigDecimal.ZERO);
        portfolioCollection.setProfitAndLoss(BigDecimal.ZERO);
        portfolioCollection.setCashInPortfolio(new HashMap<>());

        portfolioCollection.getPortfolios().forEach(portfolio -> portfolio.forEach(product -> {
            portfolioCollection.setDepositTotal(
                    portfolioCollection.getDepositTotal().add(BigDecimals.zeroIfNull(product.getDepositTotal())));

            portfolioCollection.setMarketValue(
                    portfolioCollection.getMarketValue().add(BigDecimals.zeroIfNull(product.getMarketValue())));

            portfolioCollection.setInvestedAmount(
                    portfolioCollection.getInvestedAmount().add(BigDecimals.zeroIfNull(product.getInvestedAmount())));

            portfolioCollection.setProfitAndLoss(
                    portfolioCollection.getProfitAndLoss().add(BigDecimals.zeroIfNull(product.getProfitAndLoss())));

            if (CurrencyType.isValid(product.getTicker())) {
                var key = product.getTicker();
                var map = portfolioCollection.getCashInPortfolio();
                map.putIfAbsent(key, BigDecimal.ZERO);
                map.put(key, map.get(key).add(product.getTotalShares()));
            }
        }));
    }

    /**
     * Updates the value of the Total Equity.
     *
     * @param portfolioCollection the portfolio
     */
    private void updateTotalEquity(PortfolioCollection portfolioCollection) {
        portfolioCollection.setTotalEquity(portfolioCollection.getMarketValue());

        portfolioCollection.getCashInPortfolio().forEach((currency, value) -> {
            if (currency.equals(baseCurrency.name())) {
                portfolioCollection.setTotalEquity(portfolioCollection.getTotalEquity().add(value));
            } else {
                // exchange the cash to base currency
                // ticker: USDEUR=X: 0.9778
                var ticker = currency + "/" + baseCurrency.name();
                var price = getMarketPrice(ticker);
                portfolioCollection.setTotalEquity(portfolioCollection.getTotalEquity().add(value.multiply(price)));
            }
        });
    }

    /**
     * Update the market price and the market value.
     *
     * @param portfolio the portfolio instance
     */
    private void updateMarketValue(Portfolio portfolio) {
        var ticker = portfolio.getTicker();
        var marketUnitPrice = CurrencyType.isValid(ticker) ? null : getMarketPrice(ticker);
        var marketValue = Optional
                .ofNullable(marketUnitPrice)
                .map(x -> x.multiply(portfolio.getTotalShares()));
        var investedAmount = Optional
                .ofNullable(portfolio.getAveragePrice())
                .map(x -> x.multiply(portfolio.getTotalShares()));
        var profit = investedAmount.flatMap(x -> marketValue.map(y -> y.subtract(x)));
        var profitPercent = investedAmount.flatMap(x -> marketValue
                .map(y -> y.divide(x, 4, RoundingMode.HALF_EVEN).movePointRight(2))
                .map(z -> z.subtract(BigDecimal.valueOf(100))));

        portfolio.setMarketUnitPrice(marketUnitPrice);
        portfolio.setInvestedAmount(investedAmount.orElse(null));
        portfolio.setMarketValue(marketValue.orElse(null));
        portfolio.setProfitAndLoss(profit.orElse(null));
        portfolio.setProfitLossPercent(profitPercent.orElse(null));
    }

    /**
     * Get the market price.
     *
     * @param ticker abbreviation used to uniquely identify the traded shares
     * @return       the market price
     */
    private BigDecimal getMarketPrice(String ticker) {
        var providerType = ProviderType.getProvider(ticker, providerFile);
        if (Objects.isNull(providerType)) {
            Logger.logErrorAndExit("Market price data provider not defined in the {} file.", providerFile);
        }

        var tickerAlias = ProviderType.getTicker(ticker, providerFile);
        var price = Objects.isNull(tradeDate)
                ? DOWNLOADER.get(providerType).getPrice(tickerAlias)
                : DOWNLOADER.get(providerType).getPrice(tickerAlias, Calendars.fromString(tradeDate, dateTimePattern));
        price.ifPresent(p -> p.setTicker(ticker));

        saveMarketPrice(price.orElse(null));
        return price.map(Price::getUnitPrice).orElse(null);
    }

    /**
     * Save market price to file.
     *
     * @param price the downloaded market price
     */
    private void saveMarketPrice(Price price) {
        if (Objects.nonNull(price)) {
            List<Price> prices = new ArrayList<>();
            prices.add(price);

            // write price to the history file
            if (Objects.nonNull(priceHistoryFile)) {
                Writer<Price> writer = PriceWriter.build(buildOutputArgGroup());
                writer.write(writeMode, priceHistoryFile, prices);
            }
        }
    }

    /**
     * Builds an OutputArgGroup.
     *
     * @return the argument group instance
     */
    private PriceArgGroup.OutputArgGroup buildOutputArgGroup() {
        PriceArgGroup.OutputArgGroup outputArgGroup = new PriceArgGroup.OutputArgGroup();
        outputArgGroup.setZone(zone.getId());
        outputArgGroup.setDateTimePattern(dateTimePattern);
        outputArgGroup.setLanguage(language);
        outputArgGroup.setDecimalFormat(decimalFormat);
        outputArgGroup.setMultiplicity(multiplicity);
        return outputArgGroup;
    }

    /**
     * Adds a transaction to the portfolio summary.
     *
     * @param portfolios  the product summary
     * @param transaction transaction to be added to the summary
     */
    private void addTransactionToPortfolio(List<List<Portfolio>> portfolios, Transaction transaction) {
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
                    .type(TransactionType.FEE)
                    .fee(null)
                    .ticker(transaction.getCurrency().name())
                    .quantity(transaction.getFee())
                    .price(BigDecimal.ONE);
            productSummary.addTransaction(clonedTransaction.build());
        }
    }

    /**
     * Returns with a Portfolio instance. If the belonging instance does not exist
     * then it will be generated as an empty but initialized object.
     *
     * @param portfolios the collection of the Portfolio
     * @param portfolio  name of the portfolio
     * @param ticker     name of the product
     * @return           the Portfolio instances that represents the data for the report
     */
    private Portfolio getProductSummary(List<List<Portfolio>> portfolios, String portfolio, String ticker) {
        var selectedPortfolio = portfolios
                .stream()
                .filter(productSummaries ->
                        productSummaries
                                .stream()
                                .anyMatch(productSummary -> productSummary.getName().equals(portfolio)))
                .findFirst()
                .orElseGet(() -> {
                    var ps = new ArrayList<Portfolio>();
                    portfolios.add(ps);
                    return ps;
                });

        return selectedPortfolio
                .stream()
                .filter(p -> p.getTicker().equals(ticker))
                .findFirst()
                .orElseGet(() -> {
                    var p = new Portfolio(portfolio, ticker);
                    selectedPortfolio.add(p);
                    return p;
                });
    }
}
