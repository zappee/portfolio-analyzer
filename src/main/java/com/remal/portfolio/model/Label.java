package com.remal.portfolio.model;

import com.remal.portfolio.util.I18n;
import lombok.Builder;
import lombok.Getter;

/**
 * Constants for labels that appear in the reports.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Builder
@Getter
public class Label {

    /**
     * A column header.
     */
    public static final Label AVG_PRICE = Label.builder()
            .id("AVG_PRICE")
            .key("header.avg-price")
            .build();

    /**
     * A column header.
     */
    public static final Label CURRENCY = Label.builder()
            .id("CURRENCY")
            .key("header.currency")
            .build();

    /**
     * A column header.
     */
    public static final Label DEPOSITS = Label.builder()
            .id("DEPOSITS")
            .key("header.deposits")
            .build();

    /**
     * A column header.
     */
    public static final Label FEE = Label.builder()
            .id("FEE")
            .key("header.fee")
            .build();

    /**
     * A column header.
     */
    public static final Label MARKET_VALUE = Label.builder()
            .id("MARKET_VALUE")
            .key("header.market-value")
            .build();

    /**
     * A column header.
     */
    public static final Label NET_COST = Label.builder()
            .id("NET_COST")
            .key("header.net-cost")
            .build();

    /**
     * A column header.
     */
    public static final Label ORDER_ID = Label.builder()
            .id("ORDER_ID")
            .key("header.order-id")
            .build();

    /**
     * A column header.
     */
    public static final Label PORTFOLIO = Label.builder()
            .id("PORTFOLIO")
            .key("header.portfolio")
            .build();

    /**
     * A column header.
     */
    public static final Label PRICE = Label.builder()
            .id("PRICE")
            .key("header.price")
            .build();

    /**
     * A column header.
     */
    public static final Label QUANTITY = Label.builder()
            .id("QUANTITY")
            .key("header.quantity")
            .build();

    /**
     * A column header.
     */
    public static final Label TICKER = Label.builder()
            .id("TICKER")
            .key("header.ticker")
            .build();

    /**
     * A column header.
     */
    public static final Label TRADE_DATE = Label.builder()
            .id("TRADE_DATE")
            .key("header.trade-date")
            .build();

    /**
     * A column header.
     */
    public static final Label TRADE_ID = Label.builder()
            .id("TRADE_ID")
            .key("header.trade-id")
            .build();

    /**
     * A column header.
     */
    public static final Label TRANSFER_ID = Label.builder()
            .id("TRANSFER_ID")
            .key("header.transfer-id")
            .build();

    /**
     * A column header.
     */
    public static final Label TYPE = Label.builder()
            .id("TYPE")
            .key("header.type")
            .build();

    /**
     * A column header.
     */
    public static final Label VALUATION = Label.builder()
            .id("VALUATION")
            .key("header.valuation")
            .build();

    /**
     * A column header.
     */
    public static final Label WITHDRAWALS = Label.builder()
            .id("WITHDRAWALS")
            .key("header.withdrawals")
            .build();

    /**
     * A simple text.
     */
    public static final Label LABEL_GENERATED = Label.builder()
            .id("LABEL_GENERATED")
            .key("label.generated")
            .build();

    /**
     * A simple text.
     */
    public static final Label LABEL_PORTFOLIO_SUMMARY = Label.builder()
            .id("LABEL_PORTFOLIO_SUMMARY")
            .key("label.portfolio-summary")
            .build();

    /**
     * A simple text.
     */
    public static final Label LABEL_PORTFOLIO = Label.builder()
            .id("LABEL_PORTFOLIO")
            .key("label.portfolio")
            .build();

    /**
     * A simple text.
     */
    public static final Label LABEL_TRANSACTION_HISTORY = Label.builder()
            .id("LABEL_TRANSACTION_HISTORY")
            .key("label.transaction-history")
            .build();

    /**
     * A simple text.
     */
    public static final Label LABEL_TRANSACTION_REPORT = Label.builder()
            .id("LABEL_TRANSACTION_REPORT")
            .key("label.transaction-report")
            .build();

    /**
     * A simple text.
     */
    public static final Label LABEL_TRANSACTION = Label.builder()
            .id("LABEL_TRANSACTION")
            .key("label.transaction")
            .build();

    /**
     * The id of the label.
     */
    private String id;

    /**
     * The key that identifies the translation in the I18N *.properties file.
     */
    private String key;

    /**
     * Translation resolver that reads the belonging translation from
     * the property file.
     *
     * @param language language code, e.g. en
     * @return the translation text
     */
    public String getLabel(String language) {
        return I18n.get(language, key);
    }

    /**
     * Returns with the id of the label.
     *
     * @return label id
     */
    @Override
    public String toString() {
        return id;
    }
}
