package com.remal.portfolio.parser;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.FileType;
import com.remal.portfolio.model.InventoryValuationType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.util.Filter;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Parse files that keep transactions.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class TransactionParser extends Parser<Transaction> {

    /**
     * Process a CSV file.
     *
     * @param fileName path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<Transaction> parseCsvFile(String fileName) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            var skipRows = getFirstDataRow(FileType.CSV);
            var firstColumn = 0;
            transactions.addAll(parseTextFile(skipRows, firstColumn, fileName, csvSeparator));

        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logErrorAndExit(LOG_ERROR_ARRAY_INDEX, fileName, e.getMessage());
        } catch (Exception e) {
            Logger.logErrorAndExit(LOG_ERROR_GENERAL, fileName, e.toString());
        }

        return transactions
                .stream()
                .filter(t -> Filter.dateEqualOrAfterFilter(t.getTradeDate(), from))
                .filter(t -> Filter.dateEqualOrBeforeFilter(t.getTradeDate(), to))
                .sorted(Sorter.tradeDateComparator())
                .toList();
    }

    /**
     * Process an Excel file.
     *
     * @param fileName path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<Transaction> parseExcelFile(String fileName) {
        List<Transaction> transactions = new ArrayList<>();
        try (var xlsFile = new FileInputStream(fileName)) {
            var firstRow = getFirstDataRow(FileType.EXCEL);
            var workbook = new XSSFWorkbook(xlsFile);
            var sheet = workbook.getSheetAt(0);
            var lastRow = sheet.getLastRowNum() + 1;
            log.debug("selecting rows from {} to {} in the excel spreadsheet", firstRow, lastRow);

            IntStream.range(firstRow, lastRow).forEach(rowIndex -> {
                var row = sheet.getRow(rowIndex);
                var t = Transaction.builder();
                var currency = CurrencyType.getEnum(getCellValueAsString(row, 8));

                t.portfolio(getCellValueAsString(row, 0));
                t.symbol(getSymbol(getCellValueAsString(row, 1), currency));
                t.type(TransactionType.valueOf(getCellValueAsString(row, 2)));
                t.inventoryValuation(InventoryValuationType.getEnum(getCellValueAsString(row, 3)));
                t.tradeDate(getCellValueAsLocalDateTime(row, 4));
                t.quantity(Objects.requireNonNull(getCellValueAsBigDecimal(row, 5)).abs());
                t.price(getCellValueAsBigDecimal(row, 6));
                t.fee(getCellValueAsBigDecimal(row, 7));
                t.currency(currency);
                t.orderId(getCellValueAsString(row, 9));
                t.tradeId(getCellValueAsString(row, 10));
                t.transferId(getCellValueAsString(row, 11));
                transactions.add(t.build());
            });
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logErrorAndExit(LOG_ERROR_ARRAY_INDEX, fileName, e.getMessage());
        } catch (Exception e) {
            Logger.logErrorAndExit(LOG_ERROR_GENERAL, fileName, e.toString());
        }

        return transactions
                .stream()
                .filter(t -> Filter.dateEqualOrAfterFilter(t.getTradeDate(), from))
                .filter(t -> Filter.dateEqualOrBeforeFilter(t.getTradeDate(), to))
                .sorted(Sorter.tradeDateComparator())
                .toList();
    }

    /**
     * Process a Text/Markdown file.
     *
     * @param fileName path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<Transaction> parseMarkdownFile(String fileName) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            var skipRows = getFirstDataRow(FileType.MARKDOWN);
            var firstColumn = 1;
            transactions.addAll(parseTextFile(skipRows, firstColumn, fileName, markdownSeparator));

        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logErrorAndExit(LOG_ERROR_ARRAY_INDEX, fileName, e.getMessage());
        } catch (IllegalArgumentException e) {
            Logger.logErrorAndExit("An error occurs when trying to parse the {} file. "
                    + "Consider using the combination of '--has-title' or '--has-header' options. Details: {}",
                    fileName,
                    e.getMessage());
        } catch (Exception e) {
            Logger.logErrorAndExit(LOG_ERROR_GENERAL, fileName, e.toString());
        }

        return transactions
                .stream()
                .filter(t -> Filter.dateEqualOrAfterFilter(t.getTradeDate(), from))
                .filter(t -> Filter.dateEqualOrBeforeFilter(t.getTradeDate(), to))
                .sorted(Sorter.tradeDateComparator())
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
    private List<Transaction> parseTextFile(int skipRows, int firstColumn, String file, String separator)
            throws IOException {

        List<Transaction> transactions = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Path.of(file))) {
            stream
                    .skip(skipRows)
                    .forEach(line -> {
                        var fields = line.split(separator, -1);
                        var index = new AtomicInteger(firstColumn);
                        Transaction t = Transaction
                                .builder()
                                .portfolio(getString(index, fields, Label.HEADER_PORTFOLIO))
                                .symbol(getString(index, fields, Label.HEADER_SYMBOL))
                                .type(getTransactionType(index, fields))
                                .inventoryValuation(getInventoryValuationType(index, fields))
                                .tradeDate(getLocalDateTime(index, fields))
                                .quantity(getBigDecimal(index, fields, Label.HEADER_QUANTITY))
                                .price(getBigDecimal(index, fields, Label.HEADER_PRICE))
                                .fee(getBigDecimal(index, fields, Label.HEADER_FEE))
                                .currency(getCurrencyType(index, fields))
                                .orderId(getString(index, fields, Label.HEADER_ORDER_ID))
                                .tradeId(getString(index, fields, Label.HEADER_TRADE_DATE))
                                .transferId(getString(index, fields, Label.HEADER_TRANSFER_ID))
                                .build();
                        transactions.add(t);
                    });
        }
        return transactions;
    }

    /**
     * Find the symbol.
     * When the symbol is null then the currency will be used as a symbol. That happens
     * in case of deposits and withdrawals of a company's stock.
     *
     * @param symbol abbreviation used to uniquely identify the traded shares
     * @param currency the unit of the price and fee
     * @return the symbol
     */
    private String getSymbol(String symbol, CurrencyType currency) {
        if (Objects.isNull(symbol) || symbol.isEmpty()) {
            return currency.name();
        } else {
            return symbol;
        }
    }

    /**
     * Get the value based on the missing/hidden columns.
     *
     * @param index the variable holds the index's value
     * @param fields the parsed line from the input file
     * @return next index value
     */
    private TransactionType getTransactionType(AtomicInteger index, String[] fields) {
        if (missingColumns.contains(Label.HEADER_TYPE.getId())) {
            return null;
        } else {
            return TransactionType.getEnum(fields[index.getAndIncrement()].trim());
        }
    }

    /**
     * Get the value based on the missing/hidden columns.
     *
     * @param index the variable holds the index's value
     * @param fields the parsed line from the input file
     * @return next index value
     */
    private InventoryValuationType getInventoryValuationType(AtomicInteger index, String[] fields) {
        if (missingColumns.contains(Label.HEADER_VALUATION.getId())) {
            return null;
        } else {
            return InventoryValuationType.getEnum(fields[index.getAndIncrement()].trim());
        }
    }

    /**
     * Get the value based on the missing/hidden columns.
     *
     * @param index the variable holds the index's value
     * @param fields the parsed line from the input file
     * @return next index value
     */
    private CurrencyType getCurrencyType(AtomicInteger index, String[] fields) {
        if (missingColumns.contains(Label.HEADER_CURRENCY.getId())) {
            return null;
        } else {
            return CurrencyType.getEnum(fields[index.getAndIncrement()].trim());
        }
    }
}
