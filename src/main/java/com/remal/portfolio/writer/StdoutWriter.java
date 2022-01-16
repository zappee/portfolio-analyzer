package com.remal.portfolio.writer;

import com.remal.portfolio.util.LogLevel;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is used to print content to the standard output with a slf4j
 * logger. The belonging appender definition is in the logback.xml file.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class StdoutWriter {

    /**
     * This is a replacement of the System.out.println() command and logs
     * content to the standard output with slf4j logger.
     *
     * @param quietMode turn on or off the logger after printing out the content
     * @param content to content to be printed to STDOUT
     */
    public static void debug(boolean quietMode, String content) {
        LogLevel.configureLogger(false);
        log.debug("");
        log.debug(content);
        LogLevel.configureLogger(quietMode);
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private StdoutWriter() {
        throw new UnsupportedOperationException();
    }
}
