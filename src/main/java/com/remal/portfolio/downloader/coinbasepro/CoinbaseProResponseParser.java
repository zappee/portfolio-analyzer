package com.remal.portfolio.downloader.coinbasepro;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.InventoryValuationType;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.picocli.arggroup.CoinbaseProArgGroup;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Logger;
import com.remal.portfolio.util.Sorter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Transaction downloader that downloads trades via Coinbase Pro API.
 * API information: https://docs.cloud.coinbase.com/exchange/docs
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
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
     * Transaction date filter.
     */
    private final LocalDateTime from;

    /**
     * Transaction date filter.
     */
    private final LocalDateTime to;

    /**
     * Constructor.
     *
     * @param arguments arguments from the command line interface
     */
    public CoinbaseProResponseParser(CoinbaseProArgGroup.InputArgGroup arguments) {
        super(arguments.getKey(), arguments.getPassphrase(), arguments.getSecret());

        this.baseCurrency = CurrencyType.getEnum(arguments.getBaseCurrency());
        this.defaultInventoryValuation = arguments.getInventoryValuation();
        this.from = LocalDateTimes.toLocalDateTime(arguments.getDateTimeFilterPattern(), arguments.getFrom());
        this.to = LocalDateTimes.getFilterTo(arguments.getDateTimeFilterPattern(), arguments.getTo());
        log.debug("input < initializing Coinbase Pro API caller with base currency '{}'...", baseCurrency);

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
                .stream()
                .filter(product -> Filter.baseCurrencyFilter(baseCurrency, product))
                .forEach(productId -> downloadTransactions(transactions, productId));

        return transactions
                .stream()
                .filter(t -> Filter.dateEqualOrAfterFilter(t.getTradeDate(), from))
                .filter(t -> Filter.dateEqualOrBeforeFilter(t.getTradeDate(), to))
                .sorted(Sorter.tradeDateComparator())
                .toList();
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
                        var ticker = accounts.get(fillJson.get("account_id").toString());
                        var isCurrency = currencies.contains(ticker);
                        var pattern = "yyyy-MM-dd HH:mm:ss.[SSSSSS][SSSSS]x";

                        Transaction transaction = Transaction
                                .builder()
                                .portfolio(profiles.get(fillJson.get("user_id").toString()))
                                .type(TransactionType.getEnum(fillJson.get("type").toString()))
                                .tradeDate(LocalDateTimes.toLocalDateTime(pattern, (String) fillJson.get("created_at")))
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
                                .tradeDate(LocalDateTimes.toLocalDateTime(fillJson.get("created_at").toString()))
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
