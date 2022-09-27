package com.remal.portfolio.writer;

import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.MultiplicityType;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import com.remal.portfolio.util.ZoneIds;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.util.ArrayList;
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
     * @return the writer instance
     */
    public static Writer<Price> build(PriceArgGroup.OutputArgGroup arguments) {
        // validating the output params
        ZoneIds.validate(arguments.getZone());

        //  initialize
        var writer = new PriceWriter();
        writer.setLanguage(arguments.getLanguage());
        writer.setDecimalFormat(arguments.getDecimalFormat());
        writer.setDateTimePattern(arguments.getDateTimePattern());
        writer.setZone(ZoneId.of(arguments.getZone()));
        writer.setMultiplicity(arguments.getMultiplicity());
        return writer;
    }

    /**
     * Generate the CSV report.
     *
     * @param prices list of the prices
     * @return the report content as a String
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
                    .append(getCell(Label.HEADER_SYMBOL, productPrice.getSymbol(), csvSeparator))
                    .append(getCell(Label.HEADER_PRICE, productPrice.getUnitPrice(), csvSeparator))
                    .append(getCell(Label.HEADER_TRADE_DATE, productPrice.getTradeDate(), csvSeparator))
                    .append(getCell(Label.HEADER_REQUEST_DATE, productPrice.getRequestDate(), csvSeparator))
                    .append(getCell(Label.HEADER_DATA_PROVIDER, productPrice.getDataProvider()))
                    .append(NEW_LINE));

        return report.toString();
    }

    /**
     * Generate the Text/Markdown report.
     *
     * @param prices list of the prices
     * @return the report content as a String
     */
    @Override
    protected String buildMarkdownReport(List<Price> prices) {
        if (prices.size() > 1) {
            reduceBasedOnMultiplicity(prices, multiplicity);
        }

        var widths = calculateColumnWidth(prices);

        // table header
        var header = new StringBuilder();
        var headerSeparator = new StringBuilder();
        LabelCollection.PRODUCT_PRICE_HEADERS
                .forEach(labelKey -> {
                    var labelValue = labelKey.getLabel(language);
                    var width = widths.get(labelKey.name());
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
                    report.append(getCell(Label.HEADER_SYMBOL, productPrice.getSymbol(), widths));
                    report.append(getCell(Label.HEADER_PRICE, productPrice.getUnitPrice(), widths));
                    report.append(getCell(Label.HEADER_TRADE_DATE, productPrice.getTradeDate(), widths));
                    report.append(getCell(Label.HEADER_REQUEST_DATE, productPrice.getRequestDate(), widths));
                    report.append(getCell(Label.HEADER_DATA_PROVIDER, productPrice.getDataProvider(), widths));
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
     * @return length of the columns
     */
    private Map<String, Integer> calculateColumnWidth(List<Price> prices) {
        Map<String, Integer> widths = new HashMap<>();
        prices.forEach(productPrice -> {
            updateWidth(widths, Label.HEADER_SYMBOL, productPrice.getSymbol());
            updateWidth(widths, Label.HEADER_PRICE, productPrice.getUnitPrice());
            updateWidth(widths, Label.HEADER_TRADE_DATE, productPrice.getTradeDate());
            updateWidth(widths, Label.HEADER_REQUEST_DATE, productPrice.getRequestDate());
            updateWidth(widths, Label.HEADER_DATA_PROVIDER, productPrice.getDataProvider());
        });
        return widths;
    }

    /**
     * Filter out the unacceptable items from the price list.
     *
     * @param prices the product price list
     * @param multiplicity controls how to add the price to the list
     */
    public static void reduceBasedOnMultiplicity(List<Price> prices, MultiplicityType multiplicity) {
        log.debug("> multiplicity: {}", multiplicity.name());
        log.debug("> number of items before the reduce: {}", prices.size());

        List<Price> reducedPrices = new ArrayList<>();
        prices.forEach(price -> {
            var intervalStart = price.getTradeDate();
            var intervalEnd = intervalStart.plusSeconds(multiplicity.getRangeLengthInSec());
            var itemCount = reducedPrices
                    .stream()
                    .filter(x -> x.getSymbol().equals(price.getSymbol()))
                    .filter(x -> Filter.dateBetweenFilter(intervalStart, intervalEnd, x.getRequestDate()))
                    .count();
            if (multiplicity != MultiplicityType.MANY && itemCount == 0) {
                reducedPrices.add(price);
            }
        });
        prices.clear();
        prices.addAll(reducedPrices);

        log.debug("> number of items after the reduce: {}", prices.size());
    }
}
