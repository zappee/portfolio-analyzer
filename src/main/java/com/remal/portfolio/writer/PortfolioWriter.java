package com.remal.portfolio.writer;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.Portfolio;
import com.remal.portfolio.model.PortfolioCollection;
import com.remal.portfolio.parser.PortfolioParser;
import com.remal.portfolio.picocli.arggroup.PortfolioArgGroup;
import com.remal.portfolio.picocli.arggroup.PortfolioInputArgGroup;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Portfolio summary writer.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class PortfolioWriter extends Writer<PortfolioCollection> {

    /**
     * The currency of the portfolio report.
     */
    @Setter
    private CurrencyType baseCurrency;

    /**
     * Set the product name filter.
     */
    @Setter
    private List<String> tickers = new ArrayList<>();

    /**
     * The total invested amount.
     */
    private final Map<String, BigDecimal> depositTotal = new HashMap<>();

    /**
     * Portfolio market value
     */
    private BigDecimal marketValue;

    /**
     * Invested amount.
     */
    private BigDecimal investedAmount;

    /**
     * P/L on portfolio.
     */
    private BigDecimal profitAndLoss;

    /**
     * Cash in portfolio.
     */
    private final Map<String, BigDecimal> cashInPortfolio = new HashMap<>();

    /**
     * The account value, also known as total equity, is the total dollar value of all
     * the holdings of the trading account, not just the securities, but the cash as
     * well.
     */
    private BigDecimal totalEquity;

    /**
     * Builder that initializes a new writer instance.
     *
     * @param inputArgGroup  the input CLI group
     * @param outputArgGroup the output CLI group
     * @return               the writer instance
     */
    public static PortfolioWriter build(PortfolioInputArgGroup inputArgGroup,
                                        PortfolioArgGroup.OutputArgGroup outputArgGroup) {

        var writer = new PortfolioWriter();
        writer.setBaseCurrency(CurrencyType.getEnum(outputArgGroup.getBaseCurrency()));
        writer.setTickers(inputArgGroup.getTickers());
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
     * @return      the report content as a String
     */
    @Override
    protected String buildCsvReport(List<PortfolioCollection> items) {
        items.addAll((new PortfolioParser()).parse("'filename.md'"));
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
        return report.toString();
    }

    /**
     * Generate the Excel report.
     *
     * @param items data
     * @return      the report content as bytes
     */
    @Override
    protected byte[] buildExcelReport(List<PortfolioCollection> items) {
        throw new UnsupportedOperationException();
    }

    /**
     * Generate the Text/Markdown report.
     *
     * @param items data
     * @return      the report content as a String
     */
    @Override
    protected String buildMarkdownReport(List<PortfolioCollection> items) {
        items.sort(Comparator.comparing(PortfolioCollection::getGenerated));

        var widths = calculateColumnWidth(items);
        var report = new StringBuilder();

        // report title
        if (!hideTitle) {
            var lastSummary = items.stream().reduce((first, second) -> second);
            var tradeDate = lastSummary
                    .map(PortfolioCollection::getGenerated)
                    .orElse(LocalDateTime.now().atZone(zone).toLocalDateTime());
            report.append(generateTitle(tradeDate));
        }

        // table header
        if (!hideHeader) {
            report.append(generateHeader(widths));
        }

        // data
        items.forEach(productSummary -> productSummary.getPortfolios()
                .forEach(portfolio -> portfolio
                        .stream()
                        .filter(p -> tickers.isEmpty() || tickers.contains(p.getTicker().trim()))
                        .forEach(p -> {
                            if (BigDecimals.isNotZero(p.getTotalShares())) {
                                report
                                    .append(getCell(Label.HEADER_PORTFOLIO, p.getName(), widths))
                                    .append(getCell(Label.HEADER_TICKER, p.getTicker(), widths))
                                    .append(getCell(Label.HEADER_QUANTITY, p.getTotalShares(), widths))
                                    .append(getCell(Label.HEADER_AVG_PRICE, p.getAveragePrice(), widths))
                                    .append(getCell(Label.HEADER_INVESTED_AMOUNT, p.getInvestedAmount(), widths))
                                    .append(getCell(Label.HEADER_MARKET_UNIT_PRICE, p.getMarketUnitPrice(), widths))
                                    .append(getCell(Label.HEADER_MARKET_VALUE, p.getMarketValue(), widths))
                                    .append(getCell(Label.HEADER_PROFIT_LOSS, p.getProfitAndLoss(), widths))
                                    .append(getCell(Label.HEADER_PROFIT_LOSS_PERCENT, p.getProfitLossPercent(), widths))
                                    .append(getCell(Label.HEADER_COST_TOTAL, p.getCostTotal(), widths))
                                    .append(getCell(Label.HEADER_DEPOSIT_TOTAL, p.getDepositTotal(), widths))
                                    .append(getCell(Label.HEADER_WITHDRAWAL_TOTAL, p.getWithdrawalTotal(), widths))
                                    .append(markdownSeparator)
                                    .append(NEW_LINE);
                            }
                        })
                )
        );

        // footer
        updateTotals(items);
        report.append(generateMarkdownFooter());

        return report.toString();
    }

    /**
     * Get history data from file.
     *
     * @param filename data file name
     */
    @Override
    protected List<PortfolioCollection> getHistoryFromFile(String filename) {
        return new ArrayList<>();
    }

    /**
     * Generate the table header.
     *
     * @param items data
     * @return      the table headers as a string
     */
    private StringBuilder buildTableHeader(List<PortfolioCollection> items) {
        var sb = new StringBuilder();
        sb.append(Label.HEADER_DATE.getLabel(language)).append(csvSeparator);
        items.forEach(summaries ->
                summaries.getPortfolios().forEach(portfolio ->
                        portfolio
                                .stream()
                                .filter(summary -> BigDecimals.isNotNullAndNotZero(summary.getTotalShares()))
                                .forEach(summary ->
                                        LabelCollection.SUMMARY_TABLE_HEADERS
                                                .stream()
                                                .filter(label -> Filter.columnsToHideFilter(columnsToHide, label))
                                                .forEach(label -> sb
                                                        .append(getColumnName(summary))
                                                        .append(" ")
                                                        .append(label.getLabel(language))
                                                        .append(csvSeparator)
                                                )
                                )
                )
        );
        sb.setLength(sb.length() - csvSeparator.length());
        sb.append(NEW_LINE);

        return sb;
    }

    /**
     * Updates the value of the totals.
     *
     * @param items the portfolio collection
     */
    private void updateTotals(List<PortfolioCollection> items) {
        marketValue = BigDecimal.ZERO;
        investedAmount = BigDecimal.ZERO;
        profitAndLoss = BigDecimal.ZERO;
        totalEquity = BigDecimal.ZERO;
        depositTotal.clear();
        cashInPortfolio.clear();

        items.forEach(collection -> {
            // merge collection.getDepositTotal into this.depositTotal
            collection.getDepositTotal().forEach((k, v) -> depositTotal.merge(k, v, BigDecimal::add));

            marketValue = marketValue.add(collection.getMarketValue());
            investedAmount = investedAmount.add(collection.getInvestedAmount());
            profitAndLoss = profitAndLoss.add(collection.getProfitAndLoss());
            totalEquity = totalEquity.add(collection.getTotalEquity());
            collection.getCashInPortfolio().forEach((key, value) -> cashInPortfolio.merge(key, value, BigDecimal::add));
        });
    }

    /**
     * Generate the CSV table footer.
     *
     * @return the table headers as a string
     */
    private StringBuilder generateMarkdownFooter() {
        var labelWith = getLongestLabel(Arrays.asList(
                Label.LABEL_DEPOSIT.getLabel(language),
                Label.LABEL_MARKET_VALUE.getLabel(language),
                Label.LABEL_INVESTMENT.getLabel(language),
                Label.LABEL_PROFIT_LOSS.getLabel(language),
                Label.LABEL_CASH.getLabel(language),
                Label.LABEL_TOTAL_EQUITY.getLabel(language)));

        var totalWholeSize = getTotalsWholeSize();
        var fractionalSize = 2;
        var emptyLabel = Label.HEADER_EMPTY;
        Map<String, Integer> widths = new HashMap<>();
        widths.put(getWholeWidthKey(emptyLabel), totalWholeSize);
        widths.put(getFractionalWidthKey(emptyLabel), fractionalSize);

        var scale = 2;
        var profitLossPercent = BigDecimals.percentOf(marketValue, investedAmount, scale);
        var profitLossPercentString = Objects.isNull(profitLossPercent) ? "" : " (" + profitLossPercent + "%)";

        var sb = new StringBuilder()
                .append(showTotal(Label.LABEL_DEPOSIT.getLabel(language), labelWith, depositTotal, widths))
                .append(showTotal(Label.LABEL_INVESTMENT.getLabel(language), labelWith, investedAmount, widths))
                .append(showTotal(Label.LABEL_MARKET_VALUE.getLabel(language), labelWith, marketValue, widths))
                .append(showTotal(Label.LABEL_PROFIT_LOSS.getLabel(language), labelWith, profitAndLoss, widths))
                .append(profitLossPercentString);

        cashInPortfolio.forEach((key, value) -> {
            if (BigDecimals.isNotNullAndNotZero(value)) {
                sb.append(showTotal(
                        Label.LABEL_CASH.getLabel(language).replace("{1}", key),
                        labelWith,
                        value,
                        widths));
            }
        });

        var decimalSeparatorsNumber = totalWholeSize / 3;
        var horizontalLineWidth = labelWith + ": ".length() + totalWholeSize + ".".length() + fractionalSize;
        horizontalLineWidth += decimalSeparatorsNumber;
        sb
                .append(NEW_LINE).append("_".repeat(horizontalLineWidth)).append(NEW_LINE)
                .append(showTotal(
                        Label.LABEL_TOTAL_EQUITY.getLabel(language).replace("{1}", baseCurrency.name()),
                        labelWith,
                        totalEquity,
                        widths));
        return sb;
    }

    /**
     * Generates a formatted total line.
     *
     * @param label     label of the value
     * @param labelWith length where the label will be aligned to
     * @param values    values to show
     * @param widths    with collection
     * @return          the formatted total value
     */
    private StringBuilder showTotal(String label, int labelWith, Map<String, BigDecimal> values, Map<String, Integer> widths) {
        var sb = new StringBuilder();
        values.forEach((k, v) -> sb.append(showTotal(label.replace("{1}", k), labelWith, v, widths)));
        return sb;
    }

    /**
     * Generates a formatted total line.
     *
     * @param label      label of the value
     * @param labelWidth length where the label will be aligned to
     * @param value      value to show
     * @param widths     with collection
     * @return           the formatted value
     */
    private String showTotal(String label, int labelWidth, BigDecimal value, Map<String, Integer> widths) {
        var originalDecimalFormat = decimalFormat;
        var originalMarkdownSeparator = markdownSeparator;
        decimalFormat = "###,###,###,###,###,###.##";
        markdownSeparator = "";

        var labelWithDynamicValue = generateLabelWithDynamicValue(widths);
        var valueAsString = getCell(labelWithDynamicValue, value, widths);
        decimalFormat = originalDecimalFormat;
        markdownSeparator = originalMarkdownSeparator;

        return NEW_LINE
                + label
                + ": "
                + Strings.space(labelWidth - label.length())
                + valueAsString;
    }

    /**
     * Generates a fake label with dynamic value.
     *
     * @param widths with collection
     * @return       a label with a specific value
     */
    private Label generateLabelWithDynamicValue(Map<String, Integer> widths) {
        var label = Label.HEADER_EMPTY;
        var wholeSize = widths.get(getWholeWidthKey(label));
        var decimalSeparatorsNumber = wholeSize / 3;
        var labelValue = Strings.space(wholeSize + decimalSeparatorsNumber);
        var fractionalLength = widths.get(getFractionalWidthKey(label));

        if (Objects.nonNull(fractionalLength) && fractionalLength > 0) {
            labelValue = labelValue + "." + Strings.space(fractionalLength);
        }
        label.setLabel(labelValue);
        return label;
    }

    /**
     * Calculates the length of the totals.
     *
     * @return the biggest length of the totals
     */
    private int getTotalsWholeSize() {
        var biggestTotal = depositTotal.isEmpty()
                ? BigDecimal.ZERO
                : Collections.max(depositTotal.entrySet(), Map.Entry.comparingByValue()).getValue();

        biggestTotal = biggestTotal.max(Objects.isNull(marketValue) ? BigDecimal.ZERO : marketValue);
        biggestTotal = biggestTotal.max(Objects.isNull(investedAmount) ? BigDecimal.ZERO : investedAmount);
        biggestTotal = biggestTotal.max(Objects.isNull(profitAndLoss) ? BigDecimal.ZERO : profitAndLoss);
        biggestTotal = depositTotal.isEmpty()
                ? BigDecimal.ZERO
                : biggestTotal.max(Collections.max(cashInPortfolio.entrySet(), Map.Entry.comparingByValue()).getValue());

        return String.valueOf(biggestTotal.intValue()).length();
    }

    /**
     * Calculate the maximum length of the labels.
     *
     * @param labels   the labels
     * @return maximum length
     */
    private int getLongestLabel(List<String> labels) {
        int[] length = { 0 };
        labels.forEach(label -> {
            if (length[0] < label.length()) {
                length[0] = label.length();
            }
        });
        return length[0];
    }

    /**
     * Generate the report item.
     *
     * @param portfolio product portfolio summary
     * @return          the report item
     */
    private StringBuilder buildReportItem(Portfolio portfolio) {
        return new StringBuilder()
                .append(getCell(Label.HEADER_PORTFOLIO, portfolio.getName(), csvSeparator))
                .append(getCell(Label.HEADER_TICKER, portfolio.getTicker(), csvSeparator))
                .append(getCell(Label.HEADER_QUANTITY, portfolio.getTotalShares(), csvSeparator))
                .append(getCell(Label.HEADER_AVG_PRICE, portfolio.getAveragePrice(), csvSeparator))
                .append(getCell(Label.HEADER_INVESTED_AMOUNT, portfolio.getInvestedAmount(), csvSeparator))
                .append(getCell(Label.HEADER_MARKET_UNIT_PRICE, portfolio.getMarketUnitPrice(), csvSeparator))
                .append(getCell(Label.HEADER_MARKET_VALUE, portfolio.getMarketValue(), csvSeparator))
                .append(getCell(Label.HEADER_PROFIT_LOSS, portfolio.getProfitAndLoss(), csvSeparator))
                .append(getCell(Label.HEADER_PROFIT_LOSS_PERCENT, portfolio.getProfitLossPercent(), csvSeparator))
                .append(getCell(Label.HEADER_COST_TOTAL, portfolio.getCostTotal(), csvSeparator))
                .append(getCell(Label.HEADER_DEPOSIT_TOTAL, portfolio.getDepositTotal(), csvSeparator))
                .append(getCell(Label.HEADER_WITHDRAWAL_TOTAL, portfolio.getWithdrawalTotal(), csvSeparator));
    }

    /*
    /**
     * Build the table header.
     *
     * @param  widths the column width
     * @return the table header as a string
     */
    private StringBuilder generateHeader(Map<String, Integer> widths) {
        var header = new StringBuilder();
        var headerSeparator = new StringBuilder();
        LabelCollection.SUMMARY_TABLE_HEADERS
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
     * @return          the report title as a string
     */
    private StringBuilder generateTitle(LocalDateTime tradeDate) {
        return new StringBuilder()
            .append("# ")
            .append(Label.LABEL_PORTFOLIO_SUMMARY.getLabel(language))
            .append(NEW_LINE)
            .append("_")
            .append(Label.TITLE_SUMMARY_REPORT.getLabel(language))
            .append(": ")
            .append(LocalDateTimes.toString(zone, dateTimePattern, tradeDate))
            .append("_")
            .append(NEW_LINE)
            .append(NEW_LINE);
    }

    /**
     * Calculates the with of the columns that are shown in the Markdown report.
     *
     * @param productSummaries collection of product summaries
     * @return                 length of the columns
     */
    private Map<String, Integer> calculateColumnWidth(List<PortfolioCollection> productSummaries) {
        Map<String, Integer> widths = new HashMap<>();

        LabelCollection.SUMMARY_TABLE_HEADERS.forEach(label ->
                widths.put(label.getId(), label.getLabel(language).length()));

        productSummaries.forEach(productSummary ->
                productSummary.getPortfolios().forEach(portfolio ->
                        portfolio.forEach(p -> {
                            if (BigDecimals.isNotZero(p.getTotalShares())) {
                                updateWidth(widths, Label.HEADER_PORTFOLIO, p.getName());
                                updateWidth(widths, Label.HEADER_TICKER, p.getTicker());
                                updateWidth(widths, Label.HEADER_QUANTITY, p.getTotalShares());
                                updateWidth(widths, Label.HEADER_AVG_PRICE, p.getAveragePrice());
                                updateWidth(widths, Label.HEADER_INVESTED_AMOUNT, p.getInvestedAmount());
                                updateWidth(widths, Label.HEADER_MARKET_UNIT_PRICE, p.getMarketUnitPrice());
                                updateWidth(widths, Label.HEADER_MARKET_VALUE, p.getMarketValue());
                                updateWidth(widths, Label.HEADER_PROFIT_LOSS, p.getProfitAndLoss());
                                updateWidth(widths, Label.HEADER_PROFIT_LOSS_PERCENT, p.getProfitLossPercent());
                                updateWidth(widths, Label.HEADER_COST_TOTAL, p.getCostTotal());
                                updateWidth(widths, Label.HEADER_DEPOSIT_TOTAL, p.getDepositTotal());
                                updateWidth(widths, Label.HEADER_WITHDRAWAL_TOTAL, p.getWithdrawalTotal());
                            }
                        })
                )
        );
        return widths;
    }

    /**
     * Generate the column name.
     *
     * @param portfolio the product portfolio summary
     * @return          the generated column name
     */
    private String getColumnName(Portfolio portfolio) {
        return portfolio.getName() + " " + portfolio.getTicker();
    }
}
