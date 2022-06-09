package com.remal.portfolio.model;

import com.remal.portfolio.util.Filter;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
@Slf4j
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
    private ProviderType providerType;

    /**
     * The time and date when the price was downloaded.
     */
    private LocalDateTime date;

    /**
     * Add a new price to the list based on  the provided multiplicity.
     *
     * @param productPrices the product price list
     * @param productPrice  the current price to add to the list
     * @param multiplicity  controls how to add the price to the list
     */
    public static void merge(List<ProductPrice> productPrices,
                             ProductPrice productPrice,
                             MultiplicityType multiplicity) {

        if (Objects.nonNull(productPrice)) {
            // avoid the overlapping of the intervals
            var correction = 1;

            var intervalEnd = productPrice.getDate();
            var intervalStart = intervalEnd.minusSeconds(multiplicity.getRangeLengthInSec() - correction);
            var itemCount = productPrices
                    .stream()
                    .filter(x -> x.getTicker().equals(productPrice.getTicker()))
                    .filter(x -> Filter.dateBetweenFilter(intervalStart, intervalEnd, x.getDate()))
                    .count();

            log.debug("> multiplicity: {}", multiplicity.name());
            log.debug("> number of item within the range: {}, ticker: {}", itemCount, productPrice.getTicker());

            if (multiplicity == MultiplicityType.MANY || itemCount == 0) {
                productPrices.add(productPrice);
            } else {
                var message = "> {} price wont be added to the output because of the multiplicity setting";
                log.warn(message, productPrice.getTicker());
            }
        }
    }
}
