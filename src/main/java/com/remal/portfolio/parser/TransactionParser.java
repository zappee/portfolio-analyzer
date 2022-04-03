package com.remal.portfolio.parser;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.InventoryValuationType;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Parse files keep transactions.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class TransactionParser extends Parser<Transaction> {

    /**
     * DataFormatter contains methods for formatting the value stored
     * in an Excel cell.
     */
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    /**
     * Process a CSV file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<Transaction> parseCsvFile(String file) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            var firstDataRow = 4;
            int skipLeadingElements = hasHeader ? firstDataRow : 0;
            if (skipLeadingElements != 0) {
                log.warn("skipping the first {} lines while reading the {} file...", skipLeadingElements, file);
            }

            try (Stream<String> stream = Files.lines(Paths.get(file))) {
                stream.skip(skipLeadingElements).forEach(line -> {
                    var fields = line.split(csvSeparator, -1);
                    Transaction t = Transaction
                            .builder()
                            .portfolio(fields[0])
                            .ticker(fields[1])
                            .type(TransactionType.getEnum(fields[2]))
                            .inventoryValuation(InventoryValuationType.getEnum(fields[3].trim()))
                            .tradeDate(Strings.toLocalDateTime(dateTimePattern, fields[4]))
                            .quantity(BigDecimals.valueOf(fields[5]))
                            .price(BigDecimals.valueOf(fields[6]))
                            .fee(BigDecimals.valueOf(fields[7]))
                            .currency(CurrencyType.getEnum(fields[8]))
                            .orderId(fields[9].isBlank() ? null : fields[9])
                            .tradeId(fields[10].isBlank() ? null : fields[10])
                            .transferId(fields[11].isBlank() ? null : fields[11])
                            .build();
                    transactions.add(t);
                });
            }
        } catch (IOException e) {
            Logger.logErrorAndExit(LOG_ERROR, file, e.getMessage());
        }

        Sorter.sort(transactions);
        return transactions;
    }

    /**
     * Process an Excel file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<Transaction> parseExcelFile(String file) {
        List<Transaction> transactions = new ArrayList<>();
        try (var xlsFile = new FileInputStream(file)) {
            var workbook = new XSSFWorkbook(xlsFile);
            var sheet = workbook.getSheetAt(0);
            var lastRowIndex = sheet.getLastRowNum() + 1;
            var startRow = hasHeader ? 1 : 0;
            log.debug("size of the excel spreadsheet(0)= {}:{}", startRow, lastRowIndex);

            IntStream.range(startRow, lastRowIndex).forEach(rowIndex -> {
                var row = sheet.getRow(rowIndex);
                var t = Transaction.builder().build();
                var currency = CurrencyType.getEnum(getCellValueAsString(row, 7));

                t.setPortfolio(getCellValueAsString(row, 0));
                t.setTicker(getTicker(getCellValueAsString(row, 1),currency));
                t.setType(TransactionType.valueOf(getCellValueAsString(row, 2)));
                t.setTradeDate(Strings.toLocalDateTime(dateTimePattern, getCellValueAsString(row, 3)));
                t.setQuantity(Objects.requireNonNull(getCellValueAsBigDecimal(row, 4)).abs());
                t.setPrice(getCellValueAsBigDecimal(row, 5));
                t.setFee(getCellValueAsBigDecimal(row, 6));
                t.setCurrency(currency);
                t.setOrderId(getCellValueAsString(row, 8));
                t.setTradeId(getCellValueAsString(row, 9));
                t.setTransferId(getCellValueAsString(row, 10));
                transactions.add(t);
            });
        } catch (IOException e) {
            Logger.logErrorAndExit(LOG_ERROR, file, e.getMessage());
        }

        Sorter.sort(transactions);
        return transactions;
    }

    /**
     * Process a Text/Markdown file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<Transaction> parseMarkdownFile(String file) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            var firstDataRow = 5;
            var skipLeadingElements = hasHeader ? firstDataRow : 0;
            if (skipLeadingElements != 0) {
                log.warn("skipping the first {} lines while reading the {} file...", skipLeadingElements, file);
            }

            try (Stream<String> stream = Files.lines(Paths.get(file))) {
                stream.skip(skipLeadingElements).forEach(line -> {
                    var fields = line.split(markdownSeparator, -1);
                    Transaction t = Transaction
                            .builder()
                            .portfolio(fields[1].trim())
                            .ticker(fields[2].trim())
                            .type(TransactionType.getEnum(fields[3].trim()))
                            .inventoryValuation(InventoryValuationType.getEnum(fields[4].trim()))
                            .tradeDate(Strings.toLocalDateTime(dateTimePattern, fields[5].trim()))
                            .quantity(BigDecimals.valueOf(fields[6]))
                            .price(BigDecimals.valueOf(fields[7]))
                            .fee(BigDecimals.valueOf(fields[8]))
                            .currency(CurrencyType.getEnum(fields[9].trim()))
                            .orderId(fields[10].isBlank() ? null : fields[10].trim())
                            .tradeId(fields[11].isBlank() ? null : fields[11].trim())
                            .transferId(fields[12].isBlank() ? null : fields[12].trim())
                            .build();
                    transactions.add(t);
                });
            }
        } catch (IOException e) {
            Logger.logErrorAndExit(LOG_ERROR, file, e.getMessage());
        }

        Sorter.sort(transactions);
        return transactions;
    }

    /**
     * Get an excel cell value as a String.
     *
     * @param row row in the Excel spreadsheet
     * @param colIndex column number in the row
     * @return the cell value as a String or null in the row and column indexes are invalid
     */
    private String getCellValueAsString(XSSFRow row, int colIndex) {
        return row.getCell(colIndex) == null ? null : DATA_FORMATTER.formatCellValue(row.getCell(colIndex));
    }

    /**
     * Get an excel cell value as a BigDecimal.
     *
     * @param row row in the Excel spreadsheet
     * @param colIndex column number in the row
     * @return the cell value as a BigDecimal or null in the row and column indexes are invalid
     */
    private BigDecimal getCellValueAsBigDecimal(XSSFRow row, int colIndex) {
        return row.getCell(colIndex) == null
                ? null
                : new BigDecimal(DATA_FORMATTER.formatCellValue(row.getCell(colIndex)));
    }

    /**
     * Find the ticker.
     * When the ticker is null then the currency will be used as a ticker. That happens
     * in case of deposits and withdrawals of a company's stock.
     *
     * @param ticker abbreviation used to uniquely identify the traded shares
     * @param currency the unit of the price and fee
     * @return the ticker
     */
    private String getTicker(String ticker, CurrencyType currency) {
        if (Objects.isNull(ticker) || ticker.isEmpty()) {
            return currency.name();
        } else {
            return ticker;
        }
    }
}
