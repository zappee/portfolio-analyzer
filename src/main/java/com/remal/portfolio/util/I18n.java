package com.remal.portfolio.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This is a I18N property file reader that translates Strings to different
 * languages.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class I18n {

    /**
     * Get the translation from the internationalization properties file.
     *
     * @param language an ISO 639 alpha-2 or alpha-3 language code, e.g. en
     * @param key key in the language properties file
     * @return the translation
     */
    public static String get(String language, String key) {
        var locale = new Locale(language);
        Locale.setDefault(Locale.ENGLISH);
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
