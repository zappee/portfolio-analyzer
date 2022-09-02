package com.remal.portfolio.writer;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.PortfolioReport;
import com.remal.portfolio.picocli.arggroup.PortfolioArgGroup;
import com.remal.portfolio.picocli.arggroup.PortfolioInputArgGroup;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Portfolio summary writer.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class PortfolioWriter extends Writer<PortfolioReport> {

    /**
     * The currency of the portfolio report.
     */
    @Setter
    private CurrencyType baseCurrency;

    /**
     * Set the product name filter.
     */
    @Setter
    private List<String> symbolsToShow = new ArrayList<>();

    /**
     * The total invested amount.
     */
    //private final Map<String, BigDecimal> depositTotal = new HashMap<>();

    /**
     * Portfolio market value
     */
    //private final Map<String, BigDecimal> marketValue = new HashMap<>();

    /**
     * Invested amount.
     */
    //private BigDecimal investedAmount;

    /**
     * P/L on portfolio.
     */
    //private final Map<String, BigDecimal> profitAndLoss = new HashMap<>();

    /**
     * Cash in portfolio.
     */
    //private final Map<String, BigDecimal> cashInPortfolio = new HashMap<>();

    /**
     * The account value, also known as total equity, is the total dollar value of all
     * the holdings of the trading account, not just the securities, but the cash as
     * well.
     */
    //private BigDecimal accountValue;

    /**
     * Builder that initializes a new writer instance.
     *
     * @param inputArgGroup the input CLI group
     * @param outputArgGroup the output CLI group
     * @return the writer instance
     */
    public static PortfolioWriter build(PortfolioInputArgGroup inputArgGroup,
                                        PortfolioArgGroup.OutputArgGroup outputArgGroup) {

        var writer = new PortfolioWriter();
        writer.setBaseCurrency(CurrencyType.getEnum(outputArgGroup.getBaseCurrency()));
        writer.setSymbolsToShow(inputArgGroup.getSymbols());
        writer.setHideTitle(outputArgGroup.isHideTitle());
        writer.setHideHeader(outputArgGroup.isHideHeader());
        writer.setLanguage(outputArgGroup.getLanguage());
        writer.setDateTimePattern(outputArgGroup.getDateTimePattern());
        writer.setZone(ZoneId.of(outputArgGroup.getZone()));
        writer.setColumnsToHide(outputArgGroup.getColumnsToHide());
        return writer;
    }

    /**
     * Generate the CSV report.
     *
     * @param items data
     * @return the report content as a String
     */
    @Override
    protected String buildCsvReport(List<PortfolioReport> items) {
       /* items.addAll((new PortfolioParser()).parse("'filename.md'"));
        items.sort(Comparator.comparing(PortfolioCollection::getGenerated));

        var report = new StringBuilder();
        report.append(buildTableHeader(items));
        items.forEach(summaries -> {
            report
                    .append(LocalDateTimes.toString(zone, dateTimePattern, summaries.getGenerated()))
                    .append(csvSeparator);
            summaries.getPortfolios().forEach(portfolio -> portfolio
                    .stream()
                    .filter(summary -> BigDecimals.isNotNullAndNotZero(summary.getTotalShares()))
                    .forEach(summary -> report.append(buildReportItem(summary))));
        });
        report.setLength(report.length() - csvSeparator.length());
        return report.toString();/*

        */

        throw new UnsupportedOperationException();
    }

    /**
     * Generate the Excel report.
     *
     * @param items data
     * @return the report content as bytes
     */
    @Override
    protected byte[] buildExcelReport(List<PortfolioReport> items) {
        throw new UnsupportedOperationException();
    }

    /**
     * Generate the Text/Markdown report.
     *
     * @param items data
     * @return the report content as a String
     */
    @Override
    protected String buildMarkdownReport(List<PortfolioReport> items) {
        var portfolioReport = items.stream().findFirst().orElse(new PortfolioReport(CurrencyType.EUR));
        var report = new StringBuilder();
        var widths = calculateColumnWidth(portfolioReport);

        Sorter.sortPortfolioReport(portfolioReport);

        // report title
        if (!hideTitle) {
            report.append(generateTitle(portfolioReport.getGenerated()));
        }

        // table header
        if (!hideHeader) {
            report.append(generateHeader(widths));
        }

        // data
        portfolioReport.getPortfolios().forEach((portfolioName, portfolio) ->
                portfolio.getProducts()
                        .entrySet()
                        .stream()
                        .filter(p -> symbolsToShow.isEmpty() || symbolsToShow.contains(p.getValue().getSymbol().trim()))
                        .forEach(p -> {
                            var product = p.getValue();
                            var profitAndLossPercent = product.getProfitAndLossPercent();

                            if (BigDecimals.isNotZero(product.getQuantity())) {
                                var price = product.getMarketPrice().getUnitPrice();
                                report
                                    .append(getCell(Label.HEADER_PORTFOLIO, portfolio.getName(), widths))
                                    .append(getCell(Label.HEADER_SYMBOL, product.getSymbol(), widths))
                                    .append(getCell(Label.HEADER_QUANTITY, product.getQuantity(), widths))
                                    .append(getCell(Label.HEADER_AVG_PRICE, product.getAveragePrice(), widths))
                                    .append(getCell(Label.HEADER_MARKET_UNIT_PRICE, price, widths))
                                    .append(getCell(Label.HEADER_MARKET_VALUE, product.getMarketValue(), widths))
                                    .append(getCell(Label.HEADER_INVESTED_AMOUNT, product.getInvestedAmount(), widths))
                                    .append(getCell(Label.HEADER_PROFIT_LOSS, product.getProfitAndLoss(), widths))
                                    .append(getCell(Label.HEADER_PROFIT_LOSS_PERCENT, profitAndLossPercent, widths))
                                    .append(getCell(Label.HEADER_COSTS, product.getCosts(), widths))
                                    .append(getCell(Label.HEADER_DEPOSITS, product.getDeposits(), widths))
                                    .append(getCell(Label.HEADER_WITHDRAWALS, product.getWithdrawals(), widths))
                                    .append(markdownSeparator)
                                    .append(NEW_LINE);
                            }
                        })
        );

        // totals
        report.append(generateSummary(portfolioReport));

        return report.toString();
    }

    /**
     * Generates the report summary.
     * @return the report summary
     */
    private StringBuilder generateSummary(PortfolioReport portfolioReport) {
        var labelWidth = LabelCollection.PRODUCT_SUMMARY_FOOTER
                .stream()
                .max(Comparator.comparingInt( x -> x.getLabel(language).length()))
                .get()
                .getLabel(language).length();

        return new StringBuilder()
                .append(NEW_LINE)
                .append(mapToString(
                        Label.LABEL_TOTAL_CASH_PER_CURRENCY,
                        labelWidth,
                        portfolioReport.getCashInPortfolio()))

                .append(Label.LABEL_TOTAL_CASH.getLabel(language).replace("{1}", baseCurrency.name()))
                .append(": ").append(getCashInBaseCurrency(portfolioReport)).append(NEW_LINE)

                .append(Label.LABEL_TOTAL_DEPOSIT.getLabel(language)).append(NEW_LINE)
                .append(Label.LABEL_TOTAL_WITHDRAWALS.getLabel(language)).append(NEW_LINE)
                .append(Label.LABEL_TOTAL_INVESTMENT.getLabel(language)).append(NEW_LINE);
    }

    /**
     * Exchange the cash amounts that are available in the
     * portfolios to the base currency.
     *
     * @param portfolioReport portfolio report
     * @return cash total in base currency
     */
    private BigDecimal getCashInBaseCurrency(PortfolioReport portfolioReport) {
        AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);
        portfolioReport.getCashInPortfolio().forEach((symbol, quantity) -> {
            var exchangeRateSymbol = symbol + "-" + baseCurrency;
            var exchangeRate = portfolioReport.getExchangeRates().get(exchangeRateSymbol);
            if (Objects.isNull(exchangeRate)) {
                total.set(total.get().add(quantity));
            } else {
                total.set(total.get().add(quantity.multiply(exchangeRate)));
            }
        });

        return total.get().setScale(BigDecimals.SCALE, BigDecimals.ROUNDING_MODE);
    }

    /**
     * Converts HashMap to String.
     *
     * @param label label for the value
     * @param labelWidth the with of the label
     * @param map portfolio report
     * @return the report content
     */
    private StringBuilder mapToString(Label label, int labelWidth, Map<String, BigDecimal> map) {
        var sb = new StringBuilder();
        map.forEach((symbol, value) -> {
            var l = label.getLabel(language).replace("{1}", symbol);
            sb
                    .append(l).append(": ")
                    .append(Strings.space(labelWidth - l.length()))
                    .append(value.setScale(BigDecimals.SCALE, BigDecimals.ROUNDING_MODE)).append(NEW_LINE);
        });
        return sb;
    }

    /**
     * Get history data from file.
     *
     * @param filename data file name
     */
    @Override
    protected List<PortfolioReport> getHistoryFromFile(String filename) {
        return new ArrayList<>();
    }

    /**
     * Build the table header.
     *
     * @param widths the column width
     * @return the table header as a string
     */
    private StringBuilder generateHeader(Map<String, Integer> widths) {
        var header = new StringBuilder();
        var headerSeparator = new StringBuilder();
        LabelCollection.PORTFOLIO_TABLE_HEADERS
                .stream()
                .filter(label -> Filter.columnsToHideFilter(columnsToHide, label))
                .forEach(labelKey -> {
                    var labelValue = labelKey.getLabel(language);
                    var width = widths.get(labelKey.getId());
                    header.append(markdownSeparator).append(Strings.leftPad(labelValue, width));
                    headerSeparator.append(markdownSeparator).append("-".repeat(width));
                });

        header.append(markdownSeparator).append(NEW_LINE);
        headerSeparator.append(markdownSeparator).append(NEW_LINE);
        return new StringBuilder().append(header).append(headerSeparator);
    }

    /**
     * Build the report title.
     *
     * @param tradeDate the last trade date in the report
     * @return the report title as a string
     */
    private StringBuilder generateTitle(LocalDateTime tradeDate) {
        return new StringBuilder()
            .append("# ")
            .append(Label.LABEL_PORTFOLIO_SUMMARY.getLabel(language))
            .append(NEW_LINE)
            .append("_")
            .append(Label.TITLE_SUMMARY_REPORT.getLabel(language))
            .append(": ")
            .append(LocalDateTimes.toNullSafeString(zone, dateTimePattern, tradeDate))
            .append("_")
            .append(NEW_LINE)
            .append(NEW_LINE);
    }

    /**
     * Calculates the with of the columns that are shown in the Markdown report.
     *
     * @param portfolioReport the portfolio report
     * @return length of the columns
     */
    private Map<String, Integer> calculateColumnWidth(PortfolioReport portfolioReport) {
        Map<String, Integer> widths = new HashMap<>();
        LabelCollection.PORTFOLIO_TABLE_HEADERS.forEach(x -> widths.put(x.getId(), x.getLabel(language).length()));

        portfolioReport.getPortfolios().forEach((portfolioName, portfolio) -> {
            updateWidth(widths, Label.HEADER_PORTFOLIO, portfolio.getName());

            portfolio.getProducts().forEach((symbol, product) -> {
                if (BigDecimals.isNotZero(product.getQuantity())) {
                    updateWidth(widths, Label.HEADER_SYMBOL, product.getSymbol());
                    updateWidth(widths, Label.HEADER_QUANTITY, product.getQuantity());
                    updateWidth(widths, Label.HEADER_AVG_PRICE, product.getAveragePrice());
                    updateWidth(widths, Label.HEADER_MARKET_UNIT_PRICE, product.getMarketPrice().getUnitPrice());
                    updateWidth(widths, Label.HEADER_MARKET_VALUE, product.getMarketValue());
                    updateWidth(widths, Label.HEADER_INVESTED_AMOUNT, product.getInvestedAmount());
                    updateWidth(widths, Label.HEADER_PROFIT_LOSS, product.getProfitAndLoss());
                    updateWidth(widths, Label.HEADER_PROFIT_LOSS_PERCENT, product.getProfitAndLossPercent());
                    updateWidth(widths, Label.HEADER_COSTS, product.getCosts());
                    updateWidth(widths, Label.HEADER_DEPOSITS, product.getDeposits());
                    updateWidth(widths, Label.HEADER_WITHDRAWALS, product.getWithdrawals());
                }
            });
        });
        return widths;
    }
}
