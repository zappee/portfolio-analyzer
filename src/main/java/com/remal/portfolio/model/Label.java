package com.remal.portfolio.model;

import com.remal.portfolio.util.I18n;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

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
    public static final Label HEADER_AVG_PRICE = Label.builder()
            .id("HEADER_AVG_PRICE")
            .key("header.avg-price")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_COSTS = Label.builder()
            .id("HEADER_COSTS")
            .key("header.costs")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_CURRENCY = Label.builder()
            .id("HEADER_CURRENCY")
            .key("header.currency")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_DATA_PROVIDER = Label.builder()
            .id("HEADER_DATA_PROVIDER")
            .key("header.data-provider")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_DEPOSIT_TOTAL = Label.builder()
            .id("HEADER_DEPOSIT_TOTAL")
            .key("header.deposit-total")
            .build();

    /**
     * An empty label that is used to format BigDecimal number without having a label.
     */
    public static final Label HEADER_EMPTY = Label.builder()
            .id("HEADER_EMPTY")
            .key("label.empty")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_FEE = Label.builder()
            .id("HEADER_FEE")
            .key("header.fee")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_INVESTED_AMOUNT = Label.builder()
            .id("HEADER_INVESTED_AMOUNT")
            .key("header.invested-amount")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_MARKET_UNIT_PRICE = Label.builder()
            .id("HEADER_MARKET_UNIT_PRICE")
            .key("header.market-unit-price")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_MARKET_VALUE = Label.builder()
            .id("HEADER_MARKET_VALUE")
            .key("header.market-value")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_ORDER_ID = Label.builder()
            .id("HEADER_ORDER_ID")
            .key("header.order-id")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_PORTFOLIO = Label.builder()
            .id("HEADER_PORTFOLIO")
            .key("header.portfolio")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_PRICE = Label.builder()
            .id("HEADER_PRICE")
            .key("header.price")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_PROFIT_LOSS = Label.builder()
            .id("HEADER_PROFIT_LOSS")
            .key("header.profit-loss")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_PROFIT_LOSS_PERCENT = Label.builder()
            .id("HEADER_PROFIT_LOSS_PERCENT")
            .key("header.profit-loss-percent")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_QUANTITY = Label.builder()
            .id("HEADER_QUANTITY")
            .key("header.quantity")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_REQUEST_DATE = Label.builder()
            .id("HEADER_REQUEST_DATE")
            .key("header.request-date")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_SYMBOL = Label.builder()
            .id("HEADER_SYMBOL")
            .key("header.symbol")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_TRADE_DATE = Label.builder()
            .id("HEADER_TRADE_DATE")
            .key("header.trade-date")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_TRADE_ID = Label.builder()
            .id("HEADER_TRADE_ID")
            .key("header.trade-id")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_TRANSFER_ID = Label.builder()
            .id("HEADER_TRANSFER_ID")
            .key("header.transfer-id")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_TYPE = Label.builder()
            .id("HEADER_TYPE")
            .key("header.type")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_VALUATION = Label.builder()
            .id("HEADER_VALUATION")
            .key("header.valuation")
            .build();

    /**
     * A column header.
     */
    public static final Label HEADER_WITHDRAWAL_TOTAL = Label.builder()
            .id("HEADER_WITHDRAWAL_TOTAL")
            .key("header.withdrawal-total")
            .build();

    /**
     * A simple text.
     */
    public static final Label LABEL_CASH = Label.builder()
            .id("LABEL_CASH")
            .key("label.total.cash")
            .build();

    /**
     * A simple text.
     */
    public static final Label LABEL_DEPOSIT = Label.builder()
            .id("LABEL_DEPOSIT")
            .key("label.total.deposit")
            .build();

    /**
     * A simple text.
     */
    public static final Label LABEL_INVESTMENT = Label.builder()
            .id("LABEL_INVESTMENT")
            .key("label.total.investment")
            .build();

    /**
     * A simple text.
     */
    public static final Label LABEL_MARKET_VALUE = Label.builder()
            .id("LABEL_MARKET_VALUE")
            .key("label.total.market-value")
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
    public static final Label LABEL_PORTFOLIO_SUMMARY = Label.builder()
            .id("LABEL_PORTFOLIO_SUMMARY")
            .key("label.portfolio-summary")
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
    public static final Label LABEL_PROFIT_LOSS = Label.builder()
            .id("LABEL_PROFIT_LOSS")
            .key("label.total.profit-loss")
            .build();

    /**
     * A simple text.
     */
    public static final Label LABEL_TOTAL_EQUITY = Label.builder()
            .id("LABEL_TOTAL_EQUITY")
            .key("label.total.total-equity")
            .build();

    /**
     * A simple text.
     */
    public static final Label LABEL_TRANSACTION = Label.builder()
            .id("LABEL_TRANSACTION")
            .key("label.transaction")
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
     * The translated value of the label.
     * Used only when the value must be overwritten.
     */
    private String i18n;

    /**
     * Setter method.
     *
     * @param i18n the label value
     */
    public void setLabel(String i18n) {
        this.i18n = i18n;
    }

    /**
     * Translation resolver that reads the belonging translation from
     * the property file.
     *
     * @param language language code, e.g. en
     * @return the translation text
     */
    public String getLabel(String language) {
        return Objects.isNull(i18n) ? I18n.get(language, key) : this.i18n;
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
