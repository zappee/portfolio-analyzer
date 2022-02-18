package com.remal.portfolio.writer;

import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.picocli.command.CommonCommand;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.util.Files;
import com.remal.portfolio.util.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generate a ledger as a string.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class TransactionWriter extends Writer {

    /**
     * Show the header at the top of the report.
     */
    @Setter
    private boolean showReportTitle = true;

    /**
     * The complete list of the transactions that the user has made and was filled.
     */
    private final List<Transaction> transactions;

    /**
     * Produces a report writer instance.
     *
     * @param transactions list of transactions
     * @param outputCliGroup command line interface options
     * @param replaces list of the portfolio names that will be overridden
     * @return an initialized TransactionWriter instance
     * @throws java.lang.UnsupportedOperationException in case of usage of Excel file
     */
    public static TransactionWriter build(List<Transaction> transactions,
                                          CommonCommand.OutputGroup outputCliGroup,
                                          List<String> replaces) {

        log.debug("initializing transaction report writer with '{}' language...", outputCliGroup.language);

        var writer = new TransactionWriter(transactions, replaces);
        writer.setShowReportTitle(Boolean.parseBoolean(outputCliGroup.printTitle));
        writer.setShowHeader(Boolean.parseBoolean(outputCliGroup.printHeader));
        writer.setLanguage(outputCliGroup.language);
        writer.setColumnsToHide(outputCliGroup.columnsToHide);
        writer.setDateTimePattern(outputCliGroup.dateTimePattern);
        return writer;
    }

    /**
     * Constructor.
     *
     * @param transactions the list of the transactions that the user has made and was filled
     * @param replaces list of the portfolio names that will be overridden
     */
    public TransactionWriter(List<Transaction> transactions, List<String> replaces) {
        this.transactions = transactions;
        if (replaces != null) {
            renamePortfolioNames(replaces);
        }
    }

    /**
     * Constructor.
     *
     * @param transactions the list of the transactions that the user has made and was filled
     */
    public TransactionWriter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    /**
     * Writes the report to file.
     *
     * @param writeMode controls how to open the file
     * @param file the report file
     * @throws java.lang.UnsupportedOperationException throws if unsupported file format was requested
     */
    public void writeToFile(FileWriter.WriteMode writeMode, String file) {
        var filetype = Files.getFileType(file);
        switch (filetype) {
            case TEXT:
                FileWriter.write(writeMode, file, printAsMarkdown());
                break;

            case CSV:
                FileWriter.write(writeMode, file, printAsCsv());
                break;

            default:
                log.error("Unsupported output file type: '{}'", file);
                System.exit(CommandLine.ExitCode.SOFTWARE);
        }
    }

    /**
     * Generates the transaction report.
     *
     * @return the report as a Markdown string
     */
    public String printAsMarkdown() {
        log.debug("building the Markdown transaction report with {} transactions for {}:{}...",
                transactions.size(),
                transactions.isEmpty() ? "<nothing>" : transactions.get(0).getPortfolio(),
                transactions.isEmpty() ? "<nothing>" : transactions.get(0).getTicker());

        var widths = calculateColumnWidth();
        var sb = new StringBuilder();

        if (transactions.isEmpty()) {
            return sb.toString();
        }

        // report title
        if (showReportTitle) {
            sb.append(buildMarkdownReportHeader(Label.TRANSACTIONS_TITLE.getLabel(language)));
        }

        // table header
        if (showHeader) {
            var header = new StringBuilder();
            var headerSeparator = new StringBuilder();

            LabelCollection.getTransactionTable()
                    .stream().filter(label -> !columnsToHide.contains(label.getId()))
                    .forEach(label -> {
                        var translation = label.getLabel(language);
                        header.append(tableSeparator).append(Strings.leftPad(translation, widths.get(label.getId())));
                        headerSeparator.append(tableSeparator).append("-".repeat(widths.get(label.getId())));
                    });

            header.append(tableSeparator).append(newLine);
            headerSeparator.append(tableSeparator).append(newLine);
            sb.append(header).append(headerSeparator);
        }

        // data
        transactions
                .forEach(transaction -> {
                    sb.append(getCell(Label.PORTFOLIO, transaction.getPortfolio(), widths));
                    sb.append(getCell(Label.TICKER, transaction.getTicker(), widths));
                    sb.append(getCell(Label.TYPE, transaction.getType(), widths));
                    sb.append(getCell(Label.VALUATION, transaction.getInventoryValuation(), widths));
                    sb.append(getCell(Label.TRADE_DATE, transaction.getTradeDate(), widths));
                    sb.append(getCell(Label.QUANTITY, transaction.getQuantity(), widths));
                    sb.append(getCell(Label.PRICE, transaction.getPrice(), widths));
                    sb.append(getCell(Label.FEE, transaction.getFee(), widths));
                    sb.append(getCell(Label.CURRENCY, transaction.getCurrency(), widths));
                    sb.append(getCell(Label.ORDER_ID, transaction.getOrderId(), widths));
                    sb.append(getCell(Label.TRADE_ID, transaction.getTradeId(), widths));
                    sb.append(getCell(Label.TRANSFER_ID, transaction.getTransferId(), widths));
                    sb.append(tableSeparator).append(newLine);
                });
        return sb.toString();
    }

    /**
     * Generates the transaction report.
     *
     * @return the report as a CSV content
     */
    private String printAsCsv() {
        log.debug("building the Ledger CSV report...");
        var csvSeparator = ",";
        var sb = new StringBuilder();

        // report title
        if (showReportTitle) {
            sb.append(buildCsvReportHeader());
        }

        // table header
        if (showHeader) {
            LabelCollection.getTransactionTable()
                    .stream()
                    .filter(label -> !columnsToHide.contains(label.getId()))
                    .forEach(label -> sb.append(label.getLabel(language)).append(csvSeparator));
            sb.setLength(sb.length() - 1);
            sb.append(newLine);
        }

        // data
        transactions
                .forEach(transaction -> {
                    sb.append(getCell(Label.PORTFOLIO, transaction.getPortfolio())).append(csvSeparator);
                    sb.append(getCell(Label.TICKER, transaction.getTicker())).append(csvSeparator);
                    sb.append(getCell(Label.TYPE, transaction.getType())).append(csvSeparator);
                    sb.append(getCell(Label.VALUATION, transaction.getInventoryValuation())).append(csvSeparator);
                    sb.append(getCell(Label.TRADE_DATE, transaction.getTradeDate())).append(csvSeparator);
                    sb.append(getCell(Label.QUANTITY, transaction.getQuantity())).append(csvSeparator);
                    sb.append(getCell(Label.PRICE, transaction.getPrice())).append(csvSeparator);
                    sb.append(getCell(Label.FEE, transaction.getFee())).append(csvSeparator);
                    sb.append(getCell(Label.CURRENCY, transaction.getCurrency())).append(csvSeparator);
                    sb.append(getCell(Label.ORDER_ID, transaction.getOrderId())).append(csvSeparator);
                    sb.append(getCell(Label.TRADE_ID, transaction.getTradeId())).append(csvSeparator);
                    sb.append(getCell(Label.TRANSFER_ID, transaction.getTransferId())).append(newLine);
                });
        return sb.toString();
    }

    /**
     * Initialize the list of the portfolio names that will be overridden
     * during the parse.
     *
     * @param replaces user defined values from the command line interface
     */
    private void renamePortfolioNames(List<String> replaces) {
        Map<String, String> portfolioNameToRename = new HashMap<>();
        try {
            replaces.forEach(x -> {
                var from = x.split(":")[0];
                var to = x.split(":")[1];
                log.debug("portfolio name overwriting: '{}' -> '{}'", from, to);
                portfolioNameToRename.put(from, to);
            });

            portfolioNameToRename.forEach((k, v) -> transactions
                    .stream()
                    .filter(x -> x.getPortfolio().equals(k)).forEach(x -> x.setPortfolio(v)));

        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("Invalid value provided for '-map' option.");
            System.exit(CommandLine.ExitCode.SOFTWARE);
        }
    }

    /**
     * Calculates the with of the columns that are shown in the Markdown report.
     *
     * @return length of the columns
     */
    private Map<String, Integer> calculateColumnWidth() {
        Map<String, Integer> widths = new HashMap<>();
        transactions.forEach(transaction -> {
            updateWidth(widths, Label.PORTFOLIO, transaction.getPortfolio());
            updateWidth(widths, Label.TYPE, transaction.getType());
            updateWidth(widths, Label.VALUATION, transaction.getInventoryValuation());
            updateWidth(widths, Label.TRADE_DATE, transaction.getTradeDate());
            updateWidth(widths, Label.QUANTITY, transaction.getQuantity());
            updateWidth(widths, Label.PRICE, transaction.getPrice());
            updateWidth(widths, Label.FEE, transaction.getFee());
            updateWidth(widths, Label.CURRENCY, transaction.getCurrency());
            updateWidth(widths, Label.TICKER, transaction.getTicker());
            updateWidth(widths, Label.TRANSFER_ID, transaction.getTransferId());
            updateWidth(widths, Label.TRADE_ID, transaction.getTradeId());
            updateWidth(widths, Label.ORDER_ID, transaction.getOrderId());
        });
        return widths;
    }
}
