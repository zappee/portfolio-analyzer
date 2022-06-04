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
import java.util.ArrayList;
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
     * Set the product name filter.
     */
    @Setter
    private List<String> tickers = new ArrayList<>();

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
        items
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
                                        .append(getCell(Label.DEPOSIT_TOTAL, summary.getDepositTotal(), widths))
                                        .append(getCell(Label.WITHDRAWAL_TOTAL, summary.getWithdrawalTotal(), widths))
                                        .append(getCell(Label.COST_TOTAL, summary.getCostTotal(), widths))
                                        .append(getCell(Label.MARKET_VALUE, summary.getMarketValue(), widths))
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

        LabelCollection.SUMMARY_TABLE_HEADERS
                .forEach(label -> widths.put(label.getId(), label.getLabel(language).length()));

        items
                .forEach(portfolio ->
                        portfolio.forEach(summary -> {
                            if (BigDecimals.isNotZero(summary.getTotalShares())) {
                                updateWidth(widths, Label.PORTFOLIO, summary.getPortfolio());
                                updateWidth(widths, Label.TICKER, summary.getTicker());
                                updateWidth(widths, Label.QUANTITY, summary.getTotalShares());
                                updateWidth(widths, Label.AVG_PRICE, summary.getAveragePrice());
                                updateWidth(widths, Label.DEPOSIT_TOTAL, summary.getDepositTotal());
                                updateWidth(widths, Label.WITHDRAWAL_TOTAL, summary.getWithdrawalTotal());
                                updateWidth(widths, Label.COST_TOTAL, summary.getCostTotal());
                                updateWidth(widths, Label.MARKET_VALUE, summary.getMarketValue());
                            }
                        }));
        return widths;
    }
}