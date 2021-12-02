package com.remal.portfolio.writer;

import com.remal.portfolio.constant.Header;
import com.remal.portfolio.model.Currency;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.util.LocaleDateTimes;
import com.remal.portfolio.util.Strings;
import lombok.Setter;

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
public class LedgerWriter {

    /**
     * Default language.
     */
    @Setter
    private String language = "en";

    /**
     * Markdown table separator character.
     */
    @Setter
    private String tableSeparator = "|";

    /**
     * If it set to true then the header of the table will print.
     */
    @Setter
    private boolean printHeader = true;

    /**
     * Controls how the decimal numbers will be shown in the reports.
     */
    @Setter
    private String decimalFormat = "%8.4f";

    /**
     * Date pattern that is used to show timestamps in the reports.
     */
    @Setter
    private String dateTimePattern = "yyyy.MM.dd HH:mm:ss";

    /**
     * Timezone that is used when timestamp is rendered.
     */
    @Setter
    private String zoneIdAsString = "Europe/London";

    /**
     * List of the columns that will be displayed in the report.
     */
    @Setter
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
     * Generates a ledger report in Markdown format.
     *
     * @return the report as a Markdown string
     */
    public String printAsMarkdown() {
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
     * Writes the report to file.
     *
     * @param writeMode controls how to open the file
     * @param pathToFile the path of the file to be created
     */
    public void writeToFile(FileWriter.WriteMode writeMode, String pathToFile) {
        String report = printAsMarkdown();
        FileWriter.write(
                writeMode,
                pathToFile.contains("'") ? pathToFile : "'" + pathToFile + "'",
                zoneIdAsString,
                report);
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
                    value = (String) getter.invoke(transaction);

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
