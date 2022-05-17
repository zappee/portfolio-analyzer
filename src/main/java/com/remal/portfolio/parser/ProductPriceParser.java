package com.remal.portfolio.parser;

import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.ProductPrice;
import com.remal.portfolio.model.ProviderType;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.Sorter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Parse file that keeps market prices.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class ProductPriceParser extends Parser<ProductPrice> {

    /**
     * Process a CSV file.
     *
     * @param fileName path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<ProductPrice> parseCsvFile(String fileName) {
        List<ProductPrice> productPrices = new ArrayList<>();
        try {
            var skipRows = 1;
            var firstColumn = 0;
            productPrices.addAll(parseTextFile(skipRows, firstColumn, fileName, csvSeparator));

        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logErrorAndExit(LOG_ERROR_ARRAY_INDEX, fileName, e.getMessage());
        } catch (Exception e) {
            Logger.logErrorAndExit(LOG_ERROR_GENERAL, fileName, e.toString());
        }

        return productPrices
                .stream()
                .sorted(Sorter.productPriceComparator())
                .toList();
    }

    /**
     * Process an Excel file.
     *
     * @param fileName path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<ProductPrice> parseExcelFile(String fileName) {
        List<ProductPrice> productPrices = new ArrayList<>();
        try (var xlsFile = new FileInputStream(fileName)) {
            var firstRow = 1;
            var workbook = new XSSFWorkbook(xlsFile);
            var sheet = workbook.getSheetAt(0);
            var lastRow = sheet.getLastRowNum() + 1;
            log.debug("selecting rows from {} to {} in the excel spreadsheet", firstRow, lastRow);

            IntStream.range(firstRow, lastRow).forEach(rowIndex -> {
                var row = sheet.getRow(rowIndex);
                var p = ProductPrice.builder();

                p.ticker(getCellValueAsString(row, 0));
                p.price(getCellValueAsBigDecimal(row, 1));
                p.date(getCellValueAsLocalDateTime(row, 2));
                p.providerType(ProviderType.valueOf(getCellValueAsString(row, 3)));
                productPrices.add(p.build());
            });
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logErrorAndExit(LOG_ERROR_ARRAY_INDEX, fileName, e.getMessage());
        } catch (Exception e) {
            Logger.logErrorAndExit(LOG_ERROR_GENERAL, fileName, e.toString());
        }

        return productPrices
                .stream()
                .sorted(Sorter.productPriceComparator())
                .toList();
    }

    /**
     * Process a Text/Markdown file.
     *
     * @param fileName path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<ProductPrice> parseMarkdownFile(String fileName) {
        List<ProductPrice> productPrices = new ArrayList<>();
        try {
            var skipRows = 2;
            var firstColumn = 1;
            productPrices.addAll(parseTextFile(skipRows, firstColumn, fileName, markdownSeparator));

        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logErrorAndExit(LOG_ERROR_ARRAY_INDEX, fileName, e.getMessage());
        } catch (Exception e) {
            Logger.logErrorAndExit(LOG_ERROR_GENERAL, fileName, e.toString());
        }

        return productPrices
                .stream()
                .sorted(Sorter.productPriceComparator())
                .toList();
    }

    /**
     * Parse the Markdown and CSV file.
     *
     * @param skipRows number of the lines that must be skip while parsing the file
     * @param firstColumn index from here starts to read the columns
     * @param file the input file
     * @param separator separator char used in the input file
     * @return the list of the transactions
     * @throws IOException in case of file not found
     */
    private List<ProductPrice> parseTextFile(int skipRows, int firstColumn, String file, String separator)
            throws IOException {

        List<ProductPrice> productPrices = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Path.of(file))) {
            stream
                    .skip(skipRows)
                    .forEach(line -> {
                        var fields = line.split(separator, -1);
                        var index = new AtomicInteger(firstColumn);
                        ProductPrice p = ProductPrice
                                .builder()
                                .ticker(getString(index, fields, Label.TICKER))
                                .price(getBigDecimal(index, fields, Label.PRICE))
                                .date(getLocalDateTime(index, fields))
                                .providerType(getProviderType(index, fields))
                                .build();
                        productPrices.add(p);
                    });
        }
        return productPrices;
    }

    /**
     * Get the value based on the missing/hidden columns.
     *
     * @param index the variable holds the index's value
     * @param fields the parsed line from the input file
     * @return the next index value
     */
    private ProviderType getProviderType(AtomicInteger index, String[] fields) {
        if (missingColumns.contains(Label.CURRENCY.getId())) {
            return null;
        } else {
            return ProviderType.getEnum(fields[index.getAndIncrement()].trim());
        }
    }
}
