package com.remal.portfolio;

import com.remal.portfolio.adapter.gdax.GdaxExportParser;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.writer.LedgerWriter;

import java.io.IOException;

/**
 * Entry point of the command-line-interface.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class Main {

    /**
     * Starts the application.
     *
     * @param args command line arguments
     * @throws java.io.IOException throws in case of error
     */
    public static void main(String[] args) throws IOException {
        var accountFile = "path/to/account.csv";
        var fillsFile = "path/tp/fills.csv";

        var parser = new GdaxExportParser(accountFile, fillsFile);
        parser.parse();

        var pathToFile = "path/to/ledger.md";
        var writer = new LedgerWriter(parser.getTransactions());
        System.out.println(writer.printAsMarkdown());
        writer.writeToFile(FileWriter.WriteMode.OVERWRITE, pathToFile);
    }
}
