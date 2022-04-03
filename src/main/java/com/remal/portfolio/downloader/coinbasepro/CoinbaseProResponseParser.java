package com.remal.portfolio.downloader.coinbasepro;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.InventoryValuationType;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Transaction downloader that downloads trades via Coinbase Pro API.
 * API information: https://docs.cloud.coinbase.com/exchange/docs
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class CoinbaseProResponseParser extends CoinbaseProRequestBuilder {

    /**
     * Coinbase profile info.
     */
    private final HashMap<String, String> profiles = new HashMap<>();

    /**
     * Coinbase account info.
     */
    private final HashMap<String, String> accounts = new HashMap<>();

    /**
     * Products that are supported by Coinbase.
     */
    private final List<String> productIds = new ArrayList<>();

    /**
     * Currencies that are supported by Coinbase.
     */
    private final List<String> currencies = new ArrayList<>();

    /**
     * Base currency of the user's CoinbasePro account.
     */
    private final CurrencyType baseCurrency;

    /**
     * Inventory valuation.
     */
    private final InventoryValuationType defaultInventoryValuation;

    /**
     * The timezone to where trade date will convert to.
     */
    private final String zoneTo;

    /**
     * Constructor.
     *
     * @param publicKey Coinbase Pro API key as a string
     * @param passphrase Coinbase Pro passphrase
     * @param secret Coinbase Pro secret for the API key
     * @param defaultInventoryValuation default inventory valuation
     * @param baseCurrency ISO 4217 currency code that Coinbase registered during the registration process
     * @param zoneTo the timezone to where trade date will convert to
     */
    public CoinbaseProResponseParser(String publicKey,
                                     String passphrase,
                                     String secret,
                                     CurrencyType baseCurrency,
                                     InventoryValuationType defaultInventoryValuation,
                                     String zoneTo) {

        super(publicKey, passphrase, secret);
        this.baseCurrency = baseCurrency;
        this.defaultInventoryValuation = defaultInventoryValuation;
        this.zoneTo = zoneTo;
        log.debug("initializing Coinbase Pro API caller with base currency '{}'...", baseCurrency);

        try {
            initializeProfiles();
            initializeProducts();
            initializeAccounts();
            initializeCurrencies();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            Logger.logErrorAndExit("Coinbase Pro API Initialization error: {}", e.getMessage());
        }
    }

    /**
     * Downloads your trade activities from Coinbase.
     * This includes anything that would affect the accounts balance, i.e. transfers, trades, fees, etc.
     *
     * @return list of transactions
     */
    public List<Transaction> parse() {
        List<Transaction> transactions = new ArrayList<>();
        downloadTransfers(transactions);
        productIds
                .stream().filter(product -> baseCurrency == null || product.contains(baseCurrency.name()))
                .forEach(productId -> downloadTransactions(transactions, productId));

        Sorter.sort(transactions);
        return transactions;
    }

    /**
     * Downloads your transfers (deposits and withdrawals) from Coinbase
     * and add them to the transaction list.
     *
     * @param transactions list of transactions
     */
    private void downloadTransfers(List<Transaction> transactions) {
        try {
            var endpoint = "/transfers";
            var jsonArray = getJsonArrayResponse(endpoint);
            jsonArray.ifPresent(x ->
                    IntStream.range(0, x.size()).forEach(index -> {
                        var jsonItem = x.get(index);
                        var fillJson = (JSONObject) jsonItem;
                        var createdAt = getCreatedAt((String) fillJson.get("created_at"));
                        var ticker = accounts.get(fillJson.get("account_id").toString());
                        var isCurrency = currencies.contains(ticker);

                        Transaction transaction = Transaction
                                .builder()
                                .portfolio(profiles.get(fillJson.get("user_id").toString()))
                                .type(TransactionType.getEnum(fillJson.get("type").toString()))
                                .tradeDate(createdAt)
                                .quantity(new BigDecimal(fillJson.get("amount").toString()))
                                .price(isCurrency ? BigDecimal.ONE : BigDecimal.TEN)
                                .fee(isCurrency ? BigDecimal.ZERO : BigDecimal.TEN)
                                .currency(isCurrency ? CurrencyType.getEnum(ticker) : CurrencyType.UNKNOWN)
                                .ticker(ticker)
                                .transferId(fillJson.get("id").toString())
                                .build();
                        transactions.add(transaction);
                    })
            );
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("An unexpected error has occurred while downloading transfers from Coinbase Pro. {}.",
                    e.toString());
        }
    }

    private LocalDateTime getCreatedAt(String createdAtAsString) {
        var pattern = "yyyy-MM-dd HH:mm:ss.[SSSSSS][SSSSS]x";
        var createdAt = Strings.toLocalDateTime(pattern, createdAtAsString);

        if (Objects.nonNull(zoneTo)) {
            createdAt = createdAt
                    .atZone(ZoneOffset.UTC)
                    .withZoneSameInstant(ZoneId.of(zoneTo))
                    .toLocalDateTime();
        }

        return createdAt;
    }

    /**
     * Downloads your transactions (buys and sells) that belong to a given
     * product-id from Coinbase and add them to the transaction list.
     *
     * @param transactions list of transactions
     * @param productId coinbase product-id
     */
    private void downloadTransactions(List<Transaction> transactions, String productId) {
        try {
            var endpoint = "/fills?product_id=" + productId;
            var jsonArray = getJsonArrayResponse(endpoint);
            jsonArray.ifPresent(x ->
                    IntStream.range(0, x.size()).forEach(index -> {
                        var jsonItem = x.get(index);
                        var fillJson = (JSONObject) jsonItem;
                        var transactionType = TransactionType.getEnum(fillJson.get("side").toString());
                        var inventoryValuation = (transactionType == TransactionType.SELL)
                                ? defaultInventoryValuation
                                : null;

                        Transaction transaction = Transaction
                                .builder()
                                .portfolio(profiles.get(fillJson.get("profile_id").toString()))
                                .type(transactionType)
                                .inventoryValuation(inventoryValuation)
                                .tradeDate(Strings.toLocalDateTime(fillJson.get("created_at").toString()))
                                .quantity(new BigDecimal(fillJson.get("size").toString()))
                                .price(new BigDecimal(fillJson.get("price").toString()))
                                .fee(new BigDecimal(fillJson.get("fee").toString()))
                                .currency(CurrencyType.getEnum(productId.split("-")[1]))
                                .ticker(productId)
                                .tradeId(fillJson.get("trade_id").toString())
                                .orderId(fillJson.get("order_id").toString())
                                .build();
                        transactions.add(transaction);
                    })
            );
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("An unexpected error has occurred while downloading transactions from Coinbase Pro. {}.",
                    e.toString());
        }
    }

    /**
     * Downloads user profiles from Coinbase.
     *
     * @throws IOException throws in case of error
     * @throws NoSuchAlgorithmException throws in case of error
     * @throws InvalidKeyException throws in case of error
     */
    private void initializeProfiles() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        var jsonArray = getJsonArrayResponse("/profiles");
        if (jsonArray.isPresent()) {
            for (Object jsonItem : jsonArray.get()) {
                var json = (JSONObject) jsonItem;
                if ((boolean) json.get("active")) {
                    var id = json.get("id").toString();
                    var userId = json.get("user_id").toString();
                    var name = json.get("name").toString();
                    profiles.put(id, name);
                    profiles.put(userId, name);
                }
            }
        }
    }

    /**
     * Downloads currencies that are supported by Coinbase.
     *
     * @throws IOException throws in case of error
     * @throws NoSuchAlgorithmException throws in case of error
     * @throws InvalidKeyException throws in case of error
     */
    private void initializeCurrencies() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        var jsonArray = getJsonArrayResponse("/currencies");
        if (jsonArray.isPresent()) {
            for (Object jsonItem : jsonArray.get()) {
                var json = (JSONObject) jsonItem;
                var details = (JSONObject) json.get("details");
                var type = details.get("type").toString();
                if ("fiat".equalsIgnoreCase(type)) {
                    currencies.add(json.get("id").toString());
                }
            }
        }
    }

    /**
     * Downloads products that are supported by Coinbase.
     *
     * @throws IOException throws in case of error
     * @throws NoSuchAlgorithmException throws in case of error
     * @throws InvalidKeyException throws in case of error
     */
    private void initializeProducts() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        var jsonArray = getJsonArrayResponse("/products");
        if (jsonArray.isPresent()) {
            for (Object jsonItem : jsonArray.get()) {
                var json = (JSONObject) jsonItem;
                productIds.add(json.get("id").toString());
            }
        }
    }

    /**
     * Downloads user account info from Coinbase.
     *
     * @throws IOException throws in case of error
     * @throws NoSuchAlgorithmException throws in case of error
     * @throws InvalidKeyException throws in case of error
     */
    private void initializeAccounts() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        var jsonArray = getJsonArrayResponse("/accounts");
        if (jsonArray.isPresent()) {
            for (Object jsonItem : jsonArray.get()) {
                var json = (JSONObject) jsonItem;
                var id = json.get("id").toString();
                var ticker = json.get("currency").toString();
                accounts.put(id, ticker);
            }
        }
    }
}
