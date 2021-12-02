package com.remal.portfolio.writer;

import com.remal.portfolio.constant.Header;
import com.remal.portfolio.model.Currency;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.LocaleDateTimes;
import com.remal.portfolio.util.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generate a ledger as a string.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class LedgerWriter {

    /**
     * Default language.
     */
    @Setter
    @Getter
    private String language = "en";

    /**
     * Markdown table separator character.
     */
    @Setter
    @Getter
    private String tableSeparator = "|";

    /**
     * If it set to true then the header of the table will print.
     */
    @Setter
    @Getter
    private boolean printHeader = true;

    /**
     * Controls how the decimal numbers will be shown in the reports.
     */
    @Setter
    @Getter
    private String decimalFormat = "%8.4f";

    /**
     * Date pattern that is used to show timestamps in the reports.
     */
    @Getter
    @Setter
    private String dateTimePattern = "yyyy.MM.dd HH:mm:ss";

    /**
     * Timezone that is used when timestamp is rendered.
     */
    @Getter
    @Setter
    private String zoneIdAsString = "Europe/London";

    /**
     * List of the columns that will be displayed in the report.
     */
    @Setter
    @Getter
    private List<Header> columnsToPrint = Arrays.asList(Header.TYPE, Header.TICKER, Header.CREATED, Header.VOLUME,
            Header.PRICE, Header.FEE, Header.CURRENCY);

    /**
     * The complete list of the transactions that the user has made and was filled.
     */
    private final List<Transaction> transactions;

    /**
     * Constructor.
     *
     * @param transactions the list of the transactions that the user has made and was filled
     */
    public LedgerWriter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    /**
     * Generates a ledger.
     *
     * @return the report as a Markdown string
     */
    public String printAsMarkdown() {
        log.debug("building the Ledger Markdown report...");
        var widths = calculateColumnWidth();
        var zoneId = ZoneId.of(zoneIdAsString);
        var sb = new StringBuilder();

        if (printHeader) {
            // header
            columnsToPrint
                    .forEach(column -> sb
                            .append(tableSeparator)
                            .append(Strings.leftPad(column.getValue(language), widths.get(column))));
            sb.append(tableSeparator).append(System.lineSeparator());

            // header separator
            columnsToPrint
                    .forEach(column -> sb
                            .append(tableSeparator)
                            .append("-".repeat(widths.get(column))));
            sb.append(tableSeparator).append(System.lineSeparator());
        }

        // data
        transactions
                .stream()
                .sorted(Comparator.comparing(Transaction::getCreated))
                .forEach(transaction -> {
                    columnsToPrint
                            .forEach(header -> sb
                                            .append(tableSeparator)
                                            .append(Strings.leftPad(
                                                    callGetter(header, transaction, zoneId), widths.get(header))
                                            ));
                    sb.append(tableSeparator).append(System.lineSeparator());
                });
        return sb.toString();
    }

    /**
     * Generates a ledger report.
     *
     * @return the report as a CSV content
     */
    public String printAsCsv() {
        log.debug("building the Ledger CSV report...");
        var zoneId = ZoneId.of(zoneIdAsString);
        var csvSeparator = ",";
        var sb = new StringBuilder();

        if (printHeader) {
            // header
            columnsToPrint
                    .forEach(column -> sb.append(column.getValue(language)).append(csvSeparator));
            sb.setLength(sb.length() - 1);
            sb.append(System.lineSeparator());
        }

        // data
        transactions
                .stream()
                .sorted(Comparator.comparing(Transaction::getCreated))
                .forEach(transaction -> {
                    columnsToPrint
                            .forEach(header -> sb
                                    .append(callGetter(header, transaction, zoneId).trim())
                                    .append(csvSeparator));
                    sb.setLength(sb.length() - 1);
                    sb.append(System.lineSeparator());
                });
        return sb.toString();
    }

    /**
     * Calculates the with of the columns in the Markdown reports.
     *
     * @return length of the columns
     */
    private Map<Header, Integer> calculateColumnWidth() {
        EnumMap<Header, Integer> widths = new EnumMap<>(Header.class);
        var zoneId = ZoneId.of(zoneIdAsString);

        // header length
        Header
                .stream()
                .forEach(header -> widths.put(header, header.getValue().length()));

        // data length
        transactions.forEach(transaction -> columnsToPrint.forEach(header -> {
            String value = Optional.ofNullable(callGetter(header, transaction, zoneId)).orElse("");
            var width = widths.getOrDefault(header, 0);
            widths.put(header, Math.max(width, value.length()));
        }));
        return widths;
    }

    private String callGetter(Header header, Transaction transaction, ZoneId zoneId) {
        String value = "";
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(Transaction.class, Object.class);
            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
            var descriptor = Arrays
                    .stream(descriptors)
                    .filter(d ->
                            d.getReadMethod()
                                    .getName()
                                    .toLowerCase()
                                    .replaceFirst("get", "")
                                    .equals(header.name().toLowerCase().replaceFirst("_", "")))
                    .findFirst();

            if (descriptor.isPresent()) {
                Method getter = descriptor.get().getReadMethod();
                Class<?> returnType = getter.getReturnType();

                if (String.class.equals(returnType)) {
                    value = (String) Optional.ofNullable(getter.invoke(transaction)).orElse("");

                } else if (TransactionType.class.equals(returnType)) {
                    var x = (TransactionType) getter.invoke(transaction);
                    value = x.name();

                } else if (LocalDateTime.class.equals(returnType)) {
                    var x = (LocalDateTime) getter.invoke(transaction);
                    value = LocaleDateTimes.toString(zoneId, dateTimePattern, x);

                } else if (BigDecimal.class.equals(returnType)) {
                    var x = (BigDecimal) getter.invoke(transaction);
                    value = BigDecimals.toString(decimalFormat, x);

                } else if (Currency.class.equals(returnType)) {
                    var x = (Currency) getter.invoke(transaction);
                    value = x.name();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }
}
