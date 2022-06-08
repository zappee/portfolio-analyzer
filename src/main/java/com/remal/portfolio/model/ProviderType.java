package com.remal.portfolio.model;

import com.remal.portfolio.util.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Accepted data providers for getting the price of a stock.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public enum ProviderType {

    /**
     * Yahoo Finance API.
     */
    YAHOO,

    /**
     * Coinbase PRO API.
     */
    COINBASE_PRO;

    /**
     * A null safe valueOf method.
     *
     * @param value the String value of the enum
     * @return      the enum value or null if the given input is not parsable
     */
    public static ProviderType getEnum(String value) {
        try {
            return ProviderType.valueOf(value.toUpperCase());
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Get the data providerType from the *.properties file.
     *
     * @param ticker the product id that represents the company's stock
     * @param file   the configuration file with the providerType names
     * @return       the selected data providerType
     */
    public static ProviderType getProvider(String ticker, String file) {
        ProviderType providerType = null;
        var providerAsString = "";

        try (InputStream inputStream = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(inputStream);

            providerAsString = properties.getProperty(ticker);
            if (Objects.isNull(providerAsString)) {
                var message = "There is no providerType definition in the '{}' for ticker '{}'.";
                Logger.logErrorAndExit(message, file, ticker);
            } else {
                // providerAsString: <TICKER-NAME-AT-PROVIDER>;<PROVIDER-NAME>
                // sample: OTP.BD;YAHOO
                var providerParts = providerAsString.split(";");
                if (providerParts.length == 1) {
                    providerType = ProviderType.valueOf(providerParts[0]);
                } else {
                    providerType = ProviderType.valueOf(providerParts[1]);
                }
            }

        } catch (IOException e) {
            var message = "Error while reading the \"{}\" file. Error: {}";
            Logger.logErrorAndExit(message, file, e.toString());
        } catch (IllegalArgumentException e) {
            var message = "Invalid data provider is set for ticker '{}' in the '{}' file, provider: '{}'";
            Logger.logErrorAndExit(message, ticker, file, providerAsString);
        }
        return providerType;
    }

    /**
     * Get the data providerType from the *.properties file.
     *
     * @param ticker the product id that represents the company's stock
     * @param file   the configuration file with the providerType names
     * @return       the selected data providerType
     */
    public static String getTicker(String ticker, String file) {
        var tickerAlias = ticker;
        var providerAsString = "";

        if (Objects.isNull(file)) {
            return ticker;
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(inputStream);

            providerAsString = properties.getProperty(ticker);
            if (Objects.isNull(providerAsString)) {
                var message = "There is no providerType definition in the '{}' for ticker '{}'.";
                Logger.logErrorAndExit(message, file, ticker);
            } else {
                // providerAsString: <TICKER-NAME-AT-PROVIDER>;<PROVIDER-NAME>
                // sample: OTP.BD;YAHOO
                var providerParts = providerAsString.split(";");
                if (providerParts.length == 2) {
                    tickerAlias = providerParts[0];
                }
            }
        } catch (IOException e) {
            var message = "Error while reading the \"{}\" file. Error: {}";
            Logger.logErrorAndExit(message, file, e.toString());
        } catch (IllegalArgumentException e) {
            var message = "Invalid data provider is set for ticker '{}' in the '{}' file, provider: '{}'";
            Logger.logErrorAndExit(message, ticker, file, providerAsString);
        }
        return tickerAlias;
    }
}
