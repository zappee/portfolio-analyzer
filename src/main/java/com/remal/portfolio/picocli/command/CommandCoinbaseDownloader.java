package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.parser.coinbase.CoinbaseProApiParser;
import com.remal.portfolio.util.LogLevel;
import com.remal.portfolio.writer.StdoutWriter;
import com.remal.portfolio.writer.TransactionWriter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Implementation of the 'coinbase' command.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@CommandLine.Command(
        name = "coinbase",
        sortOptions = false,
        usageHelpAutoWidth = true,
        description = "Downloads transactions from Coinbase Pro API.",
        descriptionHeading = "%n",
        optionListHeading = "%n",
        footerHeading = Main.FOOTER_HEADING,
        footer = Main.FOOTER)
@Slf4j
public class CommandCoinbaseDownloader extends CommandCommon implements Callable<Integer> {

    /**
     * An argument group definition for writing the report to file.
     */
    @CommandLine.ArgGroup(
            multiplicity = "1")
    private final DataSourceGroup dataSourceGroup = new DataSourceGroup();

    /**
     * Exclusive command line parameter.
     */
    private static class DataSourceGroup {

        /**
         * An argument group definition for Coinbase PRO API.
         */
        @CommandLine.ArgGroup(
                exclusive = false,
                multiplicity = "1",
                heading = "%nCoinbase PRO API:%n")
        final CoinbaseApiDataSourceOption coinbaseApiDataSourceOption = new CoinbaseApiDataSourceOption();
    }

    /**
     * Option list definition for Coinbase PRO API.
     */
    private static class CoinbaseApiDataSourceOption {

        /**
         * CLI definition: set Coinbase Pro API key as a string.
         */
        @CommandLine.Option(
                names = {"-k", "--api-access-key"},
                description = "Coinbase PRO API key.",
                required = true)
        String key;

        /**
         * CLI definition: set Coinbase Pro passphrase.
         */
        @CommandLine.Option(
                names = {"-p", "--api-passphrase"},
                description = "Coinbase PRO API passphrase.",
                required = true)
        String passphrase;

        /**
         * CLI definition: set Coinbase Pro secret for the API key.
         */
        @CommandLine.Option(
                names = {"-s", "--api-secret"},
                description = "Coinbase PRO API secret.",
                required = true)
        String secret;

        /**
         * CLI definition: set the ISO 4217 currency code that Coinbase
         * registered for you.
         */
        @CommandLine.Option(
                names = {"-b", "--base-currency"},
                description = "Used for determine the products you are allowed to trade."
                        + "%n  Candidates: EUR, USD, GBP, etc.")
        String baseCurrency;
    }

    /**
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        LogLevel.configureLogger(quietMode);

        var baseCurrency = Optional
                .ofNullable(dataSourceGroup.coinbaseApiDataSourceOption.baseCurrency)
                .map(String::toUpperCase)
                .orElse(null);

        Parser parser = new CoinbaseProApiParser(
                dataSourceGroup.coinbaseApiDataSourceOption.key,
                dataSourceGroup.coinbaseApiDataSourceOption.passphrase,
                dataSourceGroup.coinbaseApiDataSourceOption.secret,
                baseCurrency);
        List<Transaction> transactions = parser.parse();
        log.debug("{} transactions has been downloaded", transactions.size());

        // print to output
        var writer = TransactionWriter.build(transactions, outputGroup, replaces);
        if (outputGroup.outputFile == null) {
            StdoutWriter.debug(quietMode, writer.printAsMarkdown());
        } else {
            writer.writeToFile(outputGroup.fileWriteMode, outputGroup.outputFile);
        }

        return CommandLine.ExitCode.OK;
    }
}
