package com.remal.portfolio.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POJO that holds trade data.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Builder(toBuilder = true)
@ToString
@Getter
@Setter
public class Transaction {

    /**
     * The name of the group where the trade belongs to.
     */
    private String portfolio;

    /**
     * The type/side of the transaction.
     */
    private TransactionType type;

    /**
     * When the transaction was filled.
     */
    private LocalDateTime tradeDate;

    /**
     * The volume is commonly reported as the number of shares that changed
     * hands.
     */
    private BigDecimal quantity;

    /**
     * The price for one unit.
     */
    private BigDecimal price;

    /**
     * Trade commission, also called a stock trading fee. This is a brokerage
     * fee that is charged when you buy or sell stocks. You may also pay
     * commissions or fees for buying and selling other investments, such as
     * options or exchange-traded funds.
     */
    private BigDecimal fee;

    /**
     * The unit of the price and fee.
     */
    private CurrencyType currency;

    /**
     * A ticker is an abbreviation used to uniquely identify publicly traded
     * shares of a particular stock on a particular stock market.
     */
    private String ticker;

    /**
     * Used when transferring the items from one account to another.
     */
    private String transferId;

    /**
     * A  unique identification code that registers transactions on the
     * exchange provider’s side. This unique code allows verifying its status
     * and tracking within the exchange system. Trade ID is generated only when
     * a trade is executed for the order placed.
     */
    private String tradeId;

    /**
     * This refers to a single order you placed.
     * Order ID is generated as soon as your order is accepted by the exchange.
     */

    private String orderId;

    /**
     * Type of inventory valuation.
     */
    private InventoryValuationType inventoryValuation;
}
