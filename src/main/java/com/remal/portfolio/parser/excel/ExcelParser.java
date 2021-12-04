package com.remal.portfolio.parser.excel;

import com.remal.portfolio.model.Currency;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
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
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Parses Excel file to list of transactions.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class ExcelParser {

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
    @Getter
    @Setter
    private String dateTimePattern = "yyyy.MM.dd HH:mm:ss";

    /**
     * Timezone that is used when timestamp is rendered.
     */
    @Getter
    @Setter
    private String zoneIdAsString = "Europe/London";

    /**
     * The complete list of the transactions that the user has made
     * and was filled.
     */
    @Getter
    private final List<Transaction> transactions = new ArrayList<>();

    private final String pathToExcelFile;
    private final DataFormatter formatter = new DataFormatter();

    /**
     * Constructor.
     *
     * @param pathToExcelFile path to the Excel file
     */
    public ExcelParser(String pathToExcelFile) {
        this.pathToExcelFile = pathToExcelFile;
        formatter.addFormat("General", new java.text.DecimalFormat("#.###############"));
    }

    /**
     * Builds the transaction list.
     */
    public void parse() {
        var zoneId = ZoneId.of(zoneIdAsString);
        transactions.clear();

        try (var xlsFile = new FileInputStream(pathToExcelFile)) {
            var workbook = new XSSFWorkbook(xlsFile);
            var sheet = workbook.getSheetAt(0);
            var lastRowIndex = sheet.getLastRowNum() + 1;

            var startRow = hasHeader ? 1 : 0;
            IntStream.range(startRow, lastRowIndex).forEach(rowIndex -> {
                var row = sheet.getRow(rowIndex);
                var t = Transaction.builder().build();
                var colIndex = 0;
                t.setPortfolio(getCellValueAsString(row, colIndex));
                colIndex++;
                t.setTicker(getCellValueAsString(row, colIndex));
                colIndex++;
                t.setType(TransactionType.valueOf(getCellValueAsString(row, colIndex)));
                colIndex++;
                t.setCreated(Strings.toLocalDateTime(dateTimePattern, zoneId, getCellValueAsString(row, colIndex)));
                colIndex++;
                t.setVolume(getCellValueAsBigDecimal(row, colIndex));
                colIndex++;
                t.setPrice(getCellValueAsBigDecimal(row, colIndex));
                colIndex++;
                t.setFee(getCellValueAsBigDecimal(row, colIndex));
                colIndex++;
                t.setCurrency(Currency.getEnum(getCellValueAsString(row, colIndex)));
                colIndex++;
                t.setOrderId(getCellValueAsString(row, colIndex));
                colIndex++;
                t.setTradeId(getCellValueAsString(row, colIndex));
                colIndex++;
                t.setTransferId(getCellValueAsString(row, colIndex));
                transactions.add(t);
            });
        } catch (Exception e) {
            var message = "An unexpected error appeared while parsing the '{}' file. Error: {}";
            log.error(message, pathToExcelFile, e.getMessage());
            System.exit(CommandLine.ExitCode.SOFTWARE);
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
