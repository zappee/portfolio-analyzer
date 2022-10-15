package com.remal.portfolio.writer;

import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.parser.TransactionParser;
import com.remal.portfolio.picocli.arggroup.OutputArgGroup;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.PortfolioNameRenamer;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import com.remal.portfolio.util.ZoneIds;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generate transaction reports.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class TransactionWriter extends Writer<Transaction> {

    /**
     * Builder that initializes a new writer instance.
     *
     * @param arguments input arguments
     * @return the writer instance
     */
    public static Writer<Transaction> build(OutputArgGroup arguments) {
        // validating the output params
        LocalDateTimes.validate(arguments.getDateTimePattern(), arguments.getFrom());
        LocalDateTimes.validate(arguments.getDateTimePattern(), arguments.getTo());
        ZoneIds.validate(arguments.getZone());

        //  initialize
        Writer<Transaction> writer = new TransactionWriter();
        writer.setPortfolioNameReplaces(arguments.getReplaces());
        writer.setHideTitle(arguments.isHideTitle());
        writer.setHideHeader(arguments.isHideHeader());
        writer.setLanguage(arguments.getLanguage());
        writer.setColumnsToHide(arguments.getColumnsToHide());
        writer.setDecimalFormat(arguments.getDecimalFormat());
        writer.setDateTimePattern(arguments.getDateTimePattern());
        writer.setZone(ZoneId.of(arguments.getZone()));
        writer.setFrom(LocalDateTimes.toLocalDateTime(arguments.getDateTimePattern(), arguments.getFrom()));
        writer.setTo(LocalDateTimes.getFilterTo(arguments.getDateTimePattern(), arguments.getTo()));
        return writer;
    }

    /**
     * Generate the CSV report.
     *
     * @param transactions list of the transactions
     * @return the report content as a String
     */
    @Override
    protected String buildCsvReport(List<Transaction> transactions) {
        var report = new StringBuilder();

        // report title
        if (!hideTitle) {
            report
                    .append(Label.LABEL_TRANSACTION_REPORT.getLabel(language))
                    .append(NEW_LINE)
                    .append(Label.TITLE_TRANSACTIONS_REPORT.getLabel(language))
                    .append(": ")
                    .append(LocalDateTimes.toNullSafeString(zone, dateTimePattern, LocalDateTime.now()))
                    .append(NEW_LINE);
        }

        // table header
        if (!hideHeader) {
            LabelCollection.TRANSACTION_TABLE_HEADERS
                    .stream()
                    .filter(label -> Filter.columnsToHideFilter(columnsToHide, label))
                    .forEach(label -> report.append(label.getLabel(language)).append(csvSeparator));
            report.setLength(report.length() - csvSeparator.length());
            report.append(NEW_LINE);
        }

        // data
        PortfolioNameRenamer.rename(transactions, portfolioNameReplaces);
        transactions
                .stream()
                .filter(t -> Filter.dateEqualOrAfterFilter(t.getTradeDate(), from))
                .filter(t -> Filter.dateEqualOrBeforeFilter(t.getTradeDate(), to))
                .sorted(Sorter.tradeDateComparator())
                .forEach(transaction -> report
                        .append(getCell(Label.HEADER_PORTFOLIO, transaction.getPortfolio(), csvSeparator))
                        .append(getCell(Label.HEADER_SYMBOL, transaction.getSymbol(), csvSeparator))
                        .append(getCell(Label.HEADER_TYPE, transaction.getType(), csvSeparator))
                        .append(getCell(Label.HEADER_VALUATION, transaction.getInventoryValuation(), csvSeparator))
                        .append(getCell(Label.HEADER_TRADE_DATE, transaction.getTradeDate(), csvSeparator))
                        .append(getCell(Label.HEADER_QUANTITY, transaction.getQuantity(), csvSeparator))
                        .append(getCell(Label.HEADER_PRICE, transaction.getPrice(), csvSeparator))
                        .append(getCell(Label.HEADER_PRICE_CURRENCY, transaction.getPriceCurrency(), csvSeparator))
                        .append(getCell(Label.HEADER_FEE, transaction.getFee(), csvSeparator))
                        .append(getCell(Label.HEADER_FEE_CURRENCY, transaction.getFeeCurrency(), csvSeparator))
                        .append(getCell(Label.HEADER_ORDER_ID, transaction.getOrderId(), csvSeparator))
                        .append(getCell(Label.HEADER_TRADE_ID, transaction.getTradeId(), csvSeparator))
                        .append(getCell(Label.HEADER_TRANSFER_ID, transaction.getTransferId()))
                        .append(NEW_LINE));
        return report.toString();
    }

    /**
     * Generate the Text/Markdown report.
     *
     * @param transactions list of the transactions
     * @return the report content as a String
     */
    @Override
    protected String buildMarkdownReport(List<Transaction> transactions) {
        var widths = calculateColumnWidth(transactions);
        var report = new StringBuilder();

        // report title
        if (!hideTitle) {
            report
                    .append("# ")
                    .append(Label.LABEL_TRANSACTION_REPORT.getLabel(language))
                    .append(NEW_LINE)
                    .append("_")
                    .append(Label.TITLE_TRANSACTIONS_REPORT.getLabel(language))
                    .append(": ")
                    .append(LocalDateTimes.toNullSafeString(zone, dateTimePattern, LocalDateTime.now()))
                    .append("_")
                    .append(NEW_LINE)
                    .append(NEW_LINE);
        }

        // table header
        if (!hideHeader && !transactions.isEmpty()) {
            var header = new StringBuilder();
            var headerSeparator = new StringBuilder();
            LabelCollection.TRANSACTION_TABLE_HEADERS
                    .stream()
                    .filter(label -> Filter.columnsToHideFilter(columnsToHide, label))
                    .forEach(labelKey -> {
                        var labelValue = labelKey.getLabel(language);
                        var width = widths.get(labelKey.name());
                        header.append(markdownSeparator).append(Strings.leftPad(labelValue, width));
                        headerSeparator.append(markdownSeparator).append("-".repeat(width));
                    });

            header.append(markdownSeparator).append(NEW_LINE);
            headerSeparator.append(markdownSeparator).append(NEW_LINE);
            report.append(header).append(headerSeparator);
        }

        // data
        PortfolioNameRenamer.rename(transactions, portfolioNameReplaces);
        transactions
                .stream()
                .filter(t -> Filter.dateEqualOrAfterFilter(t.getTradeDate(), from))
                .filter(t -> Filter.dateEqualOrBeforeFilter(t.getTradeDate(), to))
                .sorted(Sorter.tradeDateComparator())
                .forEach(transaction -> {
                    report.append(getCell(Label.HEADER_PORTFOLIO, transaction.getPortfolio(), widths));
                    report.append(getCell(Label.HEADER_SYMBOL, transaction.getSymbol(), widths));
                    report.append(getCell(Label.HEADER_TYPE, transaction.getType(), widths));
                    report.append(getCell(Label.HEADER_VALUATION, transaction.getInventoryValuation(), widths));
                    report.append(getCell(Label.HEADER_TRADE_DATE, transaction.getTradeDate(), widths));
                    report.append(getCell(Label.HEADER_QUANTITY, transaction.getQuantity(), widths));
                    report.append(getCell(Label.HEADER_PRICE, transaction.getPrice(), widths));
                    report.append(getCell(Label.HEADER_PRICE_CURRENCY, transaction.getPriceCurrency(), widths));
                    report.append(getCell(Label.HEADER_FEE, transaction.getFee(), widths));
                    report.append(getCell(Label.HEADER_FEE_CURRENCY, transaction.getFeeCurrency(), widths));
                    report.append(getCell(Label.HEADER_ORDER_ID, transaction.getOrderId(), widths));
                    report.append(getCell(Label.HEADER_TRADE_ID, transaction.getTradeId(), widths));
                    report.append(getCell(Label.HEADER_TRANSFER_ID, transaction.getTransferId(), widths));
                    report.append(markdownSeparator).append(NEW_LINE);
                });

        return report.toString();
    }

    /**
     * Get the history data from file.
     *
     * @param filename data file name
     */
    @Override
    protected List<Transaction> getHistoryFromFile(String filename) {
        var inputArgGroup = buildTransactionParserInputArgGroup(filename);
        var parser = TransactionParser.build(inputArgGroup);
        return parser.parse(inputArgGroup.getFile());
    }

    /**
     * Calculate the with of the columns that are shown in the report.
     *
     * @param transactions list of the transactions
     * @return length of the columns
     */
    private Map<String, Integer> calculateColumnWidth(List<Transaction> transactions) {
        Map<String, Integer> widths = new HashMap<>();
        transactions.forEach(transaction -> {
            updateWidth(widths, Label.HEADER_PORTFOLIO, transaction.getPortfolio());
            updateWidth(widths, Label.HEADER_TYPE, transaction.getType());
            updateWidth(widths, Label.HEADER_VALUATION, transaction.getInventoryValuation());
            updateWidth(widths, Label.HEADER_TRADE_DATE, transaction.getTradeDate());
            updateWidth(widths, Label.HEADER_QUANTITY, transaction.getQuantity());
            updateWidth(widths, Label.HEADER_PRICE, transaction.getPrice());
            updateWidth(widths, Label.HEADER_PRICE_CURRENCY, transaction.getPriceCurrency());
            updateWidth(widths, Label.HEADER_FEE, transaction.getFee());
            updateWidth(widths, Label.HEADER_FEE_CURRENCY, transaction.getFeeCurrency());
            updateWidth(widths, Label.HEADER_SYMBOL, transaction.getSymbol());
            updateWidth(widths, Label.HEADER_TRANSFER_ID, transaction.getTransferId());
            updateWidth(widths, Label.HEADER_TRADE_ID, transaction.getTradeId());
            updateWidth(widths, Label.HEADER_ORDER_ID, transaction.getOrderId());
        });
        return widths;
    }
}
