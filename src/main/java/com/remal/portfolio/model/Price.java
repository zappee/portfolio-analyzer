package com.remal.portfolio.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POJO that hold information about a sock price.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Builder
@ToString
@Getter
@Setter
@EqualsAndHashCode
@Slf4j
public class Price {

    /**
     * A ticker is an abbreviation used to uniquely identify publicly traded
     * shares of a particular stock on a particular stock market.
     */
    private String ticker;

    /**
     * The price for one unit.
     */
    @EqualsAndHashCode.Exclude
    private BigDecimal unitPrice;

    /**
     * The data provider.
     */
    private ProviderType providerType;

    /**
     * The time and date when the price was downloaded.
     */
    private LocalDateTime date;
}
