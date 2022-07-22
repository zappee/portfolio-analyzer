package com.remal.portfolio.writer;

import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.MultiplicityType;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.util.Enums;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import com.remal.portfolio.util.ZoneIds;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class PriceWriter extends Writer<Price> {

    /**
     * Controls the price export to file.
     */
    @Setter
    private MultiplicityType multiplicity;

    /**
     * Builder that initializes a new writer instance.
     *
     * @param arguments input arguments
     * @return          the writer instance
     */
    public static Writer<Price> build(PriceArgGroup.OutputArgGroup arguments) {
        // validating the output params
        ZoneIds.validate(arguments.getZone());

        //  initialize
        PriceWriter writer = new PriceWriter();
        writer.setLanguage(arguments.getLanguage());
        writer.setDecimalFormat(arguments.getDecimalFormat());
        writer.setDecimalGroupingSeparator(Character.MIN_VALUE);
        writer.setDateTimePattern(arguments.getDateTimePattern());
        writer.setZone(ZoneId.of(arguments.getZone()));
        writer.setMultiplicity(arguments.getMultiplicity());
        return writer;
    }

    /**
     * Generate the CSV report.
     *
     * @param prices list of the prices
     * @return       the report content as a String
     */
    @Override
    protected String buildCsvReport(List<Price> prices) {
        reduceBasedOnMultiplicity(prices, multiplicity);

        // table header
        var report = new StringBuilder();
        LabelCollection.PRODUCT_PRICE_HEADERS
                .forEach(label -> report.append(label.getLabel(language)).append(csvSeparator));
        report.setLength(report.length() - csvSeparator.length());
        report.append(NEW_LINE);

        // data
        prices
                .stream()
                .sorted(Sorter.priceComparator())
                .forEach(productPrice -> report
                    .append(getCell(Label.TICKER, productPrice.getTicker(), csvSeparator))
                    .append(getCell(Label.PRICE, productPrice.getUnitPrice(), csvSeparator))
                    .append(getCell(Label.DATE, productPrice.getDate(), csvSeparator))
                    .append(getCell(Label.DATA_PROVIDER, productPrice.getProviderType()))
                    .append(NEW_LINE));

        return report.toString();
    }

    /**
     * Generate the Excel report.
     *
     * @param prices list of the prices
     * @return       the report content as bytes
     */
    @Override
    protected byte[] buildExcelReport(List<Price> prices) {
        reduceBasedOnMultiplicity(prices, multiplicity);

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
        prices
                .stream()
                .sorted(Sorter.priceComparator())
                .forEach(productPrice -> {
                    var dataRow = sheet.createRow(rowIndex.incrementAndGet());
                    var index = new AtomicInteger(-1);

                    skipIfNullOrSet(workbook, dataRow, index, productPrice.getTicker());
                    skipIfNullOrSet(workbook, dataRow, index, productPrice.getUnitPrice());
                    skipIfNullOrSet(workbook, dataRow, index, productPrice.getDate());
                    skipIfNullOrSet(workbook, dataRow, index, Enums.enumToString(productPrice.getProviderType()));
                });

        return workbookToBytes(workbook);
    }

    /**
     * Generate the Text/Markdown report.
     *
     * @param prices list of the prices
     * @return       the report content as a String
     */
    @Override
    protected String buildMarkdownReport(List<Price> prices) {
        reduceBasedOnMultiplicity(prices, multiplicity);

        var widths = calculateColumnWidth(prices);

        // table header
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

        var report = new StringBuilder();
        report.append(header).append(headerSeparator);

        // data
        prices
                .stream()
                .sorted(Sorter.priceComparator())
                .forEach(productPrice -> {
                    report.append(getCell(Label.TICKER, productPrice.getTicker(), widths));
                    report.append(getCell(Label.PRICE, productPrice.getUnitPrice(), widths));
                    report.append(getCell(Label.DATE, productPrice.getDate(), widths));
                    report.append(getCell(Label.DATA_PROVIDER, productPrice.getProviderType(), widths));
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
    protected List<Price> getHistoryFromFile(String filename) {
        var outputArgGroup = new PriceArgGroup.OutputArgGroup();
        outputArgGroup.setZone(zone.getId());
        outputArgGroup.setDateTimePattern(dateTimePattern);

        Parser<Price> parser = Parser.build(outputArgGroup);
        return parser.parse(filename);
    }

    /**
     * Calculate the with of the columns that are shown in the report.
     *
     * @param prices list of the prices
     * @return       length of the columns
     */
    private Map<String, Integer> calculateColumnWidth(List<Price> prices) {
        Map<String, Integer> widths = new HashMap<>();
        prices.forEach(productPrice -> {
            updateWidth(widths, Label.TICKER, productPrice.getTicker());
            updateWidth(widths, Label.PRICE, productPrice.getUnitPrice());
            updateWidth(widths, Label.DATE, productPrice.getDate());
            updateWidth(widths, Label.DATA_PROVIDER, productPrice.getProviderType());
        });
        return widths;
    }

    /**
     * Filter out the unacceptable items from the price list.
     *
     * @param prices the product price list
     * @param multiplicity  controls how to add the price to the list
     */
    public static void reduceBasedOnMultiplicity(List<Price> prices, MultiplicityType multiplicity) {
        var correction = 1; // avoid the overlapping of the intervals

        log.debug("> multiplicity: {}", multiplicity.name());
        log.debug("> number of item before the reduce: {}", prices.size());

        prices.removeIf(price -> {
            var intervalEnd = price.getDate();
            var intervalStart = intervalEnd.minusSeconds(multiplicity.getRangeLengthInSec() - correction);
            var itemCount = prices
                    .stream()
                    .filter(x -> x.getTicker().equals(price.getTicker()))
                    .filter(x -> Filter.dateBetweenFilter(intervalStart, intervalEnd, x.getDate()))
                    .count();
            return multiplicity != MultiplicityType.MANY && itemCount > 1;
        });
        log.debug("> number of item after the reduce: {}", prices.size());
    }
}
