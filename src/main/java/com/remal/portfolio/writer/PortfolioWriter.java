package com.remal.portfolio.writer;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.PortfolioReport;
import com.remal.portfolio.picocli.arggroup.PortfolioArgGroup;
import com.remal.portfolio.picocli.arggroup.PortfolioInputArgGroup;
import com.remal.portfolio.util.BigDecimalFormatter;
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
     * Prefix to be removed from the label id.
     */
    private static final String PREFIX_TO_REMOVE = "LABEL_";

    /**
     * Markdown unordered list.
     */
    private static final String MARKDOWN_LIST = "* ";

    /**
     * Decimal number formatter.
     */
    private BigDecimalFormatter bigDecimalFormatter;

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
        bigDecimalFormatter = initializeBogDecimalFormatter(portfolioReport);
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
        var emptyLabel = Label.HEADER_EMPTY;
        emptyLabel.setLabel("");
        
        var labelWidth = LabelCollection.PRODUCT_SUMMARY_FOOTER
                .stream()
                .max(Comparator.comparingInt(x -> x.getLabel(language).length()))
                .orElse(emptyLabel).getLabel(language)
                .length();
        var sb = new StringBuilder();

        if (!columnsToHide.contains(Label.LABEL_TOTAL_CASH.getId().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(NEW_LINE)
                    .append(showSummaryPerCurrencyAndTotal(
                            portfolioReport.getExchangeRates(),
                            portfolioReport.getCashInPortfolio(),
                            Label.LABEL_TOTAL_CASH_PER_CURRENCY,
                            Label.LABEL_TOTAL_CASH,
                            labelWidth));
        }

        if (!columnsToHide.contains(Label.LABEL_TOTAL_EXCHANGE_RATE.getId().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(NEW_LINE)
                    .append(showMapValue(portfolioReport.getExchangeRates(), labelWidth));
        }

        if (!columnsToHide.contains(Label.LABEL_TOTAL_DEPOSIT.getId().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(NEW_LINE)
                    .append(showSummaryPerCurrencyAndTotal(
                            portfolioReport.getExchangeRates(),
                            portfolioReport.getDeposits(),
                            Label.LABEL_TOTAL_DEPOSIT_PER_CURRENCY,
                            Label.LABEL_TOTAL_DEPOSIT,
                            labelWidth));
        }

        if (!columnsToHide.contains(Label.LABEL_TOTAL_WITHDRAWAL.getId().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(NEW_LINE)
                    .append(showSummaryPerCurrencyAndTotal(
                            portfolioReport.getExchangeRates(),
                            portfolioReport.getWithdrawals(),
                            Label.LABEL_TOTAL_WITHDRAWAL_PER_CURRENCY,
                            Label.LABEL_TOTAL_WITHDRAWAL,
                            labelWidth));
        }

        if (!columnsToHide.contains(Label.LABEL_TOTAL_INVESTMENT.getId().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(NEW_LINE)
                    .append(showSummaryPerCurrencyAndTotal(
                            portfolioReport.getExchangeRates(),
                            portfolioReport.getInvestments(),
                            Label.LABEL_TOTAL_INVESTMENT_PER_CURRENCY,
                            Label.LABEL_TOTAL_INVESTMENT,
                            labelWidth));
        }

        if (!columnsToHide.contains(Label.LABEL_TOTAL_MARKET_VALUE.getId().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(NEW_LINE)
                    .append(showSummaryPerCurrencyAndTotal(
                            portfolioReport.getExchangeRates(),
                            portfolioReport.getMarketValues(),
                            Label.LABEL_TOTAL_MARKET_VALUE_PER_CURRENCY,
                            Label.LABEL_TOTAL_MARKET_VALUE,
                            labelWidth));
        }

        if (!columnsToHide.contains(Label.LABEL_TOTAL_PROFIT_LOSS.getId().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(NEW_LINE)
                    .append(showSummaryPerCurrencyAndTotal(
                            portfolioReport.getExchangeRates(),
                            portfolioReport.getProfitLoss(),
                            Label.LABEL_TOTAL_PROFIT_LOSS_PER_CURRENCY,
                            Label.LABEL_TOTAL_PROFIT_LOSS,
                            labelWidth));
        }

        return sb;
    }

    /**
     * Get the length of the longest decimal value from the footer.
     *
     * @param portfolioReport portfolio report
     * @return the length of the longest decimal value in the footer
     */
    private BigDecimalFormatter initializeBogDecimalFormatter(PortfolioReport portfolioReport) {
        var formatter = new BigDecimalFormatter(decimalFormat, decimalGroupingSeparator);

        portfolioReport.getExchangeRates().forEach(formatter.get(BigDecimals.SCALE_FOR_EXCHANGE_RATE));
        portfolioReport.getCashInPortfolio().forEach(formatter.get(BigDecimals.SCALE_DEFAULT));
        portfolioReport.getDeposits().forEach(formatter.get(BigDecimals.SCALE_DEFAULT));
        portfolioReport.getWithdrawals().forEach(formatter.get(BigDecimals.SCALE_DEFAULT));
        portfolioReport.getInvestments().forEach(formatter.get(BigDecimals.SCALE_DEFAULT));
        portfolioReport.getMarketValues().forEach(formatter.get(BigDecimals.SCALE_DEFAULT));
        portfolioReport.getProfitLoss().forEach(formatter.get(BigDecimals.SCALE_DEFAULT));

        return formatter;
    }

    /**
     * Generates a footer content.
     *
     * @param valuesToShow portfolio report
     * @param labelWidth label will align left on this
     * @return the footer content
     */
    private StringBuilder showMapValue(Map<String, BigDecimal> valuesToShow, int labelWidth) {
        return new StringBuilder()
                .append(mapToString(
                        Label.LABEL_TOTAL_EXCHANGE_RATE,
                        labelWidth,
                        valuesToShow,
                        BigDecimals.SCALE_FOR_EXCHANGE_RATE));
    }

    /**
     * Generates a footer content.
     *
     * @param rates exchange rates
     * @param valuesToSum values to sum
     * @param labelWidth label will align left on this
     * @return the footer content
     */
    private StringBuilder showSummaryPerCurrencyAndTotal(Map<String, BigDecimal> rates,
                                                         Map<String, BigDecimal> valuesToSum,
                                                         Label labelForCurrency,
                                                         Label labelForTotal,
                                                         int labelWidth) {
        if (valuesToSum.isEmpty()) {
            return new StringBuilder();
        } else {
            var labelAsString = labelForTotal.getLabel(language).replace("{1}", baseCurrency.name());
            return new StringBuilder()
                    .append(mapToString(labelForCurrency, labelWidth, valuesToSum, BigDecimals.SCALE_DEFAULT))
                    .append(MARKDOWN_LIST).append(labelAsString)
                    .append(": ")
                    .append(Strings.space(labelWidth - labelAsString.length()))
                    .append(sumAndShow(rates, valuesToSum))
                    .append(NEW_LINE);
        }
    }

    /**
     * Exchange the currencies to the base currency and sum up them.
     *
     * @param rates exchange rates
     * @param valuesToSum values to sum
     * @return summed value in base currency as a formatted string
     */
    private String sumAndShow(Map<String, BigDecimal> rates,
                                  Map<String, BigDecimal> valuesToSum) {
        AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);
        valuesToSum.forEach((symbol, quantity) -> {
            var exchangeRateSymbol = symbol + "-" + baseCurrency;
            var exchangeRate = rates.get(exchangeRateSymbol);
            if (Objects.isNull(exchangeRate)) {
                total.set(total.get().add(quantity));
            } else {
                total.set(total.get().add(quantity.multiply(exchangeRate)));
            }
        });

        return bigDecimalFormatter.format(total.get(), BigDecimals.SCALE_DEFAULT);
    }

    /**
     * Converts HashMap to String.
     *
     * @param label label for the value
     * @param labelWidth the with of the label
     * @param map portfolio report
     * @param scale scale of the BigDecimal value to be returned
     * @return the report content
     */
    private StringBuilder mapToString(Label label, int labelWidth, Map<String, BigDecimal> map, int scale) {
        var sb = new StringBuilder();
        map
                .entrySet()
                .stream()
                .filter(entry -> BigDecimals.isNotNullAndNotZero(entry.getValue()))
                .forEach(entry -> {
                    var labelAsString = label.getLabel(language).replace("{1}", entry.getKey());
                    var decimalValue = entry.getValue().setScale(scale, BigDecimals.ROUNDING_MODE);
                    var decimalValueAsString = bigDecimalFormatter.format(decimalValue, scale);
                    sb
                            .append(MARKDOWN_LIST)
                            .append(labelAsString).append(": ")
                            .append(Strings.space(labelWidth - labelAsString.length()))
                            .append(decimalValueAsString).append(NEW_LINE);
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
