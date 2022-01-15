package com.remal.portfolio.parser.markdown;

import com.remal.portfolio.model.Currency;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.parser.Parser;
import com.remal.portfolio.util.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Transforms a Markdown file to list of transactions.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class MarkdownParser implements Parser {

    /**
     * CSV separator.
     */
    @Setter
    @Getter
    private String separator = "\\|";

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
     * @param file markdown file to be parsed
     * @param dateTimePattern pattern used for converting string to LocalDateTime
     */
    public MarkdownParser(String file, String dateTimePattern) {
        this.file = file;
        this.dateTimePattern = dateTimePattern;
    }

    /**
     * Builds the transaction list.
     */
    @Override
    public List<Transaction> parse() {
        log.debug("reading transactions from the '{}' markdown file...", file);
        List<Transaction> transactions = new ArrayList<>();

        try {
            int skipLeadingElements = hasHeader ? 2 : 0;
            try (Stream<String> stream = Files.lines(Paths.get(file))) {
                stream.skip(skipLeadingElements).forEach(line -> {
                    var fields = line.split(separator, -1);
                    Transaction t = Transaction
                            .builder()
                            .portfolio(fields[1].trim())
                            .ticker(fields[2].trim())
                            .type(TransactionType.getEnum(fields[3].trim()))
                            .created(Strings.toLocalDateTime(dateTimePattern, ZoneOffset.UTC, fields[4].trim()))
                            .volume(new BigDecimal(fields[5].trim()))
                            .price(new BigDecimal(fields[6].trim()))
                            .fee(new BigDecimal(fields[7].trim()))
                            .currency(Currency.getEnum(fields[8].trim()))
                            .orderId(fields[9].isBlank() ? null : fields[9].trim())
                            .tradeId(fields[10].isBlank() ? null : fields[10].trim())
                            .transferId(fields[11].isBlank() ? null : fields[11].trim())
                            .build();
                    transactions.add(t);
                });
            }
        } catch (IOException e) {
            log.error("Error while parsing the '{}' file. {}", file, e.toString());
            System.exit(CommandLine.ExitCode.SOFTWARE);
        }

        log.debug("{} transactions has been loaded", transactions.size());
        return transactions;
    }
}
