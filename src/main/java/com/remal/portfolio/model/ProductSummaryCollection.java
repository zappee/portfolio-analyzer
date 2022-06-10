package com.remal.portfolio.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Collection of product summaries POJO.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Getter
@Builder
public class ProductSummaryCollection {

    /**
     * The date when the report is generated.
     */
    private LocalDateTime generated;

    /**
     * The report itself.
     */
    private List<List<ProductSummary>> portfolios;
}
