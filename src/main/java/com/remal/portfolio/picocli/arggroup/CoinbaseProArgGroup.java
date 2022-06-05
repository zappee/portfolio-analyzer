package com.remal.portfolio.picocli.arggroup;

import com.remal.portfolio.model.InventoryValuationType;
import lombok.Getter;
import picocli.CommandLine;

/**
 * Command line interface argument group for defining input options.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class CoinbaseProArgGroup {

    /**
     * Input configuration.
     */
    @Getter
    public static class InputArgGroup {

        /**
         * The date-time pattern that is used to parse the "from" and "to"
         * parameters.
         */
        public static final String DATE_TIME_FILTER_PATTERN = "yyyy-MM-dd HH:mm:ss";

        /**
         * Set the Coinbase API key.
         */
        @CommandLine.Option(
                names = {"-k", "--api-access-key"},
                description = "Coinbase PRO API key.",
                required = true)
        private String key;

        /**
         * Set Coinbase Pro passphrase.
         */
        @CommandLine.Option(
                names = {"-p", "--api-passphrase"},
                description = "Coinbase PRO API passphrase.",
                required = true)
        private String passphrase;

        /**
         * Set Coinbase Pro secret for the API key.
         */
        @CommandLine.Option(
                names = {"-e", "--api-secret"},
                description = "Coinbase PRO API secret.",
                required = true)
        private String secret;

        /**
         * Set the ISO 4217 currency code that Coinbase
         * registered for you.
         */
        @CommandLine.Option(
                names = {"-b", "--base-currency"},
                description = "The currency of your Coinbase account you are allowed to trade, e.g. \"EUR\", etc. "
                        + "Default: \"${DEFAULT-VALUE}\"",
                defaultValue = "EUR")
        private String baseCurrency;

        /**
         * Set the inventory valuation type.
         */
        @CommandLine.Option(
                names = {"-v", "--valuation"},
                description = "Default inventory valuation type. Candidates: ${COMPLETION-CANDIDATES}. "
                        + "Default: \"${DEFAULT-VALUE}\"",
                defaultValue = "FIFO")
        private InventoryValuationType inventoryValuation;

        /**
         * Set the filter that filters the list of transactions.
         */
        @CommandLine.Option(
                names = {"-f", "--in-from"},
                description = "Filter on trade date, after a specified date. Pattern: \"yyyy-MM-dd HH:mm:ss\"")
        private String from;

        /**
         * Set the filter that filters the list of transactions.
         */
        @CommandLine.Option(
                names = {"-t", "--in-to"},
                description = "Filter on trade date, before a specified date. Pattern: \"yyyy-MM-dd HH:mm:ss\"")
        private String to;
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private CoinbaseProArgGroup() {
        throw new UnsupportedOperationException();
    }
}
