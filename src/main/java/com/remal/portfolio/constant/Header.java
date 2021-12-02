package com.remal.portfolio.constant;

import com.remal.portfolio.util.I18n;
import lombok.AllArgsConstructor;

import java.util.stream.Stream;

/**
 * Header translation file. The keys will be resolved from the
 * internationalization files (*.properties).
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@AllArgsConstructor
public enum Header {

    CREATED("header.created"),
    CURRENCY("header.currency"),
    FEE("header.fee"),
    ORDER_ID("header.order-id"),
    PORTFOLIO("header.portfolio"),
    PRICE("header.price"),
    TICKER("header.ticker"),
    TRADE_ID("header.trade-id"),
    TRANSFER_ID("header.transfer-id"),
    TYPE("header.type"),
    VOLUME("header.volume");

    private static final String DEFAULT_LANGUAGE = "en";
    private String i18nKey;

    /**
     * Returns with a Header stream that can be used with lambda expressions.
     *
     * @return stream of Headers
     */
    public static Stream<Header> stream() {
        return Stream.of(Header.values());
    }

    /**
     * Reads translation from the internationalization properties file.
     *
     * @param language an ISO 639 alpha-2 or alpha-3 language code
     * @return the translated value
     */
    public String getValue(String language) {
        return I18n.get(language, i18nKey);
    }

    /**
     * Reads the default translation from the internationalization
     * properties file.
     *
     * @return the translated value
     */
    public String getValue() {
        return I18n.get(DEFAULT_LANGUAGE, i18nKey);
    }
}
