package com.remal.portfolio.writer;

import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.ProductSummary;
import com.remal.portfolio.model.ProductSummaryCollection;
import com.remal.portfolio.parser.SummaryParser;
import com.remal.portfolio.picocli.arggroup.SummaryArgGroup;
import com.remal.portfolio.picocli.arggroup.SummaryInputArgGroup;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProductSummary summary writer.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class SummaryWriter extends Writer<ProductSummaryCollection> {

    /**
     * Set the product name filter.
     */
    @Setter
    private List<String> tickers = new ArrayList<>();

    /**
     * Builder that initializes a new writer instance.
     *
     * @param inputArgGroup  the input CLI group
     * @param outputArgGroup the output CLI group
     * @return               the writer instance
     */
    public static SummaryWriter build(SummaryInputArgGroup inputArgGroup,
                                      SummaryArgGroup.OutputArgGroup outputArgGroup) {

        var writer = new SummaryWriter();
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
     * @return the report content as a String
     */
    @Override
    protected String buildCsvReport(List<ProductSummaryCollection> items) {
        items.addAll((new SummaryParser()).parse("'filename.md'"));
        items.sort(Comparator.comparing(ProductSummaryCollection::getGenerated));

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
    protected byte[] buildExcelReport(List<ProductSummaryCollection> items) {
        throw new UnsupportedOperationException();
    }

    /**
     * Generate the Text/Markdown report.
     *
     * @param items data
     * @return      the report content as a String
     */
    @Override
    protected String buildMarkdownReport(List<ProductSummaryCollection> items) {
        items.addAll((new SummaryParser()).parse("'filename.md'"));
        items.sort(Comparator.comparing(ProductSummaryCollection::getGenerated));

        var widths = calculateColumnWidth(items);
        var report = new StringBuilder();

        // report title
        if (!hideTitle) {
            var lastSummary = items.stream().reduce((first, second) -> second);
            var tradeDate = lastSummary
                    .map(ProductSummaryCollection::getGenerated)
                    .orElse(LocalDateTime.now().atZone(zone).toLocalDateTime());
            report.append(generateTitle(tradeDate));
        }

        // table header
        if (!hideHeader) {
            report.append(generateHeader(widths));
        }

        // data
        items.forEach(productSummary -> productSummary.getPortfolios()
                .forEach(summaries -> summaries
                        .stream()
                        .filter(summary -> tickers.isEmpty() || tickers.contains(summary.getTicker().trim()))
                        .forEach(summary -> {
                            if (BigDecimals.isNotZero(summary.getTotalShares())) {
                                report
                                    .append(getCell(Label.PORTFOLIO, summary.getPortfolio(), widths))
                                    .append(getCell(Label.TICKER, summary.getTicker(), widths))
                                    .append(getCell(Label.QUANTITY, summary.getTotalShares(), widths))
                                    .append(getCell(Label.AVG_PRICE, summary.getAveragePrice(), widths))
                                    .append(getCell(Label.INVESTED_AMOUNT, summary.getInvestedAmount(), widths))
                                    .append(getCell(Label.MARKET_UNIT_PRICE, summary.getMarketUnitPrice(), widths))
                                    .append(getCell(Label.MARKET_VALUE, summary.getMarketValue(), widths))
                                    .append(getCell(Label.PROFIT_LOSS, summary.getProfitLoss(), widths))
                                    .append(getCell(Label.PROFIT_LOSS_PERCENT, summary.getProfitLossPercent(), widths))
                                    .append(getCell(Label.COST_TOTAL, summary.getCostTotal(), widths))
                                    .append(getCell(Label.DEPOSIT_TOTAL, summary.getDepositTotal(), widths))
                                    .append(getCell(Label.WITHDRAWAL_TOTAL, summary.getWithdrawalTotal(), widths))
                                    .append(markdownSeparator)
                                    .append(NEW_LINE);
                            }
                        })
                )
        );

        return report.toString();
    }

    /**
     * Generate the table header.
     *
     * @param items data
     * @return the table headers as a string
     */
    private StringBuilder buildTableHeader(List<ProductSummaryCollection> items) {
        var sb = new StringBuilder();
        sb.append(Label.DATE.getLabel(language)).append(csvSeparator);
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
     * Generate the report item.
     *
     * @param summary product summary
     * @return the report item
     */
    private StringBuilder buildReportItem(ProductSummary summary) {
        return new StringBuilder()
                .append(getCell(Label.PORTFOLIO, summary.getPortfolio(), csvSeparator))
                .append(getCell(Label.TICKER, summary.getTicker(), csvSeparator))
                .append(getCell(Label.QUANTITY, summary.getTotalShares(), csvSeparator))
                .append(getCell(Label.AVG_PRICE, summary.getAveragePrice(), csvSeparator))
                .append(getCell(Label.INVESTED_AMOUNT, summary.getInvestedAmount(), csvSeparator))
                .append(getCell(Label.MARKET_UNIT_PRICE, summary.getMarketUnitPrice(), csvSeparator))
                .append(getCell(Label.MARKET_VALUE, summary.getMarketValue(), csvSeparator))
                .append(getCell(Label.PROFIT_LOSS, summary.getProfitLoss(), csvSeparator))
                .append(getCell(Label.PROFIT_LOSS_PERCENT, summary.getProfitLossPercent(), csvSeparator))
                .append(getCell(Label.COST_TOTAL, summary.getCostTotal(), csvSeparator))
                .append(getCell(Label.DEPOSIT_TOTAL, summary.getDepositTotal(), csvSeparator))
                .append(getCell(Label.WITHDRAWAL_TOTAL, summary.getWithdrawalTotal(), csvSeparator));
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
    private Map<String, Integer> calculateColumnWidth(List<ProductSummaryCollection> productSummaries) {
        Map<String, Integer> widths = new HashMap<>();

        LabelCollection.SUMMARY_TABLE_HEADERS.forEach(label ->
                widths.put(label.getId(), label.getLabel(language).length()));

        productSummaries.forEach(productSummary ->
                productSummary.getPortfolios().forEach(portfolio ->
                        portfolio.forEach(summary -> {
                            if (BigDecimals.isNotZero(summary.getTotalShares())) {
                                updateWidth(widths, Label.PORTFOLIO, summary.getPortfolio());
                                updateWidth(widths, Label.TICKER, summary.getTicker());
                                updateWidth(widths, Label.QUANTITY, summary.getTotalShares());
                                updateWidth(widths, Label.AVG_PRICE, summary.getAveragePrice());
                                updateWidth(widths, Label.INVESTED_AMOUNT, summary.getInvestedAmount());
                                updateWidth(widths, Label.MARKET_UNIT_PRICE, summary.getMarketUnitPrice());
                                updateWidth(widths, Label.MARKET_VALUE, summary.getMarketValue());
                                updateWidth(widths, Label.PROFIT_LOSS, summary.getProfitLoss());
                                updateWidth(widths, Label.PROFIT_LOSS_PERCENT, summary.getProfitLossPercent());
                                updateWidth(widths, Label.COST_TOTAL, summary.getCostTotal());
                                updateWidth(widths, Label.DEPOSIT_TOTAL, summary.getDepositTotal());
                                updateWidth(widths, Label.WITHDRAWAL_TOTAL, summary.getWithdrawalTotal());
                            }
                        })
                )
        );
        return widths;
    }

    /**
     * Generate the column name.
     *
     * @param summary the product summary instance
     * @return the generated column name
     */
    private String getColumnName(ProductSummary summary) {
        return summary.getPortfolio() + " " + summary.getTicker();
    }
}
