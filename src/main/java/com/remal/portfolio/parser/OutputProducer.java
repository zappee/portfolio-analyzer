package com.remal.portfolio.parser;

import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.picocli.command.CommandCommon;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.util.Files;
import com.remal.portfolio.writer.StdoutWriter;
import com.remal.portfolio.writer.TransactionWriter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class generated the output based on the parameters from the command
 * line interface.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class OutputProducer {

    /**
     * Writes the list of the transactions to output.
     *
     * @param transactions list of transactions
     * @param quietMode in quiet mode the log file won't be written to the standard output
     * @param outputCliGroup command line interface options
     * @throws java.lang.UnsupportedOperationException in case of usage of Excel file
     */
    public static void writeTransactions(List<Transaction> transactions,
                                         boolean quietMode,
                                         CommandCommon.OutputGroup outputCliGroup) {
        var writer = new TransactionWriter(transactions);
        writer.setPrintHeader(Boolean.parseBoolean(outputCliGroup.printHeader));
        writer.setLanguage(outputCliGroup.language.name());
        writer.setColumnsToHide(outputCliGroup.columnsToHide);
        writer.setDateTimePattern(outputCliGroup.dateTimePattern);

        String report;
        var filetype = Files.getFileType(outputCliGroup.outputFile);
        switch (filetype) {
            case TEXT:
                report = writer.printAsMarkdown();
                FileWriter.write(outputCliGroup.fileWriteMode, outputCliGroup.outputFile, report);
                break;

            case EXCEL:
                throw new UnsupportedOperationException();

            case CSV:
                report = writer.printAsCsv();
                FileWriter.write(outputCliGroup.fileWriteMode, outputCliGroup.outputFile, report);
                break;

            case NOT_DEFINED:
                report = writer.printAsMarkdown();
                StdoutWriter.debug(report, quietMode);
                break;

            default:
                log.error("Unsupported output file type: '{}'", outputCliGroup.outputFile);
                System.exit(CommandLine.ExitCode.SOFTWARE);
        }
    }

    /**
     * Initialize the list of the portfolio names that will be overridden
     * during the parse.
     *
     * @param replaces user defined values from the command line interface
     * @param transactions list of transactions
     */
    public static void renamePortfolioNames(List<String> replaces, List<Transaction> transactions) {
        Map<String, String> portfolioNameToRename = new HashMap<>();
        try {
            replaces.forEach(x -> {
                var from = x.split(":")[0];
                var to = x.split(":")[1];
                log.debug("initializing portfolio name overwriting: '{}' -> '{}'", from, to);
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
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private OutputProducer() {
        throw new UnsupportedOperationException();
    }

}
