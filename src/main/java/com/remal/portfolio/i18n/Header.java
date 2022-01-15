package com.remal.portfolio.i18n;

import com.remal.portfolio.util.I18n;
import lombok.AllArgsConstructor;

import java.util.List;
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

    PORTFOLIO("header.portfolio"),
    TICKER("header.ticker"),
    TYPE("header.type"),
    CREATED("header.created"),
    VOLUME("header.volume"),
    PRICE("header.price"),
    FEE("header.fee"),
    CURRENCY("header.currency"),
    ORDER_ID("header.order-id"),
    TRADE_ID("header.trade-id"),
    TRANSFER_ID("header.transfer-id");

    /**
     * List of the supported headers.
     */
    public static final List<Header> ALL = List.of(
            PORTFOLIO, TICKER, TYPE, CREATED, VOLUME, PRICE, FEE, CURRENCY, ORDER_ID, TRADE_ID, TRANSFER_ID);

    /**
     * Default language of the reports.
     */
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
}
