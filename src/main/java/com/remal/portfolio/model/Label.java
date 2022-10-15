package com.remal.portfolio.model;

import com.remal.portfolio.util.I18n;

/**
 * Constants for labels that appear in the reports.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public enum Label {

    /**
     * A column header.
     */
    HEADER_AVG_PRICE("header.avg-price"),

    /**
     * A column header.
     */
    HEADER_COSTS("header.costs"),

    /**
     * A column header.
     */
    HEADER_DATA_PROVIDER("header.data-provider"),

    /**
     * A column header.
     */
    HEADER_DEPOSITS("header.deposits"),

    /**
     * An empty label that is used to format BigDecimal number without having a label.
     */
    HEADER_EMPTY("label.empty"),

    /**
     * A column header.
     */
    HEADER_FEE("header.fee"),

    /**
     * A column header.
     */
    HEADER_FEE_CURRENCY("header.fee-currency"),

    /**
     * A column header.
     */
    HEADER_INVESTED_AMOUNT("header.invested-amount"),

    /**
     * A column header.
     */
    HEADER_MARKET_UNIT_PRICE("header.market-unit-price"),

    /**
     * A column header.
     */
    HEADER_MARKET_VALUE("header.market-value"),

    /**
     * A column header.
     */
    HEADER_ORDER_ID("header.order-id"),

    /**
     * A column header.
     */
    HEADER_PORTFOLIO("header.portfolio"),

    /**
     * A column header.
     */
    HEADER_PRICE("header.price"),

    /**
     * A column header.
     */
    HEADER_PRICE_CURRENCY("header.price-currency"),

    /**
     * A column header.
     */
    HEADER_PROFIT_LOSS("header.profit-loss"),

    /**
     * A column header.
     */
    HEADER_PROFIT_LOSS_PERCENT("header.profit-loss-percent"),

    /**
     * A column header.
     */
    HEADER_QUANTITY("header.quantity"),

    /**
     * A column header.
     */
    HEADER_REQUEST_DATE("header.request-date"),

    /**
     * A column header.
     */
    HEADER_SYMBOL("header.symbol"),

    /**
     * A column header.
     */
    HEADER_TRADE_DATE("header.trade-date"),

    /**
     * A column header.
     */
    HEADER_TRADE_ID("header.trade-id"),

    /**
     * A column header.
     */
    HEADER_TRANSFER_ID("header.transfer-id"),

    /**
     * A column header.
     */
    HEADER_TYPE("header.type"),

    /**
     * A column header.
     */
    HEADER_VALUATION("header.valuation"),

    /**
     * A column header.
     */
    HEADER_WITHDRAWALS("header.withdrawals"),

    /**
     * A simple text.
     */
    LABEL_PORTFOLIO_SUMMARY("label.portfolio-summary"),

    /**
     * A simple text.
     */
    LABEL_TOTAL_CASH_PER_CURRENCY("label.total.cash-per-currency"),

    /**
     * A simple text.
     */
    LABEL_TOTAL_CASH("label.total.cash"),

    /**
     * A simple text.
     */
    LABEL_TOTAL_DEPOSIT("label.total.deposit"),


    /**
     * A simple text.
     */
    LABEL_TOTAL_DEPOSIT_PER_CURRENCY("label.total.deposit-per-currency"),

    /**
     * A simple text.
     */
    LABEL_TOTAL_EXCHANGE_RATE("label.total.exchange-rate"),

    /**
     * A simple text.
     */
    LABEL_TOTAL_INVESTMENT("label.total.investment"),

    /**
     * A simple text.
     */
    LABEL_TOTAL_INVESTMENT_PER_CURRENCY("label.total.investment-per-currency"),

    /**
     * A simple text.
     */
    LABEL_TOTAL_MARKET_VALUE("label.total.market-value"),

    /**
     * A simple text.
     */
    LABEL_TOTAL_MARKET_VALUE_PER_CURRENCY("label.total.market-value-per-currency"),

    /**
     * A simple text.
     */
    LABEL_TOTAL_PROFIT_LOSS("label.total.profit-loss"),

    /**
     * A simple text.
     */
    LABEL_TOTAL_PROFIT_LOSS_PER_CURRENCY("label.total.profit-loss-per-currency"),

    /**
     * A simple text.
     */
    LABEL_TOTAL_WITHDRAWAL("label.total.withdrawal"),

    /**
     * A simple text.
     */
    LABEL_TOTAL_WITHDRAWAL_PER_CURRENCY("label.total.withdrawal-per-currency"),

    /**
     * A simple text.
     */
    LABEL_TRANSACTION_REPORT("label.transaction-report"),

    /**
     * A report title.
     */
    TITLE_SUMMARY_REPORT("title.summary-report"),

    /**
     * A report title.
     */
    TITLE_TRANSACTIONS_REPORT("title.transactions-report");

    /**
     * The key that identifies the translation in the I18N *.properties file.
     */
    private final String key;

    Label(String key) {
        this.key = key;
    }

    /**
     * Translation resolver that reads the belonging translation from
     * the property file.
     *
     * @param language an ISO 639 alpha-2 or alpha-3 language code, e.g. en
     * @return the translation text
     */
    public String getLabel(String language) {
        return I18n.get(language, key);
    }
}
