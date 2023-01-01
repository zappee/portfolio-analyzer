package com.remal.portfolio.writer;

import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.MultiplicityType;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.util.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
     * Generate the CSV report.
     *
     * @param prices list of the prices
     * @return the report content as a String
     */
    @Override
    protected String buildCsvReport(List<Price> prices) {
        reduceBasedOnMultiplicity(prices, multiplicity);
        prices.sort(Comparator.comparing(Price::getSymbol).thenComparing(Price::getTradeDate));

        // table header
        var report = new StringBuilder();
        LabelCollection.PRODUCT_PRICE_HEADERS
                .forEach(label -> report.append(label.getLabel(language)).append(csvSeparator));
        report.setLength(report.length() - csvSeparator.length());
        report.append(NEW_LINE);

        // data
        prices
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
        reduceBasedOnMultiplicity(prices, multiplicity);
        prices.sort(Comparator.comparing(Price::getSymbol).thenComparing(Price::getTradeDate));
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
        outputArgGroup.setZone(inputZone.getId());
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

        prices.sort(Comparator.comparing(Price::getRequestDate));

        List<Price> reducedPrices = new ArrayList<>();
        Map<String, LocalDateTime> rangeEnds = new HashMap<>();

        for (Price price : prices) {
            var symbol = price.getSymbol();
            var rangeEnd = rangeEnds.get(symbol);
            if (Objects.isNull(rangeEnd)) {
                reducedPrices.add(price);
                rangeEnds.put(symbol, price.getRequestDate().plusSeconds(multiplicity.getRangeLengthInSec()));
            } else {
                if (price.getRequestDate().isAfter(rangeEnd)) {
                    reducedPrices.add(price);
                    rangeEnds.put(symbol, price.getTradeDate().plusSeconds(multiplicity.getRangeLengthInSec()));
                }
            }
        }

        prices.clear();
        prices.addAll(reducedPrices);
        log.debug("> number of items after the reduce: {}", prices.size());
    }
}
