package com.remal.portfolio.writer;

import com.remal.portfolio.model.FileType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.ProductSummary;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.util.Files;
import com.remal.portfolio.util.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Product summary writer.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class SummaryWriter extends Writer {

    /**
     * Set it to true to show the relevant transactions in the report.
     */
    @Setter
    private boolean showTransactions = false;

    /**
     * Set it to true to show the transaction histories in the report.
     */
    @Setter
    private boolean showTransactionHistory = false;

    /**
     * Portfolio summary report content.
     */
    private final Map<String, Map<String, ProductSummary>> summary;

    /**
     * Produces a report writer instance.
     *
     * @param summary portfolio summary report content
     * @param language an ISO 639 alpha-2 or alpha-3 language code
     * @param dateTimePattern pattern used for converting string to LocalDateTime
     * @return an initialized writer instance
     * @throws java.lang.UnsupportedOperationException in case of usage of Excel file
     */
    public static SummaryWriter build(Map<String, Map<String, ProductSummary>> summary,
                                      String language,
                                      String dateTimePattern) {

        log.debug("initializing summary report writer with '{}' language...", language);

        var writer = new SummaryWriter(summary);
        writer.setLanguage(language);
        writer.setDateTimePattern(dateTimePattern);
        return writer;
    }

    /**
     * Constructor.
     *
     * @param summary portfolio summary report content
     */
    public SummaryWriter(Map<String, Map<String, ProductSummary>> summary) {
        this.summary = summary;
    }

    /**
     * Writes the report to file.
     *
     * @param writeMode controls how to open the file
     * @param file the report file
     * @throws java.lang.UnsupportedOperationException throws if unsupported file format was requested
     */
    public void writeToFile(FileWriter.WriteMode writeMode, String file) {
        var filetype = Files.getFileType(file);
        if (filetype == FileType.TEXT) {
            FileWriter.write(writeMode, file, printAsMarkdown());
        } else {
            log.error("Unsupported output file type: '{}'", file);
            System.exit(CommandLine.ExitCode.SOFTWARE);
        }
    }

    /**
     * Generates the transaction report.
     *
     * @return the report as a Markdown string
     */
    public String printAsMarkdown() {
        log.debug("building the Markdown Summary report...");
        var widths = calculateColumnWidth();
        return buildMarkdownReportHeader(Label.LABEL_PORTFOLIO_SUMMARY.getLabel(language))
                .append(buildPortfolioSummary(widths))
                .toString();
    }

    /**
     * Calculates the with of the columns that are shown in the Markdown report.
     *
     * @return length of the columns
     */
    private Map<String, Integer> calculateColumnWidth() {
        Map<String, Integer> widths = new HashMap<>();
        summary.forEach((key, value) ->
                value.forEach((k, v) -> {
                    updateWidth(widths, Label.TICKER, v.getTicker());
                    updateWidth(widths, Label.QUANTITY, v.getTotalShares());
                    updateWidth(widths, Label.AVG_PRICE, v.getAveragePrice());
                    updateWidth(widths, Label.NET_COST, v.getNetCost());
                    updateWidth(widths, Label.MARKET_VALUE, v.getMarketValue());
                }));
        return widths;
    }

    /**
     * Generates the portfolio summary report.
     *
     * @param widths length of the columns
     * @return the portfolio summary report
     */
    private StringBuilder buildPortfolioSummary(Map<String, Integer> widths) {
        var sb = new StringBuilder();
        summary.forEach((portfolioName, portfolio) -> {
            sb
                    .append(NEW_LINE)
                    .append("### ").append(Label.LABEL_PORTFOLIO.getLabel(language)).append(": ").append(portfolioName)
                    .append(NEW_LINE).append(NEW_LINE)
                    .append(buildPortfolioSummary(widths, portfolio))
                    .append(buildTransactionTables(portfolio));

            String lastLine = getLastLine(sb);
            sb.append(NEW_LINE).append("-".repeat(lastLine.length())).append(NEW_LINE);
        });

        return sb;
    }

    /**
     * Generates the portfolio summary table.
     *
     * @param widths length of the columns
     * @param portfolio portfolio data
     * @return the report as a string
     */
    private StringBuilder buildPortfolioSummary(Map<String, Integer> widths, Map<String, ProductSummary> portfolio) {
        var header = new StringBuilder();
        var headerSeparator = new StringBuilder();

        LabelCollection.getPortfolioSummaryTable()
                .stream()
                .filter(label -> !columnsToHide.contains(label.getId()))
                .forEach(label -> {
                    var translation = label.getLabel(language);
                    header
                            .append(TABLE_SEPARATOR)
                            .append(Strings.leftPad(translation, widths.get(label.getId())));
                    headerSeparator
                            .append(TABLE_SEPARATOR)
                            .append("-".repeat(widths.get(label.getId())));
                });

        header.append(TABLE_SEPARATOR).append(NEW_LINE);
        headerSeparator.append(TABLE_SEPARATOR).append(NEW_LINE);

        var sb = new StringBuilder().append(header).append(headerSeparator);

        portfolio.forEach((ticker, productSummary) -> {
            if (BigDecimals.isNotNullAndNotZero(productSummary.getTotalShares())) {
                sb
                        .append(getCell(Label.TICKER, productSummary.getTicker(), widths))
                        .append(getCell(Label.QUANTITY, productSummary.getTotalShares(), widths))
                        .append(getCell(Label.AVG_PRICE, productSummary.getAveragePrice(), widths))
                        .append(getCell(Label.NET_COST, productSummary.getNetCost(), widths))
                        .append(getCell(Label.MARKET_VALUE, productSummary.getNetCost(), widths))
                        .append(TABLE_SEPARATOR)
                        .append(NEW_LINE);
            }
        });

        return sb;
    }

    /**
     * Generates the relevant transactions and transaction history tables.
     *
     * @param portfolio portfolio data
     * @return the report as a string
     */
    private StringBuilder buildTransactionTables(Map<String, ProductSummary> portfolio) {
        var sb = new StringBuilder();
        portfolio
                .entrySet()
                .stream()
                .filter(productSummary -> ! productSummary.getValue().getTransactions().isEmpty())
                .forEach(productSummary -> {

                    // transaction list
                    if (showTransactions) {
                        sb.append(
                                buildTransactionList(
                                        Label.LABEL_TRANSACTION.getLabel(language),
                                        productSummary.getValue().getPortfolio(),
                                        productSummary.getValue().getTicker(),
                                        productSummary.getValue().getTransactions()));
                    }

                    // transaction history list
                    if (showTransactionHistory) {
                        sb.append(
                                buildTransactionList(
                                        Label.LABEL_TRANSACTION_HISTORY.getLabel(language),
                                        productSummary.getValue().getPortfolio(),
                                        productSummary.getValue().getTicker(),
                                        productSummary.getValue().getTransactionHistory()));
                    }
                });
        return sb;
    }

    /**
     * Generates a table for transactions.
     *
     * @param labelPrefix prefix of the report title
     * @param portfolio portfolio data
     * @param ticker the ticker of the product
     * @param transactions belonging transactions
     * @return the table with the transactions
     */
    private StringBuilder buildTransactionList(String labelPrefix,
                                               String portfolio,
                                               String ticker,
                                               List<Transaction> transactions) {
        TransactionWriter writer = new TransactionWriter(transactions);
        writer.setShowReportTitle(false);
        writer.setColumnsToHide(Arrays.asList(
                Label.PORTFOLIO.getId(),
                Label.TICKER.getId(),
                Label.ORDER_ID.getId(),
                Label.TRADE_ID.getId(),
                Label.TRANSFER_ID.getId()));

        return new StringBuilder()
                .append(NEW_LINE)
                .append("##### ").append(labelPrefix).append(": ").append(portfolio).append(", ").append(ticker)
                .append(NEW_LINE)
                .append(writer.printAsMarkdown());
    }

    /**
     * Gets the last line of the report.
     *
     * @param sb the report
     * @return the last line of the report
     */
    private String getLastLine(StringBuilder sb) {
        var lastNewLineIndex = sb.lastIndexOf(NEW_LINE);
        var lastLineStarts = lastNewLineIndex == sb.length() - NEW_LINE.length()
                ? sb.substring(0, lastNewLineIndex).lastIndexOf(NEW_LINE)
                : lastNewLineIndex;

        var lastLine = sb.substring(lastLineStarts);
        lastLine = lastLine.startsWith(NEW_LINE)
                ? lastLine.substring(NEW_LINE.length())
                : lastLine;

        lastLine = lastLine.endsWith(NEW_LINE)
                ? lastLine.substring(0, lastLine.length() - NEW_LINE.length())
                : lastLine;
        return lastLine;
    }
}
