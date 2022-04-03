package com.remal.portfolio.util;

import java.util.Objects;

/**
 * Tool that works with Enum objects.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class Enums {

    /**
     * Convert any enum value to a String.
     *
     * @param genericEnum enum value
     * @return the enum name or null if the enum is null
     */
    public static String enumToString(Enum<?> genericEnum) {
        if (Objects.isNull(genericEnum)) {
            return null;
        }
        return genericEnum.name();
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    private Enums() {
        throw new UnsupportedOperationException();
    }
}
