package com.remal.portfolio.model;

import com.remal.portfolio.util.I18n;
import lombok.Builder;
import lombok.Getter;

/**
 * POJO that holds information for labels appear in the reports.
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
     * A table header.
     */
    public static final Label PORTFOLIO = Label.builder()
            .id("PORTFOLIO")
            .key("header.portfolio")
            .build();

    /**
     * A table header.
     */
    public static final Label TICKER = Label.builder()
            .id("TICKER")
            .key("header.ticker")
            .build();

    /**
     * A table header.
     */
    public static final Label TYPE = Label.builder()
            .id("TYPE")
            .key("header.type")
            .build();

    /**
     * A table header.
     */
    public static final Label TRADE_DATE = Label.builder()
            .id("TRADE_DATE")
            .key("header.trade-date")
            .build();

    /**
     * A table header.
     */
    public static final Label QUANTITY = Label.builder()
            .id("QUANTITY")
            .key("header.quantity")
            .build();

    /**
     * A table header.
     */
    public static final Label PRICE = Label.builder()
            .id("PRICE")
            .key("header.price")
            .build();

    /**
     * A table header.
     */
    public static final Label AVG_PRICE = Label.builder()
            .id("AVG_PRICE")
            .key("header.avg-price")
            .build();

    /**
     * A table header.
     */
    public static final Label FEE = Label.builder()
            .id("FEE")
            .key("header.fee")
            .build();

    /**
     * A table header.
     */
    public static final Label NET_COST = Label.builder()
            .id("NET_COST")
            .key("header.net-cost")
            .build();

    /**
     * A table header.
     */
    public static final Label MARKET_VALUE = Label.builder()
            .id("MARKET_VALUE")
            .key("header.market-value")
            .build();

    /**
     * A table header.
     */
    public static final Label CURRENCY = Label.builder()
            .id("CURRENCY")
            .key("header.currency")
            .build();

    /**
     * A table header.
     */
    public static final Label ORDER_ID = Label.builder()
            .id("ORDER_ID")
            .key("header.order-id")
            .build();

    /**
     * A table header.
     */
    public static final Label TRADE_ID = Label.builder()
            .id("TRADE_ID")
            .key("header.trade-id")
            .build();

    /**
     * A table header.
     */
    public static final Label TRANSFER_ID = Label.builder()
            .id("TRANSFER_ID")
            .key("header.transfer-id")
            .build();

    /**
     * A aimple label.
     */
    public static final Label GENERATED = Label.builder()
            .id("GENERATED")
            .key("label.generated")
            .build();

    /**
     * A aimple label.
     */
    public static final Label TRANSACTION = Label.builder()
            .id("TRANSACTION")
            .key("label.transaction")
            .build();

    /**
     * A aimple label.
     */
    public static final Label TRANSACTION_HISTORY = Label.builder()
            .id("TRANSACTION_HISTORY")
            .key("label.transaction-history")
            .build();

    /**
     * A report title.
     */
    public static final Label SUMMARY_TITLE = Label.builder()
            .id("SUMMARY_TITLE")
            .key("report.summary.header.title")
            .build();

    /**
     * A report title.
     */
    public static final Label TRANSACTIONS_TITLE = Label.builder()
            .id("TRANSACTIONS_TITLE")
            .key("report.transactions.header.title")
            .build();

    /**
     * The id of the label.
     */
    private String id;

    /**
     * The key that identifies the translation in the I18N *.properties file
     * for the label.
     */
    private String key;

    /**
     * Translation resolver that reads the belonging translation from
     * the *.properties file.
     *
     * @param language language code, e.g. 'en'
     * @return the translation text
     */
    public String getLabel(String language) {
        return I18n.get(language, key);
    }

    /**
     * Retuens with the id.
     *
     * @return label id
     */
    @Override
    public String toString() {
        return id;
    }
}
