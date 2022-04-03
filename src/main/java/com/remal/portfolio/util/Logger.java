package com.remal.portfolio.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.logging.LogManager;

/**
 * Runtime log level configuration.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class Logger {

    static {
        // disabling the JDK PlatformLogger
        LogManager.getLogManager().reset();
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
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private Logger() {
        throw new UnsupportedOperationException();
    }
}
