package com.remal.portfolio.parser;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.FileType;
import com.remal.portfolio.model.InventoryValuationType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.Sorter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Parse files keep transactions.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
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
     * @param fileName path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<Transaction> parseCsvFile(String fileName) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            var skipRows = getFirstDataRow(FileType.CSV);
            var startIndex = 0;
            transactions.addAll(parseTextFile(skipRows, startIndex, fileName, csvSeparator));

        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logErrorAndExit(LOG_ERROR_ARRAY_INDEX, fileName, e.getMessage());
        } catch (Exception e) {
            Logger.logErrorAndExit(LOG_ERROR_GENERAL, fileName, e.toString());
        }

        return transactions
                .stream()
                .filter(t -> Filter.dateEqualOrAfterFilter(from, t))
                .filter(t -> Filter.dateEqualOrBeforeFilter(to, t))
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
            var firstDataRows = getFirstDataRow(FileType.EXCEL);
            var workbook = new XSSFWorkbook(xlsFile);
            var sheet = workbook.getSheetAt(0);
            var lastRowIndex = sheet.getLastRowNum() + 1;
            log.debug("size of the excel spreadsheet(0)= {}:{}", firstDataRows, lastRowIndex);

            IntStream.range(firstDataRows, lastRowIndex).forEach(rowIndex -> {
                var row = sheet.getRow(rowIndex);
                var t = Transaction.builder().build();
                var currency = CurrencyType.getEnum(getCellValueAsString(row, 8));

                t.setPortfolio(getCellValueAsString(row, 0));
                t.setTicker(getTicker(getCellValueAsString(row, 1),currency));
                t.setType(TransactionType.valueOf(getCellValueAsString(row, 2)));
                t.setInventoryValuation(InventoryValuationType.getEnum(getCellValueAsString(row, 3)));
                t.setTradeDate(getCellValueAsLocalDateTime(row));
                t.setQuantity(Objects.requireNonNull(getCellValueAsBigDecimal(row, 5)).abs());
                t.setPrice(getCellValueAsBigDecimal(row, 6));
                t.setFee(getCellValueAsBigDecimal(row, 7));
                t.setCurrency(currency);
                t.setOrderId(getCellValueAsString(row, 9));
                t.setTradeId(getCellValueAsString(row, 10));
                t.setTransferId(getCellValueAsString(row, 11));
                transactions.add(t);
            });
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logErrorAndExit(LOG_ERROR_ARRAY_INDEX, fileName, e.getMessage());
        } catch (Exception e) {
            Logger.logErrorAndExit(LOG_ERROR_GENERAL, fileName, e.toString());
        }

        return transactions
                .stream()
                .filter(t -> Filter.dateEqualOrAfterFilter(from, t))
                .filter(t -> Filter.dateEqualOrBeforeFilter(to, t))
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
            var startIndex = 1;
            transactions.addAll(parseTextFile(skipRows, startIndex, fileName, markdownSeparator));

        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logErrorAndExit(LOG_ERROR_ARRAY_INDEX, fileName, e.getMessage());
        } catch (IllegalArgumentException e) {
            Logger.logErrorAndExit("An error occurs when trying to parse the {} file. "
                    + "For better result, consider using the combination of '--has-title' or '--has-header' options.",
                    fileName);
        } catch (Exception e) {
            Logger.logErrorAndExit(LOG_ERROR_GENERAL, fileName, e.toString());
        }

        return transactions
                .stream()
                .filter(t -> Filter.dateEqualOrAfterFilter(from, t))
                .filter(t -> Filter.dateEqualOrBeforeFilter(to, t))
                .sorted(Sorter.tradeDateComparator())
                .toList();
    }

    /**
     * Parse the Markdown and CSV file.
     *
     * @param skipRows number of the lines that must be skip while parsing the file
     * @param start index from here starts to read the columns
     * @param file the input file
     * @param separator separator char used in the input file
     * @return the list of the transactions
     * @throws IOException in case of file not found
     */
    private List<Transaction> parseTextFile(int skipRows, int start, String file, String separator) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Path.of(file))) {
            stream
                    .skip(skipRows)
                    .forEach(line -> {
                        var fields = line.split(separator, -1);
                        var index = new AtomicInteger(start);
                        Transaction t = Transaction
                                .builder()
                                .portfolio(getString(index, fields, Label.PORTFOLIO))
                                .ticker(getString(index, fields, Label.TICKER))
                                .type(getTransactionType(index, fields))
                                .inventoryValuation(getInventoryValuationType(index, fields))
                                .tradeDate(getLocalDateTime(index, fields))
                                .quantity(getBigDecimal(index, fields, Label.QUANTITY))
                                .price(getBigDecimal(index, fields, Label.PRICE))
                                .fee(getBigDecimal(index, fields, Label.FEE))
                                .currency(getCurrencyType(index, fields))
                                .orderId(getString(index, fields, Label.ORDER_ID))
                                .tradeId(getString(index, fields, Label.TRADE_DATE))
                                .transferId(getString(index, fields, Label.TRANSFER_ID))
                                .build();
                        transactions.add(t);
                    });
        }
        return transactions;
    }

    /**
     * Calculate the id of the first data row based on the title and header info.
     *
     * @param fileType file type
     * @return the first line that contains data
     */
    private int getFirstDataRow(FileType fileType) {
        var titleRows = 2;
        var headerRows = 1;
        var skipRows = hasTitle ? titleRows : 0;
        skipRows += hasHeader ? headerRows : 0;
        skipRows += (fileType == FileType.MARKDOWN && hasHeader) ? 1 : 0;

        if (skipRows != 0) {
            log.debug("input < skipping the first {} lines while reading the file...", skipRows);
        }
        return skipRows;
    }

    /**
     * Get an excel cell value as a String.
     *
     * @param row row in the Excel spreadsheet
     * @param colIndex column index within the row
     * @return the cell value as a String or null in the row and column indexes are invalid
     */
    private String getCellValueAsString(XSSFRow row, int colIndex) {
        return Objects.isNull(row.getCell(colIndex)) ? null : DATA_FORMATTER.formatCellValue(row.getCell(colIndex));
    }

    /**
     * Get an excel cell value as a String.
     *
     * @param row row in the Excel spreadsheet
     * @return the cell value as a String or null in the row and column indexes are invalid
     */
    private LocalDateTime getCellValueAsLocalDateTime(XSSFRow row) {
        var colIndex = 4;
        if (Objects.isNull(row.getCell(colIndex))) {
            return null;
        } else {
            return row.getCell(colIndex).getLocalDateTimeCellValue().atZone(zone).toLocalDateTime();
        }
    }

    /**
     * Get an excel cell value as a BigDecimal.
     *
     * @param row row in the Excel spreadsheet
     * @param colIndex column index within the row
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

    /**
     * Get the value based on the missing/hidden columns.
     *
     * @param index the variable holds the index's value
     * @param fields the parsed line from the input file
     * @param actualColumn column ID
     * @return the next index value
     */
    private String getString(AtomicInteger index, String[] fields, Label actualColumn) {
        if (missingColumns.contains(actualColumn.getId())) {
            return null;
        } else {
            var value = fields[index.getAndIncrement()];
            return value.isBlank() ? null : value;
        }
    }

    /**
     * Get the value based on the missing/hidden columns.
     *
     * @param index the variable holds the index's value
     * @param fields the parsed line from the input file
     * @return the next index value
     */
    private TransactionType getTransactionType(AtomicInteger index, String[] fields) {
        if (missingColumns.contains(Label.TYPE.getId())) {
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
     * @return the next index value
     */
    private InventoryValuationType getInventoryValuationType(AtomicInteger index, String[] fields) {
        if (missingColumns.contains(Label.VALUATION.getId())) {
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
     * @return the next index value
     */
    private LocalDateTime getLocalDateTime(AtomicInteger index, String[] fields) {
        if (missingColumns.contains(Label.TRADE_DATE.getId())) {
            return null;
        } else {
            return LocalDateTimes.toLocalDateTime(zone, dateTimePattern, fields[index.getAndIncrement()].trim());
        }
    }

    /**
     * Get the value based on the missing/hidden columns.
     *
     * @param index the variable holds the index's value
     * @param fields the parsed line from the input file
     * @param actualColumn column ID
     * @return the next index value
     */
    private BigDecimal getBigDecimal(AtomicInteger index, String[] fields, Label actualColumn) {
        if (missingColumns.contains(actualColumn.getId())) {
            return null;
        } else {
            return BigDecimals.valueOf(fields[index.getAndIncrement()].trim());
        }
    }

    /**
     * Get the value based on the missing/hidden columns.
     *
     * @param index the variable holds the index's value
     * @param fields the parsed line from the input file
     * @return the next index value
     */
    private CurrencyType getCurrencyType(AtomicInteger index, String[] fields) {
        if (missingColumns.contains(Label.CURRENCY.getId())) {
            return null;
        } else {
            return CurrencyType.getEnum(fields[index.getAndIncrement()].trim());
        }
    }
}
