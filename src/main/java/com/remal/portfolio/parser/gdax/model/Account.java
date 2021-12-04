package com.remal.portfolio.parser.gdax.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POJO that holds the parsed data from account.csv file.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Builder
@ToString
@Getter
public class Account {

    /**
     *  The 'portfolio' column of the account.csv file.
     */
    private String portfolio;

    /**
     *  The 'type' column of the account.csv file.
     */
    private String type;

    /**
     *  The 'time' column of the account.csv file.
     */
    private LocalDateTime time;

    /**
     *  The 'amount' column of the account.csv file.
     */
    private BigDecimal amount;

    /**
     *  This is a calculated  column.
     */
    private BigDecimal averagePrice;

    /**
     *  The 'balance' column of the account.csv file.
     */
    private BigDecimal balance;

    /**
     *  The 'amount/balance unit' column of the account.csv file.
     */
    private String unit;

    /**
     *  The 'transfer id' column of the account.csv file.
     */
    private String transferId;

    /**
     *  The 'trade id' column of the account.csv file.
     */
    private String tradeId;

    /**
     *  The 'order id' column of the account.csv file.
     */
    private String orderId;
}
