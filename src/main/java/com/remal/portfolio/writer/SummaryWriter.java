package com.remal.portfolio.writer;

import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.ProductSummary;
import com.remal.portfolio.picocli.arggroup.SummaryArgGroup;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
public class SummaryWriter extends Writer<List<ProductSummary>> {

    /**
     * Set it to true to show the relevant transactions in the report.
     */
    @Setter
    private boolean showTransactions;

    /**
     * Builder that initializes a new writer instance.
     *
     * @param arguments input arguments
     * @return the writer instance
     */
    public static SummaryWriter build(SummaryArgGroup.OutputArgGroup arguments) {
        var writer = new SummaryWriter();
        writer.setShowTransactions(arguments.isShowTransactions());
        writer.setHideTitle(arguments.isHideTitle());
        writer.setHideHeader(arguments.isHideHeader());
        writer.setLanguage(arguments.getLanguage());
        writer.setDateTimePattern(arguments.getDateTimePattern());
        writer.setZone(ZoneId.of(arguments.getZone()));
        return writer;
    }

    @Override
    protected String buildCsvReport(List<List<ProductSummary>> items) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected byte[] buildExcelReport(List<List<ProductSummary>> items) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String buildMarkdownReport(List<List<ProductSummary>> items) {
        var widths = calculateColumnWidth(items);
        var report = new StringBuilder();

        // report title
        if (!hideTitle) {
            report.append(generateTitle());
        }

        // table header
        if (!hideHeader) {
            report.append(generateHeader(widths));
        }

        // data
        items.forEach(portfolio -> portfolio.forEach(productSummary -> {
            if (BigDecimals.isNotZero(productSummary.getTotalShares())) {
                report
                        .append(getCell(Label.PORTFOLIO, productSummary.getPortfolio(), widths))
                        .append(getCell(Label.TICKER, productSummary.getTicker(), widths))
                        .append(getCell(Label.QUANTITY, productSummary.getTotalShares(), widths))
                        .append(getCell(Label.AVG_PRICE, productSummary.getAveragePrice(), widths))
                        .append(getCell(Label.DEPOSIT_TOTAL, productSummary.getDepositTotal(), widths))
                        .append(getCell(Label.WITHDRAWAL_TOTAL, productSummary.getWithdrawalTotal(), widths))
                        .append(getCell(Label.COST_TOTAL, productSummary.getCostTotal(), widths))
                        .append(getCell(Label.MARKET_VALUE, productSummary.getMarketValue(), widths))
                        .append(markdownSeparator)
                        .append(NEW_LINE);
            }
        }));

        if (showTransactions) {
            // TODO finish it
        }
        return report.toString();
    }

    /**
     * Build the table header.
     *
     * @param  widths the column width
     * @return the table header as a string
     */
    private StringBuilder generateHeader(Map<String, Integer> widths) {
        var header = new StringBuilder();
        var headerSeparator = new StringBuilder();
        LabelCollection.PORTFOLIO_SUMMARY_TABLE_HEADERS
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
     * @return the report title as a string
     */
    private StringBuilder generateTitle() {
        return new StringBuilder()
                .append("# ")
                .append(Label.LABEL_PORTFOLIO_SUMMARY.getLabel(language))
                .append(NEW_LINE)
                .append("_")
                .append(Label.LABEL_GENERATED.getLabel(language))
                .append(": ")
                .append(LocalDateTimes.toString(zone, dateTimePattern, LocalDateTime.now()))
                .append("_")
                .append(NEW_LINE)
                .append(NEW_LINE);

    }

    /**
     * Calculates the with of the columns that are shown in the Markdown report.
     *
     * @return length of the columns
     */
    private Map<String, Integer> calculateColumnWidth(List<List<ProductSummary>> items) {
        Map<String, Integer> widths = new HashMap<>();
        items.forEach(portfolio ->
                portfolio.forEach(portfolioSummary -> {
                    if (BigDecimals.isNotZero(portfolioSummary.getTotalShares())) {
                        updateWidth(widths, Label.PORTFOLIO, portfolioSummary.getPortfolio());
                        updateWidth(widths, Label.TICKER, portfolioSummary.getTicker());
                        updateWidth(widths, Label.QUANTITY, portfolioSummary.getTotalShares());
                        updateWidth(widths, Label.AVG_PRICE, portfolioSummary.getAveragePrice());
                        updateWidth(widths, Label.DEPOSIT_TOTAL, portfolioSummary.getDepositTotal());
                        updateWidth(widths, Label.WITHDRAWAL_TOTAL, portfolioSummary.getWithdrawalTotal());
                        updateWidth(widths, Label.COST_TOTAL, portfolioSummary.getCostTotal());
                        updateWidth(widths, Label.MARKET_VALUE, portfolioSummary.getMarketValue());
                    }
                }));
        return widths;
    }
}
