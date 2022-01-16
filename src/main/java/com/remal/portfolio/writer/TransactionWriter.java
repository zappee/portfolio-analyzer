package com.remal.portfolio.writer;

import com.remal.portfolio.i18n.Header;
import com.remal.portfolio.model.Currency;
import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.model.TransactionType;
import com.remal.portfolio.picocli.command.CommandCommon;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.util.Files;
import com.remal.portfolio.util.LocaleDateTimes;
import com.remal.portfolio.util.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
public class TransactionWriter {

    /**
     * Default language.
     */
    @Setter
    @Getter
    private String language;

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
    private String decimalFormat = "%16.8f";

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
    private List<Header> columnsToHide = new ArrayList<>();

    /**
     * The complete list of the transactions that the user has made and was filled.
     */
    private final List<Transaction> transactions;

    /**
     * Writes the list of the transactions to output.
     *
     * @param transactions list of transactions
     * @param outputCliGroup command line interface options
     * @param replaces list of the portfolio names that will be overridden
     * @return an initialized TransactionWriter instance
     * @throws java.lang.UnsupportedOperationException in case of usage of Excel file
     */
    public static TransactionWriter build(List<Transaction> transactions,
                                          CommandCommon.OutputGroup outputCliGroup,
                                          List<String> replaces) {

        log.debug("initializing report writer with '{}' language...", outputCliGroup.language);

        var writer = new TransactionWriter(transactions, replaces);
        writer.setPrintHeader(Boolean.parseBoolean(outputCliGroup.printHeader));
        writer.setLanguage(outputCliGroup.language);
        writer.setColumnsToHide(outputCliGroup.columnsToHide);
        writer.setDateTimePattern(outputCliGroup.dateTimePattern);
        return writer;
    }

    /**
     * Constructor.
     *
     * @param transactions the list of the transactions that the user has made and was filled
     * @param replaces list of the portfolio names that will be overridden
     */
    public TransactionWriter(List<Transaction> transactions, List<String> replaces) {
        this.transactions = transactions;
        renamePortfolioNames(replaces);
    }

    /**
     * Writes the report to file.
     *
     * @param writeMode controls how to open the file
     * @param file report file
     * @throws java.lang.UnsupportedOperationException throws if EXCEL file type is used
     */
    public void writeToFile(FileWriter.WriteMode writeMode, String file) {
        var filetype = Files.getFileType(file);
        switch (filetype) {
            case TEXT:
                FileWriter.write(writeMode, file, printAsMarkdown());
                break;

            case EXCEL:
                throw new UnsupportedOperationException();

            case CSV:
                FileWriter.write(writeMode, file, printAsCsv());
                break;

            default:
                log.error("Unsupported output file type: '{}'", file);
                System.exit(CommandLine.ExitCode.SOFTWARE);
        }
    }

    /**
     * Generates the transaction report.
     *
     * @return the report as a Markdown string
     */
    public String printAsMarkdown() {
        log.debug("building the Markdown report with {} transactions...", transactions.size());
        var widths = calculateColumnWidth();
        var zoneId = ZoneId.of(zoneIdAsString);
        var sb = new StringBuilder();

        if (printHeader) {
            // header
            Header.ALL
                    .stream().filter(x -> !columnsToHide.contains(x))
                    .forEach(column -> sb
                            .append(tableSeparator)
                            .append(Strings.leftPad(column.getValue(language), widths.get(column))));
            sb.append(tableSeparator).append(System.lineSeparator());

            // header separator
            Header.ALL
                    .stream().filter(x -> !columnsToHide.contains(x))
                    .forEach(column -> sb
                            .append(tableSeparator)
                            .append("-".repeat(widths.get(column))));
            sb.append(tableSeparator).append(System.lineSeparator());
        }

        // data
        transactions
                .forEach(transaction -> {
                    Header.ALL
                            .stream().filter(x -> !columnsToHide.contains(x))
                            .forEach(header -> sb
                                    .append(tableSeparator)
                                    .append(
                                            Strings.leftPad(
                                                    callGetter(header, transaction, zoneId).orElse(null),
                                                    widths.get(header))
                                    ));
                    sb.append(tableSeparator).append(System.lineSeparator());
                });
        return sb.toString();
    }

