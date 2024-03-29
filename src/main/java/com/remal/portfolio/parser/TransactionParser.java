package com.remal.portfolio.parser;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.FileType;
import com.remal.portfolio.model.InventoryValuationType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.picocli.arggroup.InputArgGroup;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.Sorter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
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
     * Builder that initializes a new writer instance.
     *
     * @param arguments input arguments
     * @return          the parser instance
     */
    public static Parser<Transaction> build(InputArgGroup arguments) {
        return build(Transaction.class, arguments, null, null);
    }

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
     * Process a Text/Markdown file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<Transaction> parseMarkdownFile(String file) {
        var skipRows = getFirstDataRow(FileType.MARKDOWN);
        var firstColumn = 1;
        List<Transaction> transactions = new ArrayList<>(parseTextFile(skipRows, firstColumn, file, markdownSeparator));
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
     * @param startColumn index from here starts to read the columns
     * @param fileName the input file
     * @param separator separator char used in the input file
     * @return the list of the transactions
     */
    private List<Transaction> parseTextFile(int skipRows, int startColumn, String fileName, String separator) {
        showConfiguration(this.getClass().getSimpleName());
        List<Transaction> transactions = new ArrayList<>();
        AtomicReference<String> currentLine = new AtomicReference<>();
        try (Stream<String> stream = Files.lines(Path.of(fileName))) {
            stream
                    .skip(skipRows)
                    .forEach(line -> {
                        currentLine.set(line);
                        if (!line.isBlank()) {
                            var fields = line.split(Pattern.quote(separator), -1);
                            var index = new AtomicInteger(startColumn);
                            Transaction t = Transaction
                                    .builder()
                                    .portfolio(getString(index, fields, Label.HEADER_PORTFOLIO))
                                    .symbol(getString(index, fields, Label.HEADER_SYMBOL))
                                    .type(getTransactionType(index, fields))
                                    .inventoryValuation(getInventoryValuationType(index, fields))
                                    .tradeDate(getLocalDateTime(index, fields))
                                    .quantity(getBigDecimal(index, fields, Label.HEADER_QUANTITY))
                                    .price(getBigDecimal(index, fields, Label.HEADER_PRICE))
                                    .priceCurrency(getCurrencyType(index, fields))
                                    .fee(getBigDecimal(index, fields, Label.HEADER_FEE))
                                    .feeCurrency(getCurrencyType(index, fields))
                                    .orderId(getString(index, fields, Label.HEADER_ORDER_ID))
                                    .tradeId(getString(index, fields, Label.HEADER_TRADE_DATE))
                                    .transferId(getString(index, fields, Label.HEADER_TRANSFER_ID))
                                    .build();
                            transactions.add(t);
                        }
                    });
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logErrorAndExit(LOG_ERROR_ARRAY_INDEX, fileName, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("An error has occurred while parsing the {} file.", fileName);
            log.error("Consider using the '-e' or '-a' options.");
            log.error("Details: {}", e.getMessage());
            Logger.logErrorAndExit("Problematic line: {}", currentLine);
        } catch (Exception e) {
            Logger.logErrorAndExit(LOG_ERROR_GENERAL, fileName, e.toString());
        }

        return transactions;
    }

    /**
     * Get the value based on the missing/hidden columns.
     *
     * @param index the variable holds the index's value
     * @param fields the parsed line from the input file
     * @return next index value
     */
    private TransactionType getTransactionType(AtomicInteger index, String[] fields) {
        if (missingColumns.contains(Label.HEADER_TYPE.name())) {
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
        if (missingColumns.contains(Label.HEADER_VALUATION.name())) {
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
        if (missingColumns.contains(Label.HEADER_PRICE_CURRENCY.name())) {
            return null;
        } else {
            var currencyAsString = fields[index.getAndIncrement()].trim();
            return currencyAsString.isEmpty() ? null : CurrencyType.getEnum(currencyAsString);
        }
    }
}
