package com.remal.portfolio.parser.gdax.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POJO that holds the parsed data from Coinbase Gdax account.csv file.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Builder
@Getter
public class Fill {

    /**
     *  The 'portfolio' column of the account.csv file.
     */
    private String portfolio;

    /**
     *  The 'trade id' column of the account.csv file.
     */
    private String tradeId;

    /**
     *  The 'product' column of the account.csv file.
     */
    private String product;

    /**
     *  The 'side' column of the account.csv file.
     */
    private String side;

    /**
     *  The 'created at' column of the account.csv file.
     */
    private LocalDateTime createdAt;

    /**
     *  The 'size' column of the account.csv file.
     */
    private BigDecimal size;

    /**
     *  The 'size unit' column of the account.csv file.
     */
    private String sizeUnit;

    /**
     *  The 'price' column of the account.csv file.
     */
    private BigDecimal price;

    /**
     *  The 'fee' column of the account.csv file.
     */
    private BigDecimal fee;

    /**
     *  The 'total' column of the account.csv file.
     */
    private BigDecimal total;

    /**
     *  The 'amount/balance unit' column of the account.csv file.
     */
    private String unit;
}
