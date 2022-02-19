package com.remal.portfolio.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

/**
 * Logback provides a %highlight keyword for coloring output based on log
 * level. One problem with this is that the default color scheme is too dark
 * to read on a black/dark console. Here is a converter allowing you to
 * customize the color scheme.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@SuppressWarnings("java:S110")
public class OverwriteDefaultColors extends ForegroundCompositeConverterBase<ILoggingEvent> {

    /**
     * Derived classes return the foreground color specific to the derived class instance.
     *
     * @param event log event
     * @return the foreground color for this instance
     */
    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();

        switch (level.toInt()) {
            case Level.ERROR_INT:
                return ANSIConstants.BOLD + ANSIConstants.RED_FG;

            case Level.WARN_INT:
                return ANSIConstants.YELLOW_FG;

            case Level.INFO_INT:
                return ANSIConstants.CYAN_FG;

            default:
                return ANSIConstants.DEFAULT_FG;
        }
    }
}
