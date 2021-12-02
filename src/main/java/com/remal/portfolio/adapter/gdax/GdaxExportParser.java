package com.remal.portfolio.adapter.gdax;

import com.remal.portfolio.adapter.gdax.model.Account;
import com.remal.portfolio.adapter.gdax.model.Fill;
import com.remal.portfolio.model.Currency;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.util.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This parser class parses the Coinbase GDAX export files.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class GdaxExportParser {

    /**
     * CSV separator.
     */
    @Setter
    @Getter
    private char csvSeparator = ',';

    /**
     * Set it to true if the CSV file that will be parsed has a header at the first row.
     */
    @Accessors(fluent = true)
    @Setter
    @Getter
    private boolean hasHeader = true;

    /**
     * The parsed account.csv file.
     */
    private final List<Account> accounts = new ArrayList<>();

    /**
     * The parsed fills.csv file.
     */
    private final Map<String, Fill> fills = new HashMap<>();

    /**
     * The complete list of the transactions that the user has made
     * and was filled.
     */
    @Getter
    private final List<Transaction> transactions = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param accountCsvFile path to account.csv file
     * @param fillsCsvFile path to fill.csv file
     * @throws IOException throws in case of error
     */
    public GdaxExportParser(String accountCsvFile, String fillsCsvFile) throws IOException {
        parseAccountCsv(accountCsvFile);
        parseFillsCsv(fillsCsvFile);
    }

    /**
     * Collects the transaction list.
     */
    public void parse() {
        log.debug("generating transaction list...");
        transactions.clear();
        accounts.forEach(account -> {
            Fill fill = fills.getOrDefault(account.getTradeId(), Fill.builder().build());

            // correction in case of DEPOSIT/WITHDRAWAL in EUR (or USD)
            TransactionType type = toType(account.getType(), fill.getSide());
            Currency currency = Currency.getEnum(fill.getUnit());
            String ticker = account.getUnit();
            if ((TransactionType.DEPOSIT == type || TransactionType.WITHDRAWAL == type)
                    && Currency.UNKNOWN_CURRENCY != Currency.getEnum(ticker)) {
                currency = Currency.getEnum(ticker);
                ticker = null;
            }

            Transaction t = Transaction
                    .builder()
                    .portfolio(account.getPortfolio())
                    .type(type)
                    .created(account.getTime())
                    .volume(account.getAmount())
                    .price(fill.getPrice())
                    .fee(fill.getFee())
                    .currency(currency)
                    .ticker(ticker)
                    .transferId(account.getTransferId())
                    .tradeId(account.getTradeId())
                    .orderId(account.getOrderId())
                    .build();
            transactions.add(t);
        });
    }

    /**
     * Convert type and side info to Enum.
     *
     * @param type type from account.csv
     * @param side side from fills.csv
     * @return the enum value
     */
    private TransactionType toType(String type, String side) {
        switch (type) {
            case "deposit":
            case "withdrawal":
                return TransactionType.valueOf(type.toUpperCase());

            // fee, match
            default:
                return TransactionType.getEnum(side);

        }
    }

    /**
     * Parses the Coinbase GDAX export file.
     *
     * @param  accountCsvFile path to the account.csv file
     * @throws java.io.IOException throws in case of error
     */
    private void parseAccountCsv(String accountCsvFile) throws IOException {
        log.debug("reading '{}' file...", accountCsvFile);
        int skipLeadingElements = hasHeader ? 1 : 0;
        try (Stream<String> stream = Files.lines(Paths.get(accountCsvFile))) {
            stream.skip(skipLeadingElements).forEach(line -> {
                String[] fields = line.split(",", -1);
                Account a = Account.builder()
                        .portfolio(fields[0])
                        .type(fields[1])
                        .time(Strings.toLocalDateTime(fields[2]))
                        .amount(new BigDecimal(fields[3]))
                        .balance(new BigDecimal(fields[4]))
                        .unit(fields[5])
                        .transferId(fields[6])
                        .tradeId(fields[7])
                        .orderId(fields[8])
                        .build();

                accounts.add(a);
            });
        }
    }

    /**
     * Parses the Coinbase GDAX export file.
     *
     * @param fillsCsvFile path to the fills.csv file
     * @throws IOException throws in case of error
     */
    private void parseFillsCsv(String fillsCsvFile) throws IOException {
        log.debug("reading '{}' file...", fillsCsvFile);
        int skipLeadingElements = hasHeader ? 1 : 0;
        try (Stream<String> stream = Files.lines(Paths.get(fillsCsvFile))) {
            stream.skip(skipLeadingElements).forEach(line -> {
                String[] fields = line.split(",", -1);
                Fill fill = Fill.builder()
                        .portfolio(fields[0])
                        .tradeId(fields[1])
                        .product(fields[2])
                        .side(fields[3])
                        .createdAt(Strings.toLocalDateTime(fields[4]))
                        .size(new BigDecimal(fields[5]))
                        .sizeUnit(fields[6])
                        .price(new BigDecimal(fields[7]))
                        .fee(new BigDecimal(fields[8]))
                        .total(new BigDecimal(fields[9]))
                        .unit(fields[10])
                        .build();

                fills.put(fill.getTradeId(), fill);
            });
        }
    }
}
