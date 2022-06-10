package com.remal.portfolio.model;

import com.remal.portfolio.util.I18n;
import lombok.Builder;
import lombok.Getter;

/**
 * Constants for labels that appear in the reports.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
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
    public static final Label COST_TOTAL = Label.builder()
            .id("COST_TOTAL")
            .key("header.cost-total")
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
    public static final Label DATE = Label.builder()
            .id("DATE")
            .key("header.date")
            .build();

    /**
     * A column header.
     */
    public static final Label DATA_PROVIDER = Label.builder()
            .id("DATA_PROVIDER")
            .key("header.data-provider")
            .build();

    /**
     * A column header.
     */
    public static final Label DEPOSIT_TOTAL = Label.builder()
            .id("DEPOSIT_TOTAL")
            .key("header.deposit-total")
            .build();

    /**
     * A column header.
     */
    public static final Label INVESTED_AMOUNT = Label.builder()
            .id("INVESTED_AMOUNT")
            .key("header.invested-amount")
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
    public static final Label MARKET_UNIT_PRICE = Label.builder()
            .id("MARKET_UNIT_PRICE")
            .key("header.market-unit-price")
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
    public static final Label PROFIT_LOSS = Label.builder()
            .id("PROFIT_LOSS")
            .key("header.profit-loss")
            .build();

    /**
     * A column header.
     */
    public static final Label PROFIT_LOSS_PERCENT = Label.builder()
            .id("PROFIT_LOSS_PERCENT")
            .key("header.profit-loss-percent")
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
    public static final Label WITHDRAWAL_TOTAL = Label.builder()
            .id("WITHDRAWAL_TOTAL")
            .key("header.withdrawal-total")
            .build();

    /**
     * A simple text.
     */
    public static final Label LABEL_PRICE_HISTORY = Label.builder()
            .id("LABEL_PRICE_HISTORY")
            .key("label.price-history")
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
     * A report title.
     */
    public static final Label TITLE_SUMMARY_REPORT = Label.builder()
            .id("TITLE_SUMMARY_REPORT")
            .key("title.summary-report")
            .build();

    /**
     * A report title.
     */
    public static final Label TITLE_TRANSACTIONS_REPORT = Label.builder()
            .id("TITLE_TRANSACTIONS_REPORT")
            .key("title.transactions-report")
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
