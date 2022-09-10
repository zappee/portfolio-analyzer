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
 * More info: <a href="https://misc.flogisoft.com/bash/tip_colors_and_formatting">colors and formatting</a>
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@SuppressWarnings("java:S110")
public class OverwriteDefaultColors extends ForegroundCompositeConverterBase<ILoggingEvent> {

    /**
     * ANSI color definition.
     */
    private static final String LIGHT_BOLD_RED = "1;91";

    /**
     * ANSI color definition.
     */
    private static final String LIGHT_RED = "91";

    /**
     * ANSI color definition.
     */
    private static final String LIGHT_YELLOW = "93";

    /**
     * ANSI color definition.
     */
    private static final String LIGHT_GREEN = "92";

    /**
     * ANSI color definition.
     */
    private static final String LIGHT_CYAN = "96";

    /**
     * Derived classes return the foreground color specific to the derived class instance.
     *
     * @param event log event
     * @return the foreground color for this instance
     */
    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();

        return switch (level.toInt()) {
            case Level.ERROR_INT -> LIGHT_BOLD_RED;
            case Level.WARN_INT -> LIGHT_RED;
            case Level.INFO_INT -> LIGHT_GREEN;
            case Level.DEBUG_INT -> LIGHT_YELLOW;
            case Level.TRACE_INT -> LIGHT_CYAN;
            default -> ANSIConstants.DEFAULT_FG;
        };
    }
}
