package com.remal.portfolio.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

/**
 * Runtime log level configuration.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class LogLevel {

    /**
     * Turn on or off the logger.
     *
     * @param quietMode if it is true then the logger will be turned off
     */
    public static void configureLogger(boolean quietMode) {
        if (quietMode) {
            LogLevel.off();
        } else {
            LogLevel.on();
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
    private LogLevel() {
        throw new UnsupportedOperationException();
    }
}
