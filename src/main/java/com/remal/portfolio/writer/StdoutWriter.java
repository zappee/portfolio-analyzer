package com.remal.portfolio.writer;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is used to print content to the standard output with a SLF4J
 * logger. The belonging SLF4J appender definition locates in the logback.xml file.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class StdoutWriter {

    /**
     * This is the replacement of the System.out.println() command that writes
     * the content to the standard output via SLF4J logger.
     *
     * @param content content to print
     */
    public static void write(String content) {
        log.debug(content);
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
