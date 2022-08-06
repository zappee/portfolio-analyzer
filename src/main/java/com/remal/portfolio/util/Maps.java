package com.remal.portfolio.util;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Tool that works with Map.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class Maps {

    /**
     * Update the BigDecimal value in a Map.
     *
     * @param map the Map that stores the values
     * @param key key in the Map
     * @param valueToAdd the value that will be added to the map
     */
    public static void updateMapValue(Map<String, BigDecimal> map, String key, BigDecimal valueToAdd) {
        if (Objects.nonNull(map) && BigDecimals.isNotNullAndNotZero(valueToAdd)) {
            var previousValue = map.get(key);
            if (Objects.isNull(previousValue)) {
                map.put(key, valueToAdd);
            } else {
                map.put(key, previousValue.add(valueToAdd));
            }
        }
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private Maps() {
        throw new UnsupportedOperationException();
    }

}
