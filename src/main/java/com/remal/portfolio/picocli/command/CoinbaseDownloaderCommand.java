package com.remal.portfolio.picocli.command;

import com.remal.portfolio.Main;
import com.remal.portfolio.downloader.coinbasepro.CoinbaseProResponseParser;
import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.picocli.arggroup.CoinbaseProArgGroup;
import com.remal.portfolio.picocli.arggroup.OutputArgGroup;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.writer.TransactionWriter;
import com.remal.portfolio.writer.Writer;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Callable;

/**
 * Implementation of the 'coinbase' command.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
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
public class CoinbaseDownloaderCommand implements Callable<Integer> {

    /**
     * In the silent mode the application performs actions without
     * displaying any details.
     */
    @CommandLine.Option(names = {"-s", "--silent"},
            description = "Perform actions without displaying any details.")
    private boolean silentMode;

    /**
     * Coinbase PRO API CLI group.
     */
    @CommandLine.ArgGroup(
            exclusive = false,
            multiplicity = "1",
            heading = "%nInput (Coinbase PRO API)%n")
    private final CoinbaseProArgGroup.InputArgGroup inputArgGroup = new CoinbaseProArgGroup.InputArgGroup();

    /**
     * CLI Group definition for configuring the output.
     */
    @CommandLine.ArgGroup(
            heading = "%nOutput:%n",
            exclusive = false)
    private final OutputArgGroup outputArgGroup = new OutputArgGroup();

    /**
     * Execute the command and computes a result.
     *
     * @return exit code
     */
    @Override
    public Integer call() {
        Logger.setSilentMode(this.silentMode);

        // validating the inputs
        CurrencyType.abortIfInvalid(inputArgGroup.getBaseCurrency());
        LocalDateTimes.validate(CoinbaseProArgGroup.InputArgGroup.DATE_TIME_FILTER_PATTERN, inputArgGroup.getFrom());
        LocalDateTimes.validate(CoinbaseProArgGroup.InputArgGroup.DATE_TIME_FILTER_PATTERN, inputArgGroup.getTo());

        // input
        var parser = new CoinbaseProResponseParser(inputArgGroup);
        var transactions = parser.parse();

        // output
        var outFilenameTemplate = outputArgGroup.getOutputFile();
        var zone = ZoneId.of(outputArgGroup.getZone());
        var outFilename = LocalDateTimes.toString(zone, outFilenameTemplate, LocalDateTime.now());

        Writer<Transaction> writer = TransactionWriter.build(outputArgGroup);
        writer.write(outputArgGroup.getWriteMode(), outFilename, transactions);
        return CommandLine.ExitCode.OK;
    }
}
