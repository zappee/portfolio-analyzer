package com.remal.portfolio.writer;

import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.picocli.command.OutputCommandGroup;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Enums;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generate transaction reports.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class TransactionWriter extends Writer<Transaction> {

    /**
     * Builder that initializes a new writer instance.
     *
     * @param datePattern the date/time format that is used in the reports
     * @param zoneTo the timezone that is used to convert dates to user's timezone
     * @param outputArgGroup report and output file format
     * @return the writer
     */
    public static Writer<Transaction> build(String datePattern,
                                            String zoneTo,
                                            OutputCommandGroup.OutputArgGroup outputArgGroup) {

        Writer<Transaction> writer = new TransactionWriter();
        writer.setHideTitle(outputArgGroup.hideTitle);
        writer.setHideHeader(outputArgGroup.hideHeader);
        writer.setLanguage(outputArgGroup.language);
        writer.setColumnsToHide(outputArgGroup.columnsToHide);
        writer.setDateTimePattern(datePattern);
        writer.setDecimalFormat("##################.########");
        writer.setDecimalGroupingSeparator(Character.MIN_VALUE);
        writer.setZoneTo(zoneTo);
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
                    .append(Label.LABEL_GENERATED.getLabel(language))
                    .append(": ")
                    .append(LocalDateTimes.toString(zoneTo, dateTimePattern, LocalDateTime.now()))
                    .append(NEW_LINE);
        }

        // table header
        if (!hideHeader) {
            Arrays
                    .stream(LabelCollection.TRANSACTION_TABLE_HEADERS)
                    .filter(label -> !columnsToHide.contains(label.getId()))
                    .forEach(label -> report.append(label.getLabel(language)).append(csvSeparator));
            report.setLength(report.length() - csvSeparator.length());
            report.append(NEW_LINE);
        }

        // data
        transactions
                .forEach(transaction -> {
                    report.append(getCell(Label.PORTFOLIO, transaction.getPortfolio())).append(csvSeparator);
                    report.append(getCell(Label.TICKER, transaction.getTicker())).append(csvSeparator);
                    report.append(getCell(Label.TYPE, transaction.getType())).append(csvSeparator);
                    report.append(getCell(Label.VALUATION, transaction.getInventoryValuation())).append(csvSeparator);
                    report.append(getCell(Label.TRADE_DATE, transaction.getTradeDate())).append(csvSeparator);
                    report.append(getCell(Label.QUANTITY, transaction.getQuantity())).append(csvSeparator);
                    report.append(getCell(Label.PRICE, transaction.getPrice())).append(csvSeparator);
                    report.append(getCell(Label.FEE, transaction.getFee())).append(csvSeparator);
                    report.append(getCell(Label.CURRENCY, transaction.getCurrency())).append(csvSeparator);
                    report.append(getCell(Label.ORDER_ID, transaction.getOrderId())).append(csvSeparator);
                    report.append(getCell(Label.TRADE_ID, transaction.getTradeId())).append(csvSeparator);
                    report.append(getCell(Label.TRANSFER_ID, transaction.getTransferId())).append(NEW_LINE);
                });
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
            cell.setCellValue(Label.LABEL_GENERATED.getLabel(language) + ": " +
                    LocalDateTimes.toString(zoneTo, dateTimePattern, LocalDateTime.now()));
        }

        // table header
        if (!hideHeader) {
            var columnIndex = new AtomicInteger(-1);
            var row = sheet.createRow(rowIndex.incrementAndGet());
            Arrays
                    .stream(LabelCollection.TRANSACTION_TABLE_HEADERS)
                    .filter(label -> !columnsToHide.contains(label.getId()))
                    .forEach(label -> {
                        var cell = row.createCell(columnIndex.incrementAndGet());
                        cell.setCellValue(label.getLabel(language));
                    });
        }

        // data
        transactions
                .forEach(transaction -> {
                    var row = sheet.createRow(rowIndex.incrementAndGet());
                    var index = new AtomicInteger(-1);

                    skipIfNullOrSet(row, index, transaction.getPortfolio());
                    skipIfNullOrSet(row, index, transaction.getTicker());
                    skipIfNullOrSet(row, index, Enums.enumToString(transaction.getType()));
                    skipIfNullOrSet(row, index, Enums.enumToString(transaction.getInventoryValuation()));
                    skipIfNullOrSet(row, index, transaction.getTradeDate());
                    skipIfNullOrSet(row, index, transaction.getQuantity());
                    skipIfNullOrSet(row, index, transaction.getPrice());
                    skipIfNullOrSet(row, index, transaction.getFee());
                    skipIfNullOrSet(row, index, Enums.enumToString(transaction.getCurrency()));
                    skipIfNullOrSet(row, index, transaction.getOrderId());
                    skipIfNullOrSet(row, index, transaction.getTradeId());
                    skipIfNullOrSet(row, index, transaction.getTransferId());
                });

        return workbookToBytes(workbook);
    }

    /**
     * Set the cell value if the object is not null, otherwise skip
     * the set operation.
     *
     * @param row row in the Excel spreadsheet
     * @param columnIndex column index within the row
     * @param obj the value to be set as a cell value
     */
    private void skipIfNullOrSet(XSSFRow row, AtomicInteger columnIndex, Object obj) {
        if (Objects.isNull(obj)) {
            columnIndex.incrementAndGet();
        } else {
            if (obj instanceof BigDecimal x) {
                getNextCell(row, columnIndex).setCellValue(BigDecimals.valueOf(x));
            } else if (obj instanceof String x) {
                getNextCell(row, columnIndex).setCellValue(x);
            } else if (obj instanceof LocalDateTime x) {
                getNextCell(row, columnIndex).setCellValue(x);
            } else {
                columnIndex.incrementAndGet();
                log.warn("Unhandled type: {}", obj.getClass().getSimpleName());
            }
        }
    }

    /**
     * Get the next cell in the row.
     *
     * @param row row in the Excel spreadsheet
     * @param columnIndex column index within the row
     * @return the next cell in the row
     */
    private XSSFCell getNextCell (XSSFRow row, AtomicInteger columnIndex) {
        return row.createCell(columnIndex.incrementAndGet());
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
                    .append(Label.LABEL_GENERATED.getLabel(language))
                    .append(": ")
                    .append(LocalDateTimes.toString(zoneTo, dateTimePattern, LocalDateTime.now()))
                    .append("_")
                    .append(NEW_LINE)
                    .append(NEW_LINE);
        }

        // table header
        if (!hideHeader && !transactions.isEmpty()) {
            var header = new StringBuilder();
            var headerSeparator = new StringBuilder();
            Arrays
                    .stream(LabelCollection.TRANSACTION_TABLE_HEADERS)
                    .filter(label -> !columnsToHide.contains(label.getId()))
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
        transactions
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

    /**
     * Write Excel spreadsheet to a byte array.
     *
     * @param workbook the Excel spreadsheet
     * @return the Excel file as a byte array
     */
    private byte[] workbookToBytes(XSSFWorkbook workbook) {
        try (var outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            Logger.logErrorAndExit("Error while saving the Excel file, error: {}", e.toString());
        }
        return new byte[0];
    }
}
