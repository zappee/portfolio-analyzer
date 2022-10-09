package com.remal.portfolio.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Tool that works with java thread.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class Sleep {

    /**
     * Pause the execution for a while.
     *
     * @param millisecond the length of time to sleep in milliseconds
     */
    public static void sleep(long millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            log.warn("error while executing the 'sleep({}') command.", millisecond);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private Sleep() {
        throw new UnsupportedOperationException();
    }
}
