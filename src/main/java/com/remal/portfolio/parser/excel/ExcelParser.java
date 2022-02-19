package com.remal.portfolio.parser.excel;

import com.remal.portfolio.model.Currency;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import picocli.CommandLine;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Transform an Excel file to list of transactions.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class ExcelParser implements Parser {

    /**
     * Set it to true if the CSV file that will be parsed has a header at the first row.
     */
    @Accessors(fluent = true)
    @Setter
    @Getter
    private boolean hasHeader = true;

    /**
     * Date pattern that is used to show timestamps in the reports.
     */
    private final String dateTimePattern;

    private final DataFormatter formatter = new DataFormatter();
    private final String file;

    /**
     * Constructor.
     *
     * @param file excel file to parse
     * @param dateTimePattern pattern used for converting string to LocalDateTime
     */
    public ExcelParser(String file, String dateTimePattern) {
        this.file = file;
        this.dateTimePattern = dateTimePattern;
        formatter.addFormat("General", new java.text.DecimalFormat("#.###############"));
    }

    @Override
    public List<Transaction> parse() {
        log.debug("reading transactions from {}...", file);
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
                var currency = Currency.getEnum(getCellValueAsString(row, 7));

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
            log.debug("found {} transactions in {}.", transactions.size(), file);
        } catch (IOException e) {
            log.error("Excel file parsing error: {}", e.toString());
            System.exit(CommandLine.ExitCode.SOFTWARE);
        }

        Sorter.sort(transactions);
        return transactions;
    }

    /**
     * Calculates the ticker. When the ticker is empty then the
     * currency wil be used as a ticker. That may happen in case of
     * deposits and withdrawals.
     *
     * @param ticker abbreviation used to uniquely identify the traded shares
     * @param currency the unit of the price and fee
     * @return the ticker
     */
    private String getTicker(String ticker, Currency currency) {
        if (Objects.isNull(ticker) || ticker.isEmpty()) {
            return currency.name();
        } else {
            return ticker;
        }
    }

    /**
     * Gets cell value as a BigDecimal.
     *
     * @param row Excel row
     * @param colIndex column number in the row
     * @return the cell value as a BigDecimal or null
     */
    private BigDecimal getCellValueAsBigDecimal(XSSFRow row, int colIndex) {
        return row.getCell(colIndex) == null ? null : new BigDecimal(formatter.formatCellValue(row.getCell(colIndex)));
    }

    /**
     * Gets cell value as a String.
     *
     * @param row Excel row
     * @param colIndex column number in the row
     * @return the cell value as a String or null
     */
    private String getCellValueAsString(XSSFRow row, int colIndex) {
        return row.getCell(colIndex) == null ? null : formatter.formatCellValue(row.getCell(colIndex));
    }
}
