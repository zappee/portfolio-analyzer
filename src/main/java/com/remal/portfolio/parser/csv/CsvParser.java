package com.remal.portfolio.parser.csv;

import com.remal.portfolio.model.Currency;
import com.remal.portfolio.model.InventoryValuation;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Transforms a CSV file to list of transactions.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class CsvParser implements Parser {

    /**
     * CSV separator.
     */
    @Setter
    @Getter
    private char separator = ',';

    /**
     * Set it to true if the CSV file that will be parsed has a header at the first row.
     */
    @Accessors(fluent = true)
    @Setter
    @Getter
    private boolean hasHeader = true;

    /**
     * Date-time pattern that is used for converting string to LocalDateTime.
     */
    @Getter
    private final String dateTimePattern;

    /**
     * The CSV file to be parses.
     */
    @Getter
    private final String file;

    /**
     * Constructor.
     *
     * @param file csv file to be parsed
     * @param dateTimePattern pattern used for converting string to LocalDateTime
     */
    public CsvParser(String file, String dateTimePattern) {
        this.file = file;
        this.dateTimePattern = dateTimePattern;
    }

    /**
     * Builds the transaction list.
     */
    @Override
    public List<Transaction> parse() {
        log.debug("reading transactions from the '{}' CSV file...", file);
        List<Transaction> transactions = new ArrayList<>();

        try {
            var firstDataRow = 4;
            int skipLeadingElements = hasHeader ? firstDataRow : 0;
            if (skipLeadingElements != 0) {
                log.warn("skipping the first {} lines while reading the {} file...", skipLeadingElements, file);
            }

            try (Stream<String> stream = Files.lines(Paths.get(file))) {
                stream.skip(skipLeadingElements).forEach(line -> {
                    var fields = line.split(Character.toString(separator), -1);
                    Transaction t = Transaction
                            .builder()
                            .portfolio(fields[0])
                            .ticker(fields[1])
                            .type(TransactionType.getEnum(fields[2]))
                            .inventoryValuation(InventoryValuation.getEnum(fields[3].trim()))
                            .tradeDate(Strings.toLocalDateTime(dateTimePattern, fields[4]))
                            .quantity(BigDecimals.valueOf(fields[5]))
                            .price(BigDecimals.valueOf(fields[6]))
                            .fee(BigDecimals.valueOf(fields[7]))
                            .currency(Currency.getEnum(fields[8]))
                            .orderId(fields[9].isBlank() ? null : fields[9])
                            .tradeId(fields[10].isBlank() ? null : fields[10])
                            .transferId(fields[11].isBlank() ? null : fields[11])
                            .build();
                    transactions.add(t);
                });
            }
        } catch (IOException e) {
            log.error("Error while parsing the '{}' file. {}", file, e.toString());
            System.exit(CommandLine.ExitCode.SOFTWARE);
        }

        Sorter.sort(transactions);
        log.debug("{} transactions has been loaded", transactions.size());
        return transactions;
    }
}
