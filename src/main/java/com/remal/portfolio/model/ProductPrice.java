package com.remal.portfolio.model;

import lombok.Builder;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POJO that hold information about a sock price.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Builder
@ToString
public class ProductPrice {

    /**
     * A ticker is an abbreviation used to uniquely identify publicly traded
     * shares of a particular stock on a particular stock market.
     */
    private String ticker;

    /**
     * The price for one unit.
     */
    private BigDecimal price;

    /**
     * The data provider.
     */
    private DataProvider dataProvider;

    /**
     * The time and date when the price was downloaded.
     */
    private LocalDateTime timestamp;
}