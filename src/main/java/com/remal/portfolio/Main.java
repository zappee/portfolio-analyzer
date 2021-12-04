package com.remal.portfolio;

import com.remal.portfolio.parser.gdax.GdaxExportParser;
import com.remal.portfolio.constant.Header;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.util.LogLevel;
import com.remal.portfolio.writer.LedgerWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;

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
        var accountFile = "src/main/resources/adapter/gdax/account.csv";
        var fillsFile = "src/main/resources/adapter/gdax/fills.csv";
        var parser = new GdaxExportParser(accountFile, fillsFile);
        parser.parse();

        var writer = new LedgerWriter(parser.getTransactions());
        writer.setColumnsToPrint(Arrays.asList(Header.PORTFOLIO, Header.TICKER, Header.TYPE,Header.CREATED,
                Header.VOLUME, Header.PRICE, Header.FEE, Header.CURRENCY, Header.ORDER_ID, Header.TRADE_ID,
                Header.TRANSFER_ID));
        var report = writer.printAsCsv();
        FileWriter.write(
                FileWriter.WriteMode.OVERWRITE,
                "'src/main/resources/report/ledger-1.csv'",
                writer.getZoneIdAsString(), report);

        LogLevel.off();
        log.debug("done");
    }
}
