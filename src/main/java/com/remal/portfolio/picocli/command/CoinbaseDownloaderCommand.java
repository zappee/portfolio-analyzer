package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.downloader.coinbasepro.CoinbaseProResponseParser;
import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.InventoryValuationType;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.PortfolioNameRenamer;
import com.remal.portfolio.validator.ZoneIdValidator;
import com.remal.portfolio.writer.TransactionWriter;
import com.remal.portfolio.writer.Writer;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
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
        description = "Download your personal transactions from Coinbase.",
        descriptionHeading = "%n",
        optionListHeading = "%n",
        footerHeading = Main.FOOTER_HEADING,
        footer = Main.FOOTER)
@Slf4j
public class CoinbaseDownloaderCommand extends OutputCommandGroup implements Callable<Integer> {

    /**
     * In the silent mode the application performs actions without
     * displaying any details.
     */
    @CommandLine.Option(names = {"-s", "--silent"},
            description = "Perform actions without displaying any details.")
    boolean silentMode;

    /**
     * Set the date/time format that is used in the reports.
     */
    @CommandLine.Option(
            names = {"-p", "--date-pattern"},
            description = "Dates in the report are shown according to the pattern. Default: \"${DEFAULT-VALUE}\"",
            defaultValue = "yyyy-MM-dd HH:mm:ss")
    String datePattern;

    /**
     * Set the timezone that is used to convert dates to user's timezone.
     */
    @CommandLine.Option(
            names = {"-z", "--timezone-to"},
            description = "Convert the dates to your time zone. Supported zone types: "
                    + "Fixed offset and Geographical region, e.g. \"GMT+2\" or \"Europe/Budapest\"")
    String zoneTo;

    /**
     * The list of the portfolio names that will replace to another value
     * during the parse.
     */
    @CommandLine.Option(
            names = {"-r", "--replace"},
            description = "Replace the portfolio name. Format: \"from:to, from:to\", e.g. \"default:coinbase\".",
            split = ",")
    final List<String> replaces = new ArrayList<>();

    /**
     * Set the inventory valuation type.
     */
    @CommandLine.Option(
            names = {"-v", "--valuation"},
            description = "Default inventory valuation type. Candidates: ${COMPLETION-CANDIDATES}"
                    + "Default: \"${DEFAULT-VALUE}\"",
            defaultValue = "FIFO")
    InventoryValuationType inventoryValuation;

    /**
     * CLI Group definition for Coinbase API.
     */
    @CommandLine.ArgGroup(
            multiplicity = "1")
    private final CoinbaseArgGroup coinbaseArgGroup = new CoinbaseArgGroup();

    /**
     * Coinbase API CLI parameters.
     */
    private static class CoinbaseArgGroup {

        /**
         * ACoinbase PRO API CLI group.
         */
        @CommandLine.ArgGroup(
                exclusive = false,
                multiplicity = "1",
                heading = "%nCoinbase PRO API:%n")
        final CoinbaseApiArgGroup coinbaseApiArgGroup = new CoinbaseApiArgGroup();
    }

    /**
     * Coinbase API CLI parameters.
     */
    private static class CoinbaseApiArgGroup {

        /**
         * Set the Coinbase API key.
         */
        @CommandLine.Option(
                names = {"-k", "--api-access-key"},
                description = "Coinbase PRO API key.",
                required = true)
        String key;

        /**
         * Set Coinbase Pro passphrase.
         */
        @CommandLine.Option(
                names = {"-e", "--api-passphrase"},
                description = "Coinbase PRO API passphrase.",
                required = true)
        String passphrase;

        /**
         * Set Coinbase Pro secret for the API key.
         */
        @CommandLine.Option(
                names = {"-t", "--api-secret"},
                description = "Coinbase PRO API secret.",
                required = true)
        String secret;

        /**
         * Set the ISO 4217 currency code that Coinbase
         * registered for you.
         */
        @CommandLine.Option(
                names = {"-c", "--base-currency"},
                description = "The currency of your Coinbase account you are allowed to trade, e.g. \"EUR\", etc.")
        String baseCurrency;
    }

    /**
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        Logger.setSilentMode(silentMode);

        CurrencyType.validate(coinbaseArgGroup.coinbaseApiArgGroup.baseCurrency);
        ZoneIdValidator.validate(zoneTo);

        var parser = new CoinbaseProResponseParser(
                coinbaseArgGroup.coinbaseApiArgGroup.key,
                coinbaseArgGroup.coinbaseApiArgGroup.passphrase,
                coinbaseArgGroup.coinbaseApiArgGroup.secret,
                CurrencyType.getEnum(coinbaseArgGroup.coinbaseApiArgGroup.baseCurrency),
                inventoryValuation,
                zoneTo);

        var transactions = parser.parse();
        PortfolioNameRenamer.rename(transactions, replaces);

        Writer<Transaction> writer = TransactionWriter.build(datePattern, zoneTo, outputArgGroup);
        writer.write(outputArgGroup.writeMode, outputArgGroup.outputFile, transactions);

        return CommandLine.ExitCode.OK;
    }
}
