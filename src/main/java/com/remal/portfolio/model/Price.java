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
public class Price {

    /**
     * A ticker is an abbreviation used to uniquely identify publicly traded
     * shares of a particular stock on a particular stock market.
     */
    private String ticker;

    /**
     * The price for one unit.
     */
    private BigDecimal unitPrice;

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
     * @param prices the product price list
     * @param price  the current price to add to the list
     * @param multiplicity  controls how to add the price to the list
     */
    public static void merge(List<Price> prices, Price price, MultiplicityType multiplicity) {
        if (Objects.nonNull(price)) {
            // avoid the overlapping of the intervals
            var correction = 1;

            var intervalEnd = price.getDate();
            var intervalStart = intervalEnd.minusSeconds(multiplicity.getRangeLengthInSec() - correction);
            var itemCount = prices
                    .stream()
                    .filter(x -> x.getTicker().equals(price.getTicker()))
                    .filter(x -> Filter.dateBetweenFilter(intervalStart, intervalEnd, x.getDate()))
                    .count();

            log.debug("> multiplicity: {}", multiplicity.name());
            log.debug("> number of item within the range: {}, ticker: {}", itemCount, price.getTicker());

            if (multiplicity == MultiplicityType.MANY || itemCount == 0) {
                prices.add(price);
            } else {
                var message = "> {} price wont be added to the output because of the multiplicity setting";
                log.warn(message, price.getTicker());
            }
        }
    }
}
