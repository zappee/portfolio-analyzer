package com.remal.portfolio.writer;

import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.ProductPrice;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.util.Enums;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import com.remal.portfolio.util.ZoneIds;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
public class PriceWriter extends Writer<ProductPrice> {

    /**
     * Builder that initializes a new writer instance.
     *
     * @param arguments input arguments
     * @return          the writer instance
     */
    public static Writer<ProductPrice> build(PriceArgGroup.OutputArgGroup arguments) {
        // validating the output params
        ZoneIds.validate(arguments.getZone());

        //  initialize
        Writer<ProductPrice> writer = new PriceWriter();
        writer.setLanguage(arguments.getLanguage());
        writer.setDecimalFormat(arguments.getDecimalFormat());
        writer.setDecimalGroupingSeparator(Character.MIN_VALUE);
        writer.setDateTimePattern(arguments.getDateTimePattern());
        writer.setZone(ZoneId.of(arguments.getZone()));
        return writer;
    }

    /**
     * Generate the CSV report.
     *
     * @param productPrices list of the prices
     * @return              the report content as a String
     */
    @Override
    protected String buildCsvReport(List<ProductPrice> productPrices) {
        var report = new StringBuilder();

        // table header
        LabelCollection.PRODUCT_PRICE_HEADERS
                .forEach(label -> report.append(label.getLabel(language)).append(csvSeparator));
        report.setLength(report.length() - csvSeparator.length());
        report.append(NEW_LINE);

        // data
        productPrices
                .stream()
                .sorted(Sorter.productPriceComparator())
                .forEach(productPrice -> report
                    .append(getCell(Label.TICKER, productPrice.getTicker(), csvSeparator))
                    .append(getCell(Label.PRICE, productPrice.getPrice(), csvSeparator))
                    .append(getCell(Label.DATE, productPrice.getDate(), csvSeparator))
                    .append(getCell(Label.DATA_PROVIDER, productPrice.getProviderType()))
                    .append(NEW_LINE));

        return report.toString();
    }

    /**
     * Generate the Excel report.
     *
     * @param productPrices list of the prices
     * @return              the report content as bytes
     */
    @Override
    protected byte[] buildExcelReport(List<ProductPrice> productPrices) {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet(Label.LABEL_PRICE_HISTORY.getLabel(language));

        // table header
        var rowIndex = new AtomicInteger(-1);
        var columnIndex = new AtomicInteger(-1);
        var headerRow = sheet.createRow(rowIndex.incrementAndGet());
        LabelCollection.PRODUCT_PRICE_HEADERS
                .forEach(label -> {
                    var cell = headerRow.createCell(columnIndex.incrementAndGet());
                    cell.setCellValue(label.getLabel(language));
                });

        // data
        productPrices
                .stream()
                .sorted(Sorter.productPriceComparator())
                .forEach(productPrice -> {
                    var dataRow = sheet.createRow(rowIndex.incrementAndGet());
                    var index = new AtomicInteger(-1);

                    skipIfNullOrSet(workbook, dataRow, index, productPrice.getTicker());
                    skipIfNullOrSet(workbook, dataRow, index, productPrice.getPrice());
                    skipIfNullOrSet(workbook, dataRow, index, productPrice.getDate());
                    skipIfNullOrSet(workbook, dataRow, index, Enums.enumToString(productPrice.getProviderType()));
                });

        return workbookToBytes(workbook);
    }

    /**
     * Generate the Text/Markdown report.
     *
     * @param productPrices list of the prices
     * @return              the report content as a String
     */
    @Override
    protected String buildMarkdownReport(List<ProductPrice> productPrices) {
        var widths = calculateColumnWidth(productPrices);
        var report = new StringBuilder();

        // table header
        if (!productPrices.isEmpty()) {
            var header = new StringBuilder();
            var headerSeparator = new StringBuilder();
            LabelCollection.PRODUCT_PRICE_HEADERS
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
        productPrices
                .stream()
                .sorted(Sorter.productPriceComparator())
                .forEach(productPrice -> {
                    report.append(getCell(Label.TICKER, productPrice.getTicker(), widths));
                    report.append(getCell(Label.PRICE, productPrice.getPrice(), widths));
                    report.append(getCell(Label.DATE, productPrice.getDate(), widths));
                    report.append(getCell(Label.DATA_PROVIDER, productPrice.getProviderType(), widths));
                    report.append(markdownSeparator).append(NEW_LINE);
                });

        return report.toString();
    }

    /**
     * Calculate the with of the columns that are shown in the report.
     *
     * @param productPrices list of the prices
     * @return              length of the columns
     */
    private Map<String, Integer> calculateColumnWidth(List<ProductPrice> productPrices) {
        Map<String, Integer> widths = new HashMap<>();
        productPrices.forEach(productPrice -> {
            updateWidth(widths, Label.TICKER, productPrice.getTicker());
            updateWidth(widths, Label.PRICE, productPrice.getPrice());
            updateWidth(widths, Label.DATE, productPrice.getDate());
            updateWidth(widths, Label.DATA_PROVIDER, productPrice.getProviderType());
        });
        return widths;
    }
}
