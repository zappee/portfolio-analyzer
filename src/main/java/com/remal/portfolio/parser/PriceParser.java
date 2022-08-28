package com.remal.portfolio.parser;

import com.remal.portfolio.model.DataProviderType;
import com.remal.portfolio.model.FileType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.Price;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.Sorter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
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
public class PriceParser extends Parser<Price> {

    /**
     * Process a CSV file.
     *
     * @param fileName path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<Price> parseCsvFile(String fileName) {
        return parseTextFile(fileName, csvSeparator);
    }

    /**
     * Process an Excel file.
     *
     * @param fileName path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<Price> parseExcelFile(String fileName) {
        List<Price> prices = new ArrayList<>();
        try (var xlsFile = new FileInputStream(fileName)) {
            var firstRow = 1;
            var workbook = new XSSFWorkbook(xlsFile);
            var sheet = workbook.getSheetAt(0);
            var lastRow = sheet.getLastRowNum() + 1;
            log.debug("selecting rows from {} to {} in the excel spreadsheet", firstRow, lastRow);

            IntStream.range(firstRow, lastRow).forEach(rowIndex -> {
                var row = sheet.getRow(rowIndex);
                var p = Price.builder();

                p.symbol(getCellValueAsString(row, 0));
                p.unitPrice(getCellValueAsBigDecimal(row, 1));
                p.tradeDate(getCellValueAsLocalDateTime(row, 2));
                p.requestDate(getCellValueAsLocalDateTime(row, 3));
                p.dataProvider(DataProviderType.valueOf(getCellValueAsString(row, 3)));
                prices.add(p.build());
            });
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logErrorAndExit(LOG_ERROR_ARRAY_INDEX, fileName, e.getMessage());
        } catch (Exception e) {
            Logger.logErrorAndExit(LOG_ERROR_GENERAL, fileName, e.toString());
        }

        return prices
                .stream()
                .sorted(Sorter.priceComparator())
                .toList();
    }

    /**
     * Process a Text/Markdown file.
     *
     * @param fileName path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<Price> parseMarkdownFile(String fileName) {
        return parseTextFile(fileName, markdownSeparator);
    }

    /**
     * Calculate the id of the first data row based on the title and header info.
     *
     * @param fileType file type
     * @return         the first line that contains data
     */
    @Override
    protected int getFirstDataRow(FileType fileType) {
        var skipRows = switch (fileType) {
            case MARKDOWN -> 2;
            case CSV -> 1;
            default -> 0;
        };
        if (skipRows != 0) {
            log.info("< skipping the first {} lines while reading the file...", skipRows);
        }
        return skipRows;
    }

    /**
     * Parse the Markdown and CSV file.
     *
     * @param file the input file
     * @param separator separator char used in the input file
     * @return the list of the transactions
     */
    private List<Price> parseTextFile(String file, String separator) {
        List<Price> prices = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Path.of(file))) {
            var skipRows = getFirstDataRow(com.remal.portfolio.util.Files.getFileType(file));
            stream
                    .skip(skipRows)
                    .forEach(line -> {
                        var fields = line.split(separator, -1);
                        var index = new AtomicInteger(1);
                        Price p = Price
                                .builder()
                                .symbol(getString(index, fields, Label.HEADER_SYMBOL))
                                .unitPrice(getBigDecimal(index, fields, Label.HEADER_PRICE))
                                .tradeDate(getLocalDateTime(index, fields))
                                .requestDate(getLocalDateTime(index, fields))
                                .dataProvider(getDataProvider(index, fields))
                                .build();
                        prices.add(p);
                    });
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logErrorAndExit(LOG_ERROR_ARRAY_INDEX, file, e.getMessage());
        } catch (Exception e) {
            Logger.logErrorAndExit(LOG_ERROR_GENERAL, file, e.toString());
        }

        return prices
                .stream()
                .sorted(Sorter.priceComparator())
                .toList();
    }

    /**
     * Get the value based on the missing/hidden columns.
     *
     * @param index the variable holds the index's value
     * @param fields the parsed line from the input file
     * @return the next index value
     */
    private DataProviderType getDataProvider(AtomicInteger index, String[] fields) {
        if (missingColumns.contains(Label.HEADER_CURRENCY.getId())) {
            return null;
        } else {
            return DataProviderType.getEnum(fields[index.getAndIncrement()].trim());
        }
    }
}
