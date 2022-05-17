package com.remal.portfolio.model;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * POJO holds the data provider configuration.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Getter
public class DataProviderConfiguration {

    /**
     * A ticker is an abbreviation used to uniquely identify publicly traded
     * shares of a particular stock on a particular stock market.
     */
    private String ticker;

    /**
     * The data provider ID.
     */
    private ProviderType providerType;

    /**
     * The date and time when the data provider has been set.
     */
    private LocalDateTime updated;
}
