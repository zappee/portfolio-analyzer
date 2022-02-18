package com.remal.portfolio.model;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Objects;

/**
 * Inventory valuation is an accounting practice that is followed by companies
 * to find out the value of unsold inventory stock at the time they are
 * preparing their financial statements.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public enum InventoryValuation {

    /**
     * In FIFO, you assume that the first items purchased are the first to
     * leave the warehouse. In other words, whenever you make a sale, under
     * FIFO, the items will be subtracted from the first list of products which
     * entered your store or warehouse.
     */
    FIFO,

    /**
     * In LIFO, you make the opposite assumption: that the last items that
     * enter your store are the first ones to leave.
     */
    LIFO,
    ;

    /**
     * A null safe valueOf method.
     *
     * @param value the String value of the enum
     * @return the enum value or null if the given input is not parsable
     */
    public static InventoryValuation getEnum(String value) {
        try {
            return Objects.isNull(value) || value.isEmpty() ? null : InventoryValuation.valueOf(value.toUpperCase());
        } catch (NullPointerException e) {
            log.error("Unknown inventory evaluation type: '{}'", value);
            System.exit(CommandLine.ExitCode.SOFTWARE);
            return null;
        }
    }
}
