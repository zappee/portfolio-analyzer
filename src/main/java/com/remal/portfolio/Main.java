package com.remal.portfolio;

import com.remal.portfolio.picocli.command.CoinbaseDownloaderCommand;
import com.remal.portfolio.picocli.command.CombineCommand;
import com.remal.portfolio.picocli.command.PriceCommand;
import com.remal.portfolio.picocli.command.ShowCommand;
import com.remal.portfolio.picocli.provider.ManifestVersionProvider;
import com.remal.portfolio.picocli.renderer.CustomOptionRenderer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.status.StatusLogger;
import picocli.CommandLine;

/**
 * Portfolio analyzer command line tool.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@CommandLine.Command(
        subcommands = {
            CoinbaseDownloaderCommand.class,
            ShowCommand.class,
            CombineCommand.class,
            PriceCommand.class
        },
        synopsisSubcommandLabel = "[coinbase | show | combine | price]",
        name = "java -jar portfolio-analyzer.jar",
        description = "Remal Portfolio Analyzer is a command-line tool that helps you to track your portfolio "
                + "in one place and generate regular investment reports.%n",
        usageHelpAutoWidth = true,
        mixinStandardHelpOptions = true,
        versionProvider = ManifestVersionProvider.class,
        commandListHeading = "%nCommands:%n",
        exitCodeListHeading = "%nExit codes:%n",
        exitCodeList = {
            CommandLine.ExitCode.OK + ": Successful execution.",
            CommandLine.ExitCode.SOFTWARE + ": An unexpected error appeared while executing this application."},
        footer = Main.FOOTER,
        footerHeading = Main.FOOTER_HEADING
)
public final class Main {

    /**
     * Footer header of the application help.
     */
    public static final String FOOTER_HEADING = "%nPlease report issues at arnold.somogyi@gmail.com.";

    /**
     * Footer of the application help.
     */
    public static final String FOOTER = "%nDocumentation, source code: https://github.com/zappee/portfolio-analyzer%n";

    /**
     * Entry point of the application.
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        var app = new Main();
        app.configureLogger();

        CommandLine cmd = new CommandLine(app);
        cmd.setHelpFactory(new CustomOptionRenderer());
        System.exit(cmd.execute(args));
    }

    /**
     * <p>
     * Disable status-logger of log4j.
     * </p>
     * Apache POI tries to initialize log4j, but this project does not use log4j.
     * So org.apache.logging.log4j.status.StatusLogger must be disabled, otherwise
     * the following error message appears:
     * <p>
     *    StatusLogger Log4j2 could not find a logging implementation. Please add
     *    log4j-core to the classpath. Using SimpleLogger to log to the console...
     * </p>
     */
    private void configureLogger() {
        StatusLogger.getLogger().setLevel(Level.OFF);
    }
}
