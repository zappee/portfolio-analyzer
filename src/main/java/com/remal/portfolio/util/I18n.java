package com.remal.portfolio.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This is a property file reader that reads human-readable labels used in
 * the reports.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class I18n {

    /**
     * Reads translation from the internationalization properties file.
     *
     * @param language an ISO 639 alpha-2 or alpha-3 language code
     * @param key key in the i18n properties file
     * @return the translation
     */
    public static String get(String language, String key) {
        var locale = new Locale(language);
        var messages = ResourceBundle.getBundle("messages", locale);
        return messages.getString(key);
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private I18n() {
        throw new UnsupportedOperationException();
    }
}
