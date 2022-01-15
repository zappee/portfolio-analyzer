package com.remal.portfolio.parser.coinbase;

import com.remal.portfolio.model.Currency;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import picocli.CommandLine;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class CoinbaseProApiParser extends CoinbaseProApiRequestBuilder implements Parser {

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
    private final String baseCurrency;

    /**
     * Constructor.
     *
     * @param publicKey Coinbase Pro API key as a string
     * @param passphrase Coinbase Pro passphrase
     * @param secret Coinbase Pro secret for the API key
     * @param baseCurrency the ISO 4217 currency code that Coinbase registered for you
     */
    public CoinbaseProApiParser(String publicKey, String passphrase, String secret, String baseCurrency) {
        super(publicKey, passphrase, secret);
        this.baseCurrency = baseCurrency;

        try {
            initializeProfiles();
            initializeProducts();
            initializeAccounts();
            initializeCurrencies();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Coinbase Pro API Initialization error: {}", e.toString());
            System.exit(CommandLine.ExitCode.SOFTWARE);
        }
    }

    /**
     * Downloads your trade activities from Coinbase.
     * This includes anything that would affect the accounts balance, i.e. transfers, trades, fees, etc.
     *
     * @return list of transactions
     */
    @Override
    public List<Transaction> parse() {
        List<Transaction> transactions = new ArrayList<>();
        downloadTransfers(transactions);
        productIds
                .stream().filter(product -> baseCurrency == null || product.contains(baseCurrency))
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
            var coinbaseDateTimePattern = "yyyy-MM-dd HH:mm:ss.[SSSSSS][SSSSS]x";
            var jsonArray = getJsonArrayResponse(endpoint);
            jsonArray.ifPresent(x ->
                    IntStream.range(0, x.size()).forEach(index -> {
                        var jsonItem = x.get(index);
                        var fillJson = (JSONObject) jsonItem;
                        var createdAt = Strings.toLocalDateTime(
                                coinbaseDateTimePattern,
                                ZoneOffset.UTC,
                                fillJson.get("created_at").toString());
                        var ticker = accounts.get(fillJson.get("account_id").toString());
                        var isCurrency = currencies.contains(ticker);

                        Transaction transaction = Transaction
                                .builder()
                                .portfolio(profiles.get(fillJson.get("user_id").toString()))
                                .type(TransactionType.getEnum(fillJson.get("type").toString()))
                                .created(createdAt)
                                .volume(new BigDecimal(fillJson.get("amount").toString()))
                                .price(isCurrency ? BigDecimal.ONE : BigDecimal.TEN)
                                .fee(isCurrency ? BigDecimal.ZERO : BigDecimal.TEN)
                                .currency(isCurrency ? Currency.getEnum(ticker) : Currency.UNKNOWN)
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
                        Transaction transaction = Transaction
                                .builder()
                                .portfolio(profiles.get(fillJson.get("profile_id").toString()))
                                .type(TransactionType.getEnum(fillJson.get("side").toString()))
                                .created(Strings.toLocalDateTime(fillJson.get("created_at").toString()))
                                .volume(new BigDecimal(fillJson.get("size").toString()))
                                .price(new BigDecimal(fillJson.get("price").toString()))
                                .fee(new BigDecimal(fillJson.get("fee").toString()))
                                .currency(Currency.getEnum(productId.split("-")[1]))
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
