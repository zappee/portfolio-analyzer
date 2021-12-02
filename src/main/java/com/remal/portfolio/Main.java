package com.remal.portfolio;

import com.remal.portfolio.adapter.gdax.GdaxExportParser;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.util.LogLevel;
import com.remal.portfolio.writer.LedgerWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Entry point of the command-line-interface.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class Main {

    /**
     * Starts the application.
     *
     * @param args command line arguments
     * @throws java.io.IOException throws in case of error
     */
    public static void main(String[] args) throws IOException {
        var accountFile = "src/main/resources/gdax/account.csv";
        var fillsFile = "src/main/resources/gdax/fills.csv";

        var parser = new GdaxExportParser(accountFile, fillsFile);
        parser.parse();

        // path format for wri
        var writer = new LedgerWriter(parser.getTransactions());
        var report = writer.printAsMarkdown();
        FileWriter.write(
                FileWriter.WriteMode.OVERWRITE,
                "'src/main/resources/generated/ledger.md'",
                writer.getZoneIdAsString(), report);

        LogLevel.off();
        log.debug("done");
    }
}
