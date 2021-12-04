package com.remal.portfolio.parser.gdax;

import com.remal.portfolio.parser.gdax.model.Account;
import com.remal.portfolio.parser.gdax.model.Fill;
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
import java.util.List;
import java.util.Optional;
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
     * The complete list of the transactions that the user has made
     * and was filled.
     */
    @Getter
    private final List<Transaction> transactions = new ArrayList<>();

    /**
     * The parsed account.csv file.
     */
    private final ArrayList<Account> accounts = new ArrayList<>();

    /**
     * The parsed fills.csv file.
     */
    private final ArrayList<Fill> fills = new ArrayList<>();

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
     * Builds the transaction list.
     */
    public void parse() {
        log.debug("generating transaction list...");
        transactions.clear();
        // add transactions
        fills
                .forEach(fill -> {
                    Account a = getBelongingTransaction(fill.getTradeId(), fill.getProduct()).orElseThrow();
                    Transaction t = Transaction
                            .builder()
                            .portfolio(a.getPortfolio())
                            .type(toType(a.getType(), fill.getSide()))
                            .created(a.getTime())
                            .volume(fill.getSize())
                            .price(fill.getPrice())
                            .fee(fill.getFee())
                            .currency(Currency.getEnum(fill.getUnit()))
                            .ticker(fill.getProduct())
                            .transferId(a.getTransferId())
                            .tradeId(a.getTradeId())
                            .orderId(a.getOrderId())
                            .build();
                    transactions.add(t);
                });

        // add deposit and withdrawal
        transactions.addAll(getDepositsAndWithdrawals());
    }

    /**
     * Returns with the list of deposits and withdrawals.
     *
     * @return the list of the transactions
     */
    private List<Transaction> getDepositsAndWithdrawals() {
        List<Transaction> t = new ArrayList<>();
        accounts
                .stream()
                .filter(a ->
                        a.getType().toUpperCase().equals(TransactionType.DEPOSIT.name())
                                || a.getType().toUpperCase().equals(TransactionType.WITHDRAWAL.name()))
                .filter(a -> a.getTradeId().isEmpty())
                .forEach(a -> t.add(Transaction
                        .builder()
                        .portfolio(a.getPortfolio())
                        .type(toType(a.getType(), null))
                        .created(a.getTime())
                        .volume(a.getAmount())
                        .price(BigDecimal.ONE)
                        .fee(BigDecimal.ZERO)
                        .currency(Currency.getEnum(a.getUnit()))
                        .transferId(a.getTransferId())
                        .tradeId(a.getTradeId())
                        .orderId(a.getOrderId())
                        .build()));

        return t;
    }

    /**
     * Search for the transaction in the account.csv file that belongs
     * to the given trade.
     *
     * @param tradeId trade id
     * @param product product name, e.g. ETH-EUR
     * @return the transaction
     */
    private Optional<Account> getBelongingTransaction(String tradeId, String product) {
        return accounts
                .stream()
                .filter(t -> {
                    var unit = product.split("-")[0];
                    return t.getTradeId().equals(tradeId) && t.getUnit().equals(unit);
                })
                .findFirst();
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
            case "fee":
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
                        .balance(fields[4].isEmpty() ? null : new BigDecimal(fields[4]))
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
                fills.add(fill);
            });
        }
    }
}
