package com.remal.portfolio.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.remal.portfolio.picocli.arggroup.CoinbaseProArgGroup;
import com.remal.portfolio.picocli.arggroup.CombineInputArgGroup;
import com.remal.portfolio.picocli.arggroup.InputArgGroup;
import com.remal.portfolio.picocli.arggroup.OutputArgGroup;
import com.remal.portfolio.picocli.arggroup.PortfolioArgGroup;
import com.remal.portfolio.picocli.arggroup.PortfolioInputArgGroup;
import com.remal.portfolio.picocli.arggroup.PriceArgGroup;
import com.remal.portfolio.picocli.arggroup.TransactionParserInputArgGroup;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.logging.LogManager;

/**
 * Runtime log level configuration.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class Logger {

    static {
        // Disabling the JDK PlatformLogger.
        LogManager.getLogManager().reset();
    }

    /**
     * Command line parameter.
     */
    private static final String SYMBOL = "symbol";

    /**
     * Command line parameter.
     */
    private static final String IN_TIMEZONE = "in-timezone";

    /**
     * Command line parameter.
     */
    private static final String IN_FROM = "in-from";

    /**
     * Command line parameter.
     */
    private static final String IN_TO = "in-to";

    /**
     * Command line parameter.
     */
    private static final String FILE_MODE = "file-mode";

    /**
     * Command line parameter.
     */
    private static final String LANGUAGE = "language";

    /**
     * Command line parameter.
     */
    private static final String DECIMAL_FORMAT = "decimal-format";

    /**
     * Command line parameter.
     */
    private static final String OUT_DATE_PATTERN = "out-date-pattern";

    /**
     * Command line parameter.
     */
    private static final String OUT_TIMEZONE = "out-timezone";

    /**
     * String format used for writing to log the CLI parameters.
     */
    private static final String LOG_TEMPLATE = "  %-20s: %s";

    /**
     * Set the silent mode.
     *
     * @param silentMode true will set the silent mode
     */
    public static void setSilentMode(boolean silentMode) {
        if (silentMode) {
            Logger.off();
        } else {
            Logger.on();
        }
    }

    /**
     * Log the message at ERROR level and terminate the program.
     *
     * @param message the error message to log
     * @param arguments a list of arguments
     */
    public static void logErrorAndExit(String message, Object... arguments) {
        log.error(message, arguments);
        System.exit(CommandLine.ExitCode.SOFTWARE);
    }

    /**
     * Log the value of a command line argument.
     *
     * @param log logger
     * @param quietMode argument to log
     */
    public static void logQuietMode(org.slf4j.Logger log, boolean quietMode) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(LOG_TEMPLATE, "quiet", quietMode));
        }
    }

    /**
     * Log the value of a command line argument.
     *
     * @param log logger
     * @param priceHistoryFile argument to log
     */
    public static void logPriceHistoryFile(org.slf4j.Logger log, String priceHistoryFile) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(LOG_TEMPLATE, "price-history", priceHistoryFile));
        }
    }

    /**
     * Log the value of the input command line arguments.
     *
     * @param log logger
     * @param inputArgGroup arguments to log
     */
    public static void logInput(org.slf4j.Logger log, PortfolioInputArgGroup inputArgGroup) {
        if (log.isDebugEnabled()) {
            logInputArgGroup(log, inputArgGroup);
            log.debug(String.format(LOG_TEMPLATE, "input-file", inputArgGroup.getFile()));
            log.debug(String.format(LOG_TEMPLATE, "data-provider-file", inputArgGroup.getDataProviderFile()));
        }
    }

    /**
     *
     * Log the value of the input command line arguments.
     *
     * @param log logger
     * @param inputArgGroup arguments to log
     */
    public static void logInput(org.slf4j.Logger log, PriceArgGroup.InputArgGroup inputArgGroup) {
        if (log.isDebugEnabled()) {
            var dataProviderArgGroup = inputArgGroup.getDataProviderArgGroup();
            log.debug(String.format(LOG_TEMPLATE, SYMBOL, inputArgGroup.getSymbol()));
            log.debug(String.format(LOG_TEMPLATE, "data-provider", dataProviderArgGroup.getDataProvider()));
            log.debug(String.format(LOG_TEMPLATE, "data-provider-file", dataProviderArgGroup.getDataProviderFile()));
            log.debug(String.format(LOG_TEMPLATE, "date", inputArgGroup.getTradeDate()));
            log.debug(String.format(LOG_TEMPLATE, "date-pattern", inputArgGroup.getDateTimePattern()));
            log.debug(String.format(LOG_TEMPLATE, IN_TIMEZONE, inputArgGroup.getZone()));
        }
    }

    /**
     *
     * Log the value of the input command line arguments.
     *
     * @param log logger
     * @param inputArgGroup arguments to log
     */
    public static void logInput(org.slf4j.Logger log, CombineInputArgGroup inputArgGroup) {
        if (log.isDebugEnabled()) {
            logInputArgGroup(log, inputArgGroup);
            log.debug(String.format(LOG_TEMPLATE, "input-files", inputArgGroup.getFiles()));
            log.debug(String.format(LOG_TEMPLATE, "overwrite", inputArgGroup.isOverwrite()));
        }
    }

    /**
     * Log the value of the input command line arguments.
     *
     * @param log logger
     * @param inputArgGroup arguments to log
     */
    public static void logInput(org.slf4j.Logger log, CoinbaseProArgGroup.InputArgGroup inputArgGroup) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(LOG_TEMPLATE, "api-access-key", inputArgGroup.getKey()));
            log.debug(String.format(LOG_TEMPLATE, "api-passphrase", inputArgGroup.getPassphrase()));
            log.debug(String.format(LOG_TEMPLATE, "api-secret", inputArgGroup.getSecret()));
            log.debug(String.format(LOG_TEMPLATE, "base-currency", inputArgGroup.getBaseCurrency()));
            log.debug(String.format(LOG_TEMPLATE, "valuation", inputArgGroup.getInventoryValuation()));
            log.debug(String.format(LOG_TEMPLATE, IN_FROM, inputArgGroup.getFrom()));
            log.debug(String.format(LOG_TEMPLATE, IN_TO, inputArgGroup.getTo()));
        }
    }

    /**
     *
     * Log the value of the input command line arguments.
     *
     * @param log logger
     * @param inputArgGroup arguments to log
     */
    public static void logInput(org.slf4j.Logger log, TransactionParserInputArgGroup inputArgGroup) {
        if (log.isDebugEnabled()) {
            logInputArgGroup(log, inputArgGroup);
            log.debug(String.format(LOG_TEMPLATE, "input-file", inputArgGroup.getFile()));
        }
    }

    /**
     * Log the value of the input command line arguments.
     *
     * @param log logger
     * @param outputArgGroup arguments to log
     */
    public static void logOutput(org.slf4j.Logger log, PortfolioArgGroup.OutputArgGroup outputArgGroup) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(LOG_TEMPLATE, "base-currency", outputArgGroup.getBaseCurrency()));
            log.debug(String.format(LOG_TEMPLATE, "portfolio-summary", outputArgGroup.getPortfolioSummaryFile()));
            log.debug(String.format(LOG_TEMPLATE, "portfolio-report", outputArgGroup.getPortfolioReportFile()));
            log.debug(String.format(LOG_TEMPLATE, FILE_MODE, outputArgGroup.getWriteMode()));
            log.debug(String.format(LOG_TEMPLATE, "multiplicity", outputArgGroup.getMultiplicity()));
            log.debug(String.format(LOG_TEMPLATE, "show-transactions", outputArgGroup.isShowTransactions()));
            log.debug(String.format(LOG_TEMPLATE, "replace", outputArgGroup.getReplaces()));
            log.debug(String.format(LOG_TEMPLATE, "hide-report-title", outputArgGroup.isHideTitle()));
            log.debug(String.format(LOG_TEMPLATE, "hide-table-header", outputArgGroup.isHideHeader()));
            log.debug(String.format(LOG_TEMPLATE, LANGUAGE, outputArgGroup.getLanguage()));
            log.debug(String.format(LOG_TEMPLATE, DECIMAL_FORMAT, outputArgGroup.getDecimalFormat()));
            log.debug(String.format(LOG_TEMPLATE, OUT_DATE_PATTERN, outputArgGroup.getDateTimePattern()));
            log.debug(String.format(LOG_TEMPLATE, OUT_TIMEZONE, outputArgGroup.getZone()));
            log.debug(String.format(LOG_TEMPLATE, "columns-to-hide", outputArgGroup.getColumnsToHide()));
        }
    }

    /**
     * Log the value of the input command line arguments.
     *
     * @param log logger
     * @param outputArgGroup arguments to log
     */
    public static void logOutput(org.slf4j.Logger log, PriceArgGroup.OutputArgGroup outputArgGroup) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(LOG_TEMPLATE, "multiplicity", outputArgGroup.getMultiplicity()));
            log.debug(String.format(LOG_TEMPLATE, FILE_MODE, outputArgGroup.getWriteMode()));
            log.debug(String.format(LOG_TEMPLATE, LANGUAGE, outputArgGroup.getLanguage()));
            log.debug(String.format(LOG_TEMPLATE, DECIMAL_FORMAT, outputArgGroup.getDecimalFormat()));
            log.debug(String.format(LOG_TEMPLATE, OUT_DATE_PATTERN, outputArgGroup.getDateTimePattern()));
            log.debug(String.format(LOG_TEMPLATE, OUT_TIMEZONE, outputArgGroup.getZone()));
        }
    }

    /**
     * Log the value of the input command line arguments.
     *
     * @param log logger
     * @param outputArgGroup arguments to log
     */
    public static void logOutput(org.slf4j.Logger log, OutputArgGroup outputArgGroup) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(LOG_TEMPLATE, "output-file", outputArgGroup.getOutputFile()));
            log.debug(String.format(LOG_TEMPLATE, FILE_MODE, outputArgGroup.getWriteMode()));
            log.debug(String.format(LOG_TEMPLATE, "replace", outputArgGroup.getReplaces()));
            log.debug(String.format(LOG_TEMPLATE, "hide-report-title", outputArgGroup.isHideTitle()));
            log.debug(String.format(LOG_TEMPLATE, "hide-table-header", outputArgGroup.isHideHeader()));
            log.debug(String.format(LOG_TEMPLATE, LANGUAGE, outputArgGroup.getLanguage()));
            log.debug(String.format(LOG_TEMPLATE, "columns-to-hide", outputArgGroup.getColumnsToHide()));
            log.debug(String.format(LOG_TEMPLATE, DECIMAL_FORMAT, outputArgGroup.getDecimalFormat()));
            log.debug(String.format(LOG_TEMPLATE, OUT_DATE_PATTERN, outputArgGroup.getDateTimePattern()));
            log.debug(String.format(LOG_TEMPLATE, OUT_TIMEZONE, outputArgGroup.getZone()));
            log.debug(String.format(LOG_TEMPLATE, "out-from", outputArgGroup.getFrom()));
            log.debug(String.format(LOG_TEMPLATE, "out-to", outputArgGroup.getTo()));
        }
    }

    /**
     * Turn on the logger.
     */
    private static void on() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("ROOT").setLevel(Level.DEBUG);
    }

    /**
     * Turn off the logger.
     */
    private static void off() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("ROOT").setLevel(Level.ERROR);
    }

    /**
     * Log the value of the input command line arguments.
     *
     * @param log logger
     * @param inputArgGroup arguments to log
     */
    private static void logInputArgGroup(org.slf4j.Logger log, InputArgGroup inputArgGroup) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(LOG_TEMPLATE, "has-report-title", inputArgGroup.hasTitle()));
            log.debug(String.format(LOG_TEMPLATE, "has-table-header", inputArgGroup.hasHeader()));
            log.debug(String.format(LOG_TEMPLATE, "portfolio", inputArgGroup.getPortfolio()));
            log.debug(String.format(LOG_TEMPLATE, SYMBOL, inputArgGroup.getSymbols()));
            log.debug(String.format(LOG_TEMPLATE, "in-date-pattern", inputArgGroup.getDateTimePattern()));
            log.debug(String.format(LOG_TEMPLATE, IN_TIMEZONE, inputArgGroup.getZone()));
            log.debug(String.format(LOG_TEMPLATE, IN_FROM, inputArgGroup.getFrom()));
            log.debug(String.format(LOG_TEMPLATE, IN_TO, inputArgGroup.getTo()));
            log.debug(String.format(LOG_TEMPLATE, "missing-columns", inputArgGroup.getMissingColumns()));
        }
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private Logger() {
        throw new UnsupportedOperationException();
    }
}