    /**
     * Generates the transaction report.
     *
     * @return the report as a CSV content
     */
    private String printAsCsv() {
        log.debug("building the Ledger CSV report...");
        var zoneId = ZoneId.of(zoneIdAsString);
        var csvSeparator = ",";
        var sb = new StringBuilder();

        if (printHeader) {
            // header
            Header.ALL
                    .stream().filter(x -> !columnsToHide.contains(x))
                    .forEach(column -> sb.append(column.getValue(language)).append(csvSeparator));
            sb.setLength(sb.length() - 1);
            sb.append(System.lineSeparator());
        }

        // data
        transactions
                .forEach(transaction -> {
                    Header.ALL
                            .stream()
                            .filter(x -> !columnsToHide.contains(x))
                            .forEach(header -> sb
                                    .append(callGetter(header, transaction, zoneId).orElse(""))
                                    .append(csvSeparator));
                    sb.setLength(sb.length() - 1);
                    sb.append(System.lineSeparator());
                });
        return sb.toString();
    }

    /**
     * Initialize the list of the portfolio names that will be overridden
     * during the parse.
     *
     * @param replaces user defined values from the command line interface
     */
    private void renamePortfolioNames(List<String> replaces) {
        Map<String, String> portfolioNameToRename = new HashMap<>();
        try {
            replaces.forEach(x -> {
                var from = x.split(":")[0];
                var to = x.split(":")[1];
                log.debug("portfolio name overwriting: '{}' -> '{}'", from, to);
                portfolioNameToRename.put(from, to);
            });

            portfolioNameToRename.forEach((k, v) -> transactions
                    .stream()
                    .filter(x -> x.getPortfolio().equals(k)).forEach(x -> x.setPortfolio(v)));

        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("Invalid value provided for '-map' option.");
            System.exit(CommandLine.ExitCode.SOFTWARE);
        }
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
                .forEach(header -> widths.put(header, header.getValue(language).length()));

        // data length
        transactions.forEach(transaction ->
                Header.ALL
                        .stream().filter(x -> !columnsToHide.contains(x))
                        .forEach(header -> {
                            var value = callGetter(header, transaction, zoneId).orElse("");
                            var width = widths.getOrDefault(header, 0);
                            widths.put(header, Math.max(width, value.length()));
                        })
        );
        return widths;
    }

    /**
     * Returns the value of property of the transaction. This method uses
     * reflection to read the property.
     *
     * @param header the name of the property
     * @param transaction the object on which the getter method will be called
     * @param zoneId used to convert date-time to string
     * @return the value of the property
     */
    private Optional<String> callGetter(Header header, Transaction transaction, ZoneId zoneId) {
        Optional<String> value = Optional.empty();
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
                    value = Optional.ofNullable((String) getter.invoke(transaction));

                } else if (TransactionType.class.equals(returnType)) {
                    var x = (TransactionType) getter.invoke(transaction);
                    value = Objects.isNull(x) ? Optional.empty() : Optional.of(x.name());

                } else if (LocalDateTime.class.equals(returnType)) {
                    var x = (LocalDateTime) getter.invoke(transaction);
                    value = Optional.ofNullable(LocaleDateTimes.toString(zoneId, dateTimePattern, x));

                } else if (BigDecimal.class.equals(returnType)) {
                    var x = (BigDecimal) getter.invoke(transaction);
                    value = Optional.of(BigDecimals.toString(decimalFormat, x).trim());

                } else if (Currency.class.equals(returnType)) {
                    var x = (Currency) getter.invoke(transaction);
                    value = Objects.isNull(x) ? Optional.empty() : Optional.of(x.name());
                }
            }
        } catch (Exception e) {
            log.warn(e.toString());
            System.exit(CommandLine.ExitCode.SOFTWARE);
        }

        return value;
    }
}
