package com.remal.portfolio.writer;

import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.ProductPrice;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Enums;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.PortfolioNameRenamer;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import com.remal.portfolio.util.ZoneIds;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
public class ProductPriceWriter extends Writer<ProductPrice> {

    /**
     * Builder that initializes a new writer instance.
     *
     * @param arguments input arguments
     * @return the writer instance
     */
    public static Writer<ProductPrice> build(PriceArgGroup.OutputArgGroup arguments) {
        // validating the output params
        ZoneIds.validate(arguments.getZone());

        //  initialize
        Writer<ProductPrice> writer = new ProductPriceWriter();
        /*writer.setHideTitle(arguments.isHideTitle());
        writer.setHideHeader(arguments.isHideHeader());
        writer.setLanguage(arguments.getLanguage());
        writer.setDecimalFormat(arguments.getDecimalFormat());
        writer.setDecimalGroupingSeparator(Character.MIN_VALUE);
        writer.setDateTimePattern(arguments.getDateTimePattern());
        writer.setZone(ZoneId.of(arguments.getZone()));*/
        return writer;

    }

    /**
     * Generate the CSV report.
     *
     * @param transactions list of the transactions
     * @return the report content as a String
     */
    @Override
    protected String buildCsvReport(List<ProductPrice> productPrices) {
        var report = new StringBuilder();

        // report title
        if (!hideTitle) {
            report
                    .append(Label.LABEL_TRANSACTION_REPORT.getLabel(language))
                    .append(NEW_LINE)
                    .append(Label.LABEL_GENERATED.getLabel(language))
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
        /*PortfolioNameRenamer.rename(transactions, portfolioNameReplaces);
        transactions
                .stream()
                .filter(t -> Filter.dateEqualOrAfterFilter(from, t))
                .filter(t -> Filter.dateEqualOrBeforeFilter(to, t))
                .sorted(Sorter.tradeDateComparator())
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
                });*/
        return report.toString();
    }

    /**
     * Generate the Excel report.
     *
     * @param transactions list of the transactions
     * @return the report content as bytes
     */
    @Override
    protected byte[] buildExcelReport(List<ProductPrice> productPrices) {
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
            cell.setCellValue(Label.LABEL_GENERATED.getLabel(language) + ": "
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
        /*PortfolioNameRenamer.rename(transactions, portfolioNameReplaces);
        transactions
                .stream()
                .filter(t -> Filter.dateEqualOrAfterFilter(from, t))
                .filter(t -> Filter.dateEqualOrBeforeFilter(to, t))
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
*/
        return workbookToBytes(workbook);
    }


    /**
     * Generate the Text/Markdown report.
     *
     * @param transactions list of the transactions
     * @return the report content as a String
     */
    @Override
    protected String buildMarkdownReport(List<ProductPrice> productPrices) {
        /*var widths = calculateColumnWidth(transactions);
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
                    .append(LocalDateTimes.toString(zone, dateTimePattern, LocalDateTime.now()))
                    .append("_")
                    .append(NEW_LINE)
                    .append(NEW_LINE);
        }

        // table header
     /*   if (!hideHeader && !transactions.isEmpty()) {
            var header = new StringBuilder();
            var headerSeparator = new StringBuilder();
    /*        LabelCollection.TRANSACTION_TABLE_HEADERS
                    .stream()
                    .filter(label -> Filter.columnsToHideFilter(columnsToHide, label))
                    .forEach(labelKey -> {
                        var labelValue = labelKey.getLabel(language);
                        var width = widths.get(labelKey.getId());
                        header.append(markdownSeparator).append(Strings.leftPad(labelValue, width));
                        headerSeparator.append(markdownSeparator).append("-".repeat(width));
                    });

       /*     header.append(markdownSeparator).append(NEW_LINE);
            headerSeparator.append(markdownSeparator).append(NEW_LINE);
            report.append(header).append(headerSeparator);
        }

        // data
        PortfolioNameRenamer.rename(transactions, portfolioNameReplaces);
        transactions
                .stream()
                .filter(t -> Filter.dateEqualOrAfterFilter(from, t))
                .filter(t -> Filter.dateEqualOrBeforeFilter(to, t))
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

        return report.toString();*/
        return null;
    }

    /**
     * Set the cell value if the object is not null, otherwise skip
     * the set operation.
     *
     * @param row row in the Excel spreadsheet
     * @param columnIndex column index within the row
     * @param obj the value to be set as a cell value
     */
    private void skipIfNullOrSet(XSSFWorkbook workbook, XSSFRow row, AtomicInteger columnIndex, Object obj) {
        if (Objects.isNull(obj)) {
            columnIndex.incrementAndGet();
        } else {
            if (obj instanceof BigDecimal x) {
                getNextCell(row, columnIndex).setCellValue(BigDecimals.valueOf(x));
            } else if (obj instanceof String x) {
                getNextCell(row, columnIndex).setCellValue(x);
            } else if (obj instanceof LocalDateTime x) {
                var cell = getNextCell(row, columnIndex);
                CellStyle dateCellStyle = workbook.createCellStyle();
                CreationHelper creationHelper = workbook.getCreationHelper();
                dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat(dateTimePattern));

                cell.setCellValue(x);
                cell.setCellStyle(dateCellStyle);

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
    private XSSFCell getNextCell(XSSFRow row, AtomicInteger columnIndex) {
        return row.createCell(columnIndex.incrementAndGet());
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
