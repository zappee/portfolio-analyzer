package com.remal.portfolio.writer;

import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.picocli.arggroup.OutputArgGroup;
import com.remal.portfolio.util.Enums;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.PortfolioNameRenamer;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import com.remal.portfolio.util.ZoneIds;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
        writer.setDecimalGroupingSeparator(Character.MIN_VALUE);
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
                    .append(LocalDateTimes.toString(zone, dateTimePattern, LocalDateTime.now()))
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
                        .append(getCell(Label.PORTFOLIO, transaction.getPortfolio())).append(csvSeparator)
                        .append(getCell(Label.TICKER, transaction.getTicker())).append(csvSeparator)
                        .append(getCell(Label.TYPE, transaction.getType())).append(csvSeparator)
                        .append(getCell(Label.VALUATION, transaction.getInventoryValuation())).append(csvSeparator)
                        .append(getCell(Label.TRADE_DATE, transaction.getTradeDate())).append(csvSeparator)
                        .append(getCell(Label.QUANTITY, transaction.getQuantity())).append(csvSeparator)
                        .append(getCell(Label.PRICE, transaction.getPrice())).append(csvSeparator)
                        .append(getCell(Label.FEE, transaction.getFee())).append(csvSeparator)
                        .append(getCell(Label.CURRENCY, transaction.getCurrency())).append(csvSeparator)
                        .append(getCell(Label.ORDER_ID, transaction.getOrderId())).append(csvSeparator)
                        .append(getCell(Label.TRADE_ID, transaction.getTradeId())).append(csvSeparator)
                        .append(getCell(Label.TRANSFER_ID, transaction.getTransferId())).append(NEW_LINE));
        return report.toString();
    }

    /**
     * Generate the Excel report.
     *
     * @param transactions list of the transactions
     * @return the report content as bytes
     */
    @Override
    protected byte[] buildExcelReport(List<Transaction> transactions) {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet(Label.LABEL_TRANSACTION_REPORT.getLabel(language));
        var rowIndex = new AtomicInteger(-1);

        // report title
        if (!hideTitle) {
            var columnIndex = 0;

            // row 1
            var row = sheet.createRow(rowIndex.incrementAndGet());
            var cell = row.createCell(columnIndex);
            cell.setCellValue(Label.LABEL_TRANSACTION_REPORT.getLabel(language));

            // row 2
            row = sheet.createRow(rowIndex.incrementAndGet());
            cell = row.createCell(columnIndex);
            cell.setCellValue(Label.TITLE_TRANSACTIONS_REPORT.getLabel(language) + ": "
                    + LocalDateTimes.toString(zone, dateTimePattern, LocalDateTime.now()));
        }

        // table header
        if (!hideHeader) {
            var columnIndex = new AtomicInteger(-1);
            var row = sheet.createRow(rowIndex.incrementAndGet());
            LabelCollection.TRANSACTION_TABLE_HEADERS
                    .stream()
                    .filter(label -> Filter.columnsToHideFilter(columnsToHide, label))
                    .forEach(label -> {
                        var cell = row.createCell(columnIndex.incrementAndGet());
                        cell.setCellValue(label.getLabel(language));
                    });
        }

        // data
        PortfolioNameRenamer.rename(transactions, portfolioNameReplaces);
        transactions
                .stream()
                .filter(t -> Filter.dateEqualOrAfterFilter(t.getTradeDate(), from))
                .filter(t -> Filter.dateEqualOrBeforeFilter(t.getTradeDate(), to))
                .sorted(Sorter.tradeDateComparator())
                .forEach(transaction -> {
                    var row = sheet.createRow(rowIndex.incrementAndGet());
                    var index = new AtomicInteger(-1);

                    skipIfNullOrSet(workbook, row, index, transaction.getPortfolio());
                    skipIfNullOrSet(workbook, row, index, transaction.getTicker());
                    skipIfNullOrSet(workbook, row, index, Enums.enumToString(transaction.getType()));
                    skipIfNullOrSet(workbook, row, index, Enums.enumToString(transaction.getInventoryValuation()));
                    skipIfNullOrSet(workbook, row, index, transaction.getTradeDate());
                    skipIfNullOrSet(workbook, row, index, transaction.getQuantity());
                    skipIfNullOrSet(workbook, row, index, transaction.getPrice());
                    skipIfNullOrSet(workbook, row, index, transaction.getFee());
                    skipIfNullOrSet(workbook, row, index, Enums.enumToString(transaction.getCurrency()));
                    skipIfNullOrSet(workbook, row, index, transaction.getOrderId());
                    skipIfNullOrSet(workbook, row, index, transaction.getTradeId());
                    skipIfNullOrSet(workbook, row, index, transaction.getTransferId());
                });

        return workbookToBytes(workbook);
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
                    .append(LocalDateTimes.toString(zone, dateTimePattern, LocalDateTime.now()))
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
                        var width = widths.get(labelKey.getId());
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
                    report.append(getCell(Label.PORTFOLIO, transaction.getPortfolio(), widths));
                    report.append(getCell(Label.TICKER, transaction.getTicker(), widths));
                    report.append(getCell(Label.TYPE, transaction.getType(), widths));
                    report.append(getCell(Label.VALUATION, transaction.getInventoryValuation(), widths));
                    report.append(getCell(Label.TRADE_DATE, transaction.getTradeDate(), widths));
                    report.append(getCell(Label.QUANTITY, transaction.getQuantity(), widths));
                    report.append(getCell(Label.PRICE, transaction.getPrice(), widths));
                    report.append(getCell(Label.FEE, transaction.getFee(), widths));
                    report.append(getCell(Label.CURRENCY, transaction.getCurrency(), widths));
                    report.append(getCell(Label.ORDER_ID, transaction.getOrderId(), widths));
                    report.append(getCell(Label.TRADE_ID, transaction.getTradeId(), widths));
                    report.append(getCell(Label.TRANSFER_ID, transaction.getTransferId(), widths));
                    report.append(markdownSeparator).append(NEW_LINE);
                });

        return report.toString();
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
            updateWidth(widths, Label.PORTFOLIO, transaction.getPortfolio());
            updateWidth(widths, Label.TYPE, transaction.getType());
            updateWidth(widths, Label.VALUATION, transaction.getInventoryValuation());
            updateWidth(widths, Label.TRADE_DATE, transaction.getTradeDate());
            updateWidth(widths, Label.QUANTITY, transaction.getQuantity());
            updateWidth(widths, Label.PRICE, transaction.getPrice());
            updateWidth(widths, Label.FEE, transaction.getFee());
            updateWidth(widths, Label.CURRENCY, transaction.getCurrency());
            updateWidth(widths, Label.TICKER, transaction.getTicker());
            updateWidth(widths, Label.TRANSFER_ID, transaction.getTransferId());
            updateWidth(widths, Label.TRADE_ID, transaction.getTradeId());
            updateWidth(widths, Label.ORDER_ID, transaction.getOrderId());
        });
        return widths;
    }
}
