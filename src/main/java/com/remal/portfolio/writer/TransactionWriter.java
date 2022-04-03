package com.remal.portfolio.writer;

import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.picocli.command.OutputCommandGroup;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        throw new NotImplementedException(null);
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
}
