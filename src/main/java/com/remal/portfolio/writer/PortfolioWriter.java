package com.remal.portfolio.writer;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.FileType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.MultiplicityType;
import com.remal.portfolio.model.PortfolioReport;
import com.remal.portfolio.parser.PortfolioSummaryParser;
import com.remal.portfolio.picocli.arggroup.PortfolioArgGroup;
import com.remal.portfolio.picocli.arggroup.PortfolioInputArgGroup;
import com.remal.portfolio.util.BigDecimalFormatter;
import com.remal.portfolio.util.BigDecimals;
import com.remal.portfolio.util.FileWriter;
import com.remal.portfolio.util.Files;
import com.remal.portfolio.util.Filter;
import com.remal.portfolio.util.LocalDateTimes;
import com.remal.portfolio.util.Sorter;
import com.remal.portfolio.util.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Portfolio summary writer.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class PortfolioWriter extends Writer<PortfolioReport> {

    /**
     * Prefix to be removed from the label id.
     */
    private static final String PREFIX_TO_REMOVE = "LABEL_";

    /**
     * Markdown unordered list.
     */
    private static final String MARKDOWN_LIST = "* ";

    /**
     * Markdown horizontal line.
     */
    private static final String MARKDOWN_HR = Strings.repeat('-', 52) + NEW_LINE;

    /**
     * Decimal number formatter.
     */
    private BigDecimalFormatter decimalFormatter;

    /**
     * The currency of the portfolio report.
     */
    @Setter
    private CurrencyType baseCurrency;

    /**
     * Set the product name filter.
     */
    @Setter
    private List<String> symbolsToShow = new ArrayList<>();

    /**
     * Controls the price export to file.
     */
    @Setter
    private MultiplicityType multiplicity;

    /**
     * Builder that initializes a new writer instance.
     *
     * @param inputArgGroup the input CLI group
     * @param outputArgGroup the output CLI group
     * @return the writer instance
     */
    public static PortfolioWriter build(PortfolioInputArgGroup inputArgGroup,
                                        PortfolioArgGroup.OutputArgGroup outputArgGroup) {

        var writer = new PortfolioWriter();
        writer.setBaseCurrency(CurrencyType.getEnum(outputArgGroup.getBaseCurrency()));
        writer.setSymbolsToShow(inputArgGroup.getSymbols());
        writer.setHideTitle(outputArgGroup.isHideTitle());
        writer.setHideHeader(outputArgGroup.isHideHeader());
        writer.setLanguage(outputArgGroup.getLanguage());
        writer.setDateTimePattern(outputArgGroup.getDateTimePattern());
        writer.setInputZone(ZoneId.of(inputArgGroup.getZone()));
        writer.setOutputZone(ZoneId.of(outputArgGroup.getZone()));
        writer.setColumnsToHide(outputArgGroup.getColumnsToHide().stream().map(String::toUpperCase).toList());
        writer.setMultiplicity(outputArgGroup.getMultiplicity());
        return writer;
    }

    /**
     * Write the report to the file.
     *
     * @param writeMode control the way of open the file
     * @param filename the report file name
     * @param portfolioReport the report
     */
    public void writePortfolioReport(FileWriter.WriteMode writeMode,
                                     String filename,
                                     final PortfolioReport portfolioReport) {
        if (Objects.nonNull(filename) && Files.getFileType(filename) == FileType.CSV) {
            log.debug("> writing the portfolio report to \"{}\", write-mode: {}...", filename, writeMode);
            generatePortfolioCsvReport(writeMode, filename, portfolioReport);
        } else {
            log.warn("> skipping the portfolio report generation: filename is empty or file type is not supported");
        }
    }

    /**
     * Generates the CSV report.
     *
     * @param items data
     * @return the report content as a String
     */
    @Override
    protected String buildCsvReport(List<PortfolioReport> items) {
        var portfolioReport = items
                .stream()
                .findFirst()
                .orElse(new PortfolioReport(CurrencyType.EUR, LocalDateTime.now()));
        var report = new StringBuilder();

        // report title
        if (!hideTitle) {
            report
                .append(Label.TITLE_PORTFOLIO_SUMMARY.getLabel(language))
                .append(NEW_LINE)
                .append(Label.TITLE_GENERATED.getLabel(language)).append(": ")
                .append(LocalDateTimes.toNullSafeString(outputZone, dateTimePattern, portfolioReport.getGenerated()))
                .append(NEW_LINE)
                .append(Label.TITLE_BASE_CURRENCY.getLabel(language).replace("{0}", language))
                .append(NEW_LINE);

        }

        // table header
        if (!hideHeader) {
            LabelCollection.PORTFOLIO_TABLE_HEADERS
                    .forEach(labelKey -> report
                            .append(labelKey.getLabel(language))
                            .append(csvSeparator));

            report.setLength(report.length() - csvSeparator.length());
            report.append(NEW_LINE);
        }

        // data
        portfolioReport.getPortfolios().forEach((name, portfolio) -> portfolio.getProducts()
                .forEach((key, product) -> {
                    var profitAndLossPercent = product.getProfitAndLossPercent();
                    if (BigDecimals.isNotZero(product.getQuantity())) {
                        var price = product.getMarketPrice().getUnitPrice();
                        var cost = exchangeAndSum(portfolioReport.getExchangeRates(), product.getFees());
                        report
                            .append(getCell(Label.HEADER_PORTFOLIO, portfolio.getName(), csvSeparator))
                            .append(getCell(Label.HEADER_SYMBOL, product.getSymbol(), csvSeparator))
                            .append(getCell(Label.HEADER_QUANTITY, product.getQuantity(), csvSeparator))
                            .append(getCell(Label.HEADER_AVG_PRICE, product.getAveragePrice(), csvSeparator))
                            .append(getCell(Label.HEADER_MARKET_UNIT_PRICE, price, csvSeparator))
                            .append(getCell(Label.HEADER_MARKET_VALUE, product.getMarketValue(), csvSeparator))
                            .append(getCell(Label.HEADER_INVESTED_AMOUNT, product.getInvestedAmount(), csvSeparator))
                            .append(getCell(Label.HEADER_PROFIT_LOSS, product.getProfitAndLoss(), csvSeparator))
                            .append(getCell(Label.HEADER_PROFIT_LOSS_PERCENT, profitAndLossPercent, csvSeparator))
                            .append(getCell(Label.HEADER_COSTS, cost, csvSeparator))
                            .append(getCell(Label.HEADER_DEPOSITS, product.getDeposits(), csvSeparator))
                            .append(getCell(Label.HEADER_WITHDRAWALS, product.getWithdrawals(), csvSeparator))
                            .append(NEW_LINE);
                    }
                })
        );
        return report.toString();
    }

    /**
     * Generate the Text/Markdown report.
     *
     * @param items data
     * @return the report content as a String
     */
    @Override
    protected String buildMarkdownReport(List<PortfolioReport> items) {
        var portfolioReport = items
                .stream()
                .findFirst()
                .orElse(new PortfolioReport(CurrencyType.EUR, LocalDateTime.now()));
        decimalFormatter = initializeDecimalFormatter(portfolioReport);
        var report = new StringBuilder();
        var widths = calculateColumnWidth(portfolioReport);

        Sorter.sortPortfolioReport(portfolioReport);

        // report title
        if (!hideTitle) {
            report.append(generateMarkdownTitle(portfolioReport.getGenerated()));
        }

        // table header
        if (!hideHeader) {
            report.append(generateHeader(widths));
        }

        // data
        portfolioReport.getPortfolios().forEach((portfolioName, portfolio) ->
                portfolio.getProducts()
                        .entrySet()
                        .stream()
                        .filter(p -> symbolsToShow.isEmpty() || symbolsToShow.contains(p.getValue().getSymbol().trim()))
                        .forEach(p -> {
                            var product = p.getValue();
                            var profitAndLossPercent = product.getProfitAndLossPercent();

                            if (BigDecimals.isNotZero(product.getQuantity())) {
                                var marketPrice = product.getMarketPrice();
                                var price = Objects.isNull(marketPrice) ? null : marketPrice.getUnitPrice();
                                var cost = exchangeAndSum(portfolioReport.getExchangeRates(), product.getFees());
                                report
                                    .append(getCell(Label.HEADER_PORTFOLIO, portfolio.getName(), widths))
                                    .append(getCell(Label.HEADER_SYMBOL, product.getSymbol(), widths))
                                    .append(getCell(Label.HEADER_QUANTITY, product.getQuantity(), widths))
                                    .append(getCell(Label.HEADER_AVG_PRICE, product.getAveragePrice(), widths))
                                    .append(getCell(Label.HEADER_MARKET_UNIT_PRICE, price, widths))
                                    .append(getCell(Label.HEADER_MARKET_VALUE, product.getMarketValue(), widths))
                                    .append(getCell(Label.HEADER_INVESTED_AMOUNT, product.getInvestedAmount(), widths))
                                    .append(getCell(Label.HEADER_PROFIT_LOSS, product.getProfitAndLoss(), widths))
                                    .append(getCell(Label.HEADER_PROFIT_LOSS_PERCENT, profitAndLossPercent, widths))
                                    .append(getCell(Label.HEADER_COSTS, cost, widths))
                                    .append(getCell(Label.HEADER_DEPOSITS, product.getDeposits(), widths))
                                    .append(getCell(Label.HEADER_WITHDRAWALS, product.getWithdrawals(), widths))
                                    .append(markdownSeparator)
                                    .append(NEW_LINE);
                            }
                        })
        );

        // totals
        report.append(generatePortfolioSummaryMarkdownReport(portfolioReport));

        return report.toString();
    }


    /**
     * Get history data from file.
     *
     * @param filename data file name
     */
    @Override
    protected List<PortfolioReport> getHistoryFromFile(String filename) {
        return new ArrayList<>();
    }

    /**
     * Generates the portfolio summary CSV report.
     *
     * @param writeMode control the way of open the file
     * @param filename the report file name
     * @param portfolioReport portfolio report
     */
    private void generatePortfolioCsvReport(FileWriter.WriteMode writeMode,
                                            String filename,
                                            final PortfolioReport portfolioReport) {

        var inputArgGroup = buildTransactionParserInputArgGroup(filename);
        var parser = PortfolioSummaryParser.build(baseCurrency, language, inputArgGroup);
        var portfolioReports = new ArrayList<>(parser.parse(filename));

        if (!portfolioReports.contains(portfolioReport)) {
            portfolioReports.add(portfolioReport);
        }
        reduceBasedOnMultiplicity(portfolioReports);

        LinkedHashMap<Label, Set<String>> columnInfo = new LinkedHashMap<>();
        portfolioReports.forEach(report -> {
            // cash
            var placeholderValues = columnInfo.computeIfAbsent(
                    Label.LABEL_TOTAL_CASH_PER_CURRENCY,
                    x -> new LinkedHashSet<>());
            placeholderValues.addAll(report.getCashInPortfolio().keySet());
            columnInfo.put(Label.LABEL_TOTAL_CASH, Set.of(baseCurrency.name()));

            // exchange rates
            placeholderValues = columnInfo.computeIfAbsent(
                    Label.LABEL_TOTAL_EXCHANGE_RATE,
                    x -> new LinkedHashSet<>());
            placeholderValues.addAll(report.getExchangeRates().keySet());

            // deposits
            placeholderValues = columnInfo.computeIfAbsent(
                    Label.LABEL_TOTAL_DEPOSIT_PER_CURRENCY,
                    x -> new LinkedHashSet<>());
            placeholderValues.addAll(report.getDeposits().keySet());
            columnInfo.put(Label.LABEL_TOTAL_DEPOSIT, Set.of(baseCurrency.name()));

            // withdrawals
            placeholderValues = columnInfo.computeIfAbsent(
                    Label.LABEL_TOTAL_WITHDRAWAL_PER_CURRENCY,
                    x -> new LinkedHashSet<>());
            placeholderValues.addAll(report.getWithdrawals().keySet());
            columnInfo.put(Label.LABEL_TOTAL_WITHDRAWAL, Set.of(baseCurrency.name()));

            // investments
            placeholderValues = columnInfo.computeIfAbsent(
                    Label.LABEL_TOTAL_INVESTMENT_PER_CURRENCY,
                    x -> new LinkedHashSet<>());
            placeholderValues.addAll(report.getInvestments().keySet());
            columnInfo.put(Label.LABEL_TOTAL_INVESTMENT, Set.of(baseCurrency.name()));

            // market values
            placeholderValues = columnInfo.computeIfAbsent(
                    Label.LABEL_TOTAL_MARKET_VALUE_PER_CURRENCY,
                    x -> new LinkedHashSet<>());
            placeholderValues.addAll(report.getMarketValues().keySet());
            columnInfo.put(Label.LABEL_TOTAL_MARKET_VALUE, Set.of(baseCurrency.name()));

            // profits / losses
            placeholderValues = columnInfo.computeIfAbsent(
                    Label.LABEL_TOTAL_PROFIT_LOSS_PER_CURRENCY,
                    x -> new LinkedHashSet<>());
            placeholderValues.addAll(report.getProfitLoss().keySet());
            columnInfo.put(Label.LABEL_TOTAL_PROFIT_LOSS, Set.of(baseCurrency.name()));
        });

        var report = generatePortfolioCsvReportHeader(columnInfo);
        report += generatePortfolioCsvReportData(columnInfo, portfolioReports);

        FileWriter.write(writeMode, filename, report.getBytes());
    }

    /**
     * Remove items from the list based on the value of the multiplicity.
     *
     * @param portfolioReports the list to deduce
     */
    private void reduceBasedOnMultiplicity(ArrayList<PortfolioReport> portfolioReports) {
        log.debug("> multiplicity: {}", multiplicity.name());
        log.debug("> number of items before the reduce: {}", portfolioReports.size());

        portfolioReports.sort(Sorter.portfolioReportComparator());

        List<PortfolioReport> reducedPortfolioReports = new LinkedList<>();
        portfolioReports.forEach(portfolioReport -> {
            var generated = portfolioReport.getGenerated();
            var withoutRange = withoutRange(reducedPortfolioReports, generated);
            if (withoutRange) {
                reducedPortfolioReports.add(portfolioReport);
            }
        });

        portfolioReports.clear();
        portfolioReports.addAll(reducedPortfolioReports);
        log.debug("> number of items after the reduce: {}", portfolioReports.size());
    }

    /**
     * Checks whether the given date has been added to the list or not.
     *
     * @param portfolioReports the list with the records
     * @param dateToCheck the date to check
     * @return true if the date is not in the list
     */
    private boolean withoutRange(List<PortfolioReport> portfolioReports, LocalDateTime dateToCheck) {
        var withinRange = portfolioReports
                .stream()
                .filter(x -> x.getGenerated().plusSeconds(multiplicity.getRangeLengthInSec()).isAfter(dateToCheck))
                .count();
        return withinRange == 0;
    }

    /**
     * Generates report header.
     *
     * @param columnInfo the map that contains info about the report columns
     * @return the header
     */
    private String generatePortfolioCsvReportHeader(LinkedHashMap<Label, Set<String>> columnInfo) {
        var sb = new StringBuilder();
        sb.append(Label.HEADER_REQUEST_DATE.getLabel(language)).append(csvSeparator);

        columnInfo.forEach((label, values) ->
                values.forEach(value -> sb
                        .append(label.getLabel(language).replace("{0}", value))
                        .append(csvSeparator))
        );
        sb.setLength(sb.length() - csvSeparator.length());
        sb.append(NEW_LINE);
        return sb.toString();
    }

    /**
     * Generates CSV data.
     *
     * @param columnInfo the map that contains info about the report columns
     * @param portfolioReports portfolio report
     * @return the report
     */
    private String generatePortfolioCsvReportData(LinkedHashMap<Label, Set<String>> columnInfo,
                                                  List<PortfolioReport> portfolioReports) {
        decimalFormat = BigDecimals.UNFORMATTED;
        var sb = new StringBuilder();

        portfolioReports.forEach(reportEntry -> {
            sb.append(getStringValue(reportEntry.getGenerated()).map(x -> x + csvSeparator).orElse(csvSeparator));

            // cash
            var label = Label.LABEL_TOTAL_CASH_PER_CURRENCY;
            enrichMapValue(columnInfo.get(label), reportEntry.getCashInPortfolio());
            columnInfo.get(label).forEach(symbol -> showBigDecimal(sb, reportEntry.getCashInPortfolio().get(symbol)));
            var sum = exchangeAndSum(reportEntry.getExchangeRates(), reportEntry.getCashInPortfolio());
            sb.append(getStringValue(sum).map(x -> x + csvSeparator).orElse(csvSeparator));

            // exchange rates
            label = Label.LABEL_TOTAL_EXCHANGE_RATE;
            enrichMapValue(columnInfo.get(label), reportEntry.getExchangeRates());
            columnInfo.get(label).forEach(symbol -> showBigDecimal(sb, reportEntry.getExchangeRates().get(symbol)));

            // deposits
            label = Label.LABEL_TOTAL_DEPOSIT_PER_CURRENCY;
            enrichMapValue(columnInfo.get(label), reportEntry.getDeposits());
            columnInfo.get(label).forEach(symbol -> showBigDecimal(sb, reportEntry.getDeposits().get(symbol)));
            sum = exchangeAndSum(reportEntry.getExchangeRates(), reportEntry.getDeposits());
            sb.append(getStringValue(sum).map(x -> x + csvSeparator).orElse(csvSeparator));

            // withdrawals
            label = Label.LABEL_TOTAL_WITHDRAWAL_PER_CURRENCY;
            enrichMapValue(columnInfo.get(label), reportEntry.getWithdrawals());
            columnInfo.get(label).forEach(symbol -> showBigDecimal(sb, reportEntry.getWithdrawals().get(symbol)));
            sum = exchangeAndSum(reportEntry.getExchangeRates(), reportEntry.getWithdrawals());
            sb.append(getStringValue(sum).map(x -> x + csvSeparator).orElse(csvSeparator));

            // investments
            label = Label.LABEL_TOTAL_INVESTMENT_PER_CURRENCY;
            enrichMapValue(columnInfo.get(label), reportEntry.getInvestments());
            columnInfo.get(label).forEach(symbol -> showBigDecimal(sb, reportEntry.getInvestments().get(symbol)));
            sum = exchangeAndSum(reportEntry.getExchangeRates(), reportEntry.getInvestments());
            sb.append(getStringValue(sum).map(x -> x + csvSeparator).orElse(csvSeparator));

            // market values
            label = Label.LABEL_TOTAL_MARKET_VALUE_PER_CURRENCY;
            enrichMapValue(columnInfo.get(label), reportEntry.getMarketValues());
            columnInfo.get(label).forEach(symbol -> showBigDecimal(sb, reportEntry.getMarketValues().get(symbol)));
            sum = exchangeAndSum(reportEntry.getExchangeRates(), reportEntry.getMarketValues());
            sb.append(getStringValue(sum).map(x -> x + csvSeparator).orElse(csvSeparator));

            // profits/losses
            label = Label.LABEL_TOTAL_PROFIT_LOSS_PER_CURRENCY;
            enrichMapValue(columnInfo.get(label), reportEntry.getProfitLoss());
            columnInfo.get(label).forEach(symbol -> showBigDecimal(sb, reportEntry.getProfitLoss().get(symbol)));
            sum = exchangeAndSum(reportEntry.getExchangeRates(), reportEntry.getProfitLoss());
            sb.append(getStringValue(sum).map(x -> x + csvSeparator).orElse(csvSeparator));
            sb.setLength(sb.length() - csvSeparator.length());
            sb.append(NEW_LINE);
        });

        return sb.toString();
    }

    /**
     * Adds the decimal value to the CSV report.
     *
     * @param sb the CSV report
     * @param value value to add
     */
    private void showBigDecimal(StringBuilder sb, BigDecimal value) {
        sb.append(getStringValue(value).map(x -> x + csvSeparator).orElse(csvSeparator));
    }

    /**
     * Copy items from a Set to a Map.
     *
     * @param source the source items
     * @param target the target items
     */
    private void enrichMapValue(Set<String> source, Map<String, BigDecimal> target) {
        source.forEach(value -> {
            if (!target.containsKey(value)) {
                target.put(value, null);
            }
        });
    }

    /**
     * Generates the report summary.
     *
     * @param portfolioReport the report
     * @return the report summary
     */
    private StringBuilder generatePortfolioSummaryMarkdownReport(PortfolioReport portfolioReport) {
        var emptyLabel = Label.HEADER_EMPTY;
        var labelWidth = LabelCollection.PRODUCT_SUMMARY_FOOTER
                .stream()
                .max(Comparator.comparingInt(x -> x.getLabel(language).length()))
                .orElse(emptyLabel).getLabel(language)
                .length();
        var sb = new StringBuilder();

        if (!columnsToHide.contains(Label.LABEL_TOTAL_CASH.name().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(NEW_LINE)
                    .append(showSummaryPerCurrencyAndTotal(
                            portfolioReport.getExchangeRates(),
                            portfolioReport.getCashInPortfolio(),
                            Label.LABEL_TOTAL_CASH_PER_CURRENCY,
                            Label.LABEL_TOTAL_CASH,
                            labelWidth));
        }

        if (!columnsToHide.contains(Label.LABEL_TOTAL_EXCHANGE_RATE.name().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(showMapValue(portfolioReport.getExchangeRates(), labelWidth));
        }

        if (!columnsToHide.contains(Label.LABEL_TOTAL_DEPOSIT.name().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(showSummaryPerCurrencyAndTotal(
                            portfolioReport.getExchangeRates(),
                            portfolioReport.getDeposits(),
                            Label.LABEL_TOTAL_DEPOSIT_PER_CURRENCY,
                            Label.LABEL_TOTAL_DEPOSIT,
                            labelWidth));
        }

        if (!columnsToHide.contains(Label.LABEL_TOTAL_WITHDRAWAL.name().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(showSummaryPerCurrencyAndTotal(
                            portfolioReport.getExchangeRates(),
                            portfolioReport.getWithdrawals(),
                            Label.LABEL_TOTAL_WITHDRAWAL_PER_CURRENCY,
                            Label.LABEL_TOTAL_WITHDRAWAL,
                            labelWidth));
        }

        if (!columnsToHide.contains(Label.LABEL_TOTAL_INVESTMENT.name().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(showSummaryPerCurrencyAndTotal(
                            portfolioReport.getExchangeRates(),
                            portfolioReport.getInvestments(),
                            Label.LABEL_TOTAL_INVESTMENT_PER_CURRENCY,
                            Label.LABEL_TOTAL_INVESTMENT,
                            labelWidth));
        }

        if (!columnsToHide.contains(Label.LABEL_TOTAL_MARKET_VALUE.name().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(showSummaryPerCurrencyAndTotal(
                            portfolioReport.getExchangeRates(),
                            portfolioReport.getMarketValues(),
                            Label.LABEL_TOTAL_MARKET_VALUE_PER_CURRENCY,
                            Label.LABEL_TOTAL_MARKET_VALUE,
                            labelWidth));
        }

        if (!columnsToHide.contains(Label.LABEL_TOTAL_PROFIT_LOSS.name().replace(PREFIX_TO_REMOVE, ""))) {
            sb
                    .append(showSummaryPerCurrencyAndTotal(
                            portfolioReport.getExchangeRates(),
                            portfolioReport.getProfitLoss(),
                            Label.LABEL_TOTAL_PROFIT_LOSS_PER_CURRENCY,
                            Label.LABEL_TOTAL_PROFIT_LOSS,
                            labelWidth));
        }

        return sb;
    }

    /**
     * Get the length of the longest decimal value from the footer.
     *
     * @param portfolioReport portfolio report
     * @return the length of the longest decimal value in the footer
     */
    private BigDecimalFormatter initializeDecimalFormatter(PortfolioReport portfolioReport) {
        var formatter = new BigDecimalFormatter(decimalFormat, decimalGroupingSeparator);

        portfolioReport.getPortfolios().forEach((name, portfolio) ->
                portfolio.getProducts().forEach((ticker, product) -> {
                    if (CurrencyType.isValid(ticker)) {
                        var exchangeRateTicker = ticker + "-" + baseCurrency;
                        var rate = portfolioReport.getExchangeRates().get(exchangeRateTicker);
                        updateFieldMaxLength(product.getDeposits(), formatter, rate);
                        updateFieldMaxLength(product.getWithdrawals(), formatter, rate);
                        updateFieldMaxLength(product.getInvestedAmount(), formatter, rate);
                        updateFieldMaxLength(product.getMarketValue(), formatter, rate);
                        updateFieldMaxLength(product.getProfitAndLoss(), formatter, rate);
                    }
                })
        );

        portfolioReport.getExchangeRates().forEach(formatter.getBiConsumer(BigDecimals.SCALE_FOR_EXCHANGE_RATE));
        portfolioReport.getCashInPortfolio().forEach(formatter.getBiConsumer(BigDecimals.SCALE_DEFAULT));
        portfolioReport.getDeposits().forEach(formatter.getBiConsumer(BigDecimals.SCALE_DEFAULT));
        portfolioReport.getWithdrawals().forEach(formatter.getBiConsumer(BigDecimals.SCALE_DEFAULT));
        portfolioReport.getInvestments().forEach(formatter.getBiConsumer(BigDecimals.SCALE_DEFAULT));
        portfolioReport.getMarketValues().forEach(formatter.getBiConsumer(BigDecimals.SCALE_DEFAULT));
        portfolioReport.getProfitLoss().forEach(formatter.getBiConsumer(BigDecimals.SCALE_DEFAULT));

        // summaries
        BigDecimal sum = exchangeAndSum(portfolioReport.getExchangeRates(), portfolioReport.getCashInPortfolio());
        updateFieldMaxLength(sum, formatter, BigDecimal.ONE);

        sum = exchangeAndSum(portfolioReport.getExchangeRates(), portfolioReport.getDeposits());
        updateFieldMaxLength(sum, formatter, BigDecimal.ONE);

        sum = exchangeAndSum(portfolioReport.getExchangeRates(), portfolioReport.getWithdrawals());
        updateFieldMaxLength(sum, formatter, BigDecimal.ONE);

        sum = exchangeAndSum(portfolioReport.getExchangeRates(), portfolioReport.getInvestments());
        updateFieldMaxLength(sum, formatter, BigDecimal.ONE);

        sum = exchangeAndSum(portfolioReport.getExchangeRates(), portfolioReport.getMarketValues());
        updateFieldMaxLength(sum, formatter, BigDecimal.ONE);

        sum = exchangeAndSum(portfolioReport.getExchangeRates(), portfolioReport.getProfitLoss());
        updateFieldMaxLength(sum, formatter, BigDecimal.ONE);

        return formatter;
    }

    /**
     * Updates the maximum length of the fields in the markdown report.
     *
     * @param valueToExchange the value that will be exchange to the base currency
     * @param formatter the BigDecimal formatter
     * @param exchangeRate exchange rate used to convert values to the base currency
     */
    private void updateFieldMaxLength(BigDecimal valueToExchange,
                                      BigDecimalFormatter formatter,
                                      BigDecimal exchangeRate) {
        if (Objects.nonNull(valueToExchange) && Objects.nonNull(exchangeRate)) {
            var valueInBaseCurrency = valueToExchange.multiply(exchangeRate);
            formatter.updateFieldMaxLength(valueInBaseCurrency, BigDecimals.SCALE_FOR_EXCHANGE_RATE);
        }
    }

    /**
     * Generates a footer content.
     *
     * @param valuesToShow portfolio report
     * @param labelWidth label will align left on this
     * @return the footer content
     */
    private StringBuilder showMapValue(Map<String, BigDecimal> valuesToShow, int labelWidth) {
        return new StringBuilder()
                .append(valuesToShow.isEmpty() ? "" : MARKDOWN_HR)
                .append(mapToString(
                        Label.LABEL_TOTAL_EXCHANGE_RATE,
                        labelWidth,
                        valuesToShow,
                        BigDecimals.SCALE_FOR_EXCHANGE_RATE));
    }

    /**
     * Generates a footer content.
     *
     * @param rates exchange rates
     * @param valuesToSum values to sum
     * @param labelForCurrency currency label
     * @param labelForTotal total label
     * @param labelWidth label will align left on this
     * @return the footer content
     */
    private StringBuilder showSummaryPerCurrencyAndTotal(Map<String, BigDecimal> rates,
                                                         Map<String, BigDecimal> valuesToSum,
                                                         Label labelForCurrency,
                                                         Label labelForTotal,
                                                         int labelWidth) {
        var sb = new StringBuilder();
        if (!valuesToSum.isEmpty()) {
            var labelAsString = labelForTotal.getLabel(language).replace("{0}", baseCurrency.name());
            var value = mapToString(labelForCurrency, labelWidth, valuesToSum, BigDecimals.SCALE_DEFAULT);

            if (!value.isEmpty()) {
                sb
                        .append(sb.isEmpty() ? "" : MARKDOWN_HR)
                        .append(mapToString(labelForCurrency, labelWidth, valuesToSum, BigDecimals.SCALE_DEFAULT));

                if (!rates.isEmpty()) {
                    var sum = exchangeAndSum(rates, valuesToSum);
                    sb
                            .append(MARKDOWN_LIST).append(labelAsString)
                            .append(": ")
                            .append(Strings.space(labelWidth - labelAsString.length()))
                            .append(Objects.isNull(sum) ? "" : decimalFormatter.format(sum, BigDecimals.SCALE_DEFAULT))
                            .append(NEW_LINE);
                }
            }
        }
        return sb;
    }

    /**
     * Exchange the currencies to the base currency and sum up them.
     *
     * @param rates exchange rates
     * @param valuesToSum values to sum
     * @return summed value in base currency
     */
    private BigDecimal exchangeAndSum(Map<String, BigDecimal> rates,
                                      Map<String, BigDecimal> valuesToSum) {

        if (rates.isEmpty()) {
            return null;
        }

        AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);
        valuesToSum.forEach((symbol, value) -> {
            var quantity = BigDecimals.isNullOrZero(value) ? BigDecimal.ZERO : value;
            var exchangeRateSymbol = symbol + "-" + baseCurrency;
            var exchangeRate = rates.get(exchangeRateSymbol);
            if (Objects.isNull(exchangeRate)) {
                total.set(total.get().add(quantity));
            } else {
                total.set(total.get().add(quantity.multiply(exchangeRate)));
            }
        });

        return total.get();
    }

    /**
     * Converts HashMap to String.
     *
     * @param label label for the value
     * @param labelWidth the with of the label
     * @param map portfolio report
     * @param scale scale of the BigDecimal value to be returned
     * @return the report content
     */
    private StringBuilder mapToString(Label label, int labelWidth, Map<String, BigDecimal> map, int scale) {
        var sb = new StringBuilder();
        map
                .entrySet()
                .stream()
                .filter(entry -> BigDecimals.isNotNullAndNotZero(entry.getValue()))
                .forEach(entry -> {
                    var labelAsString = label.getLabel(language).replace("{0}", entry.getKey());
                    var decimalValue = entry.getValue().setScale(scale, BigDecimals.ROUNDING_MODE);
                    var decimalValueAsString = decimalFormatter.format(decimalValue, scale);
                    sb
                            .append(MARKDOWN_LIST)
                            .append(labelAsString).append(": ")
                            .append(Strings.space(labelWidth - labelAsString.length()))
                            .append(decimalValueAsString).append(NEW_LINE);
                });
        return sb;
    }

    /**
     * Build the table header.
     *
     * @param widths the column width
     * @return the table header as a string
     */
    private StringBuilder generateHeader(Map<String, Integer> widths) {
        var header = new StringBuilder();
        var headerSeparator = new StringBuilder();
        LabelCollection.PORTFOLIO_TABLE_HEADERS
                .stream()
                .filter(label -> Filter.columnsToHideFilter(columnsToHide, label))
                .forEach(labelKey -> {
                    var labelValue = labelKey.getLabel(language);
                    var width = widths.get(labelKey.name());
                    header.append(markdownSeparator).append(Strings.leftPad(labelValue, width));
                    headerSeparator.append(markdownSeparator).append("-".repeat(width));
                });

        header.append(markdownSeparator).append(NEW_LINE);
        headerSeparator.append(markdownSeparator).append(NEW_LINE);
        return new StringBuilder().append(header).append(headerSeparator);
    }

    /**
     * Build the report title.
     *
     * @param tradeDate the last trade date in the report
     * @return the report title as a string
     */
    private StringBuilder generateMarkdownTitle(LocalDateTime tradeDate) {
        return new StringBuilder()
                .append("# ").append(Label.TITLE_PORTFOLIO_SUMMARY.getLabel(language)).append(NEW_LINE)

                .append(MARKDOWN_ITALIC)
                .append(Label.TITLE_GENERATED.getLabel(language)).append(": ")
                .append(LocalDateTimes.toNullSafeString(outputZone, dateTimePattern, tradeDate))
                .append(MARKDOWN_ITALIC)
                .append(NEW_LINE)

                .append(MARKDOWN_ITALIC)
                .append(Label.TITLE_BASE_CURRENCY.getLabel(language).replace("{0}", baseCurrency.name()))
                .append(MARKDOWN_ITALIC)
                .append(NEW_LINE)
                .append(NEW_LINE);
    }

    /**
     * Calculates the with of the columns that are shown in the Markdown report.
     *
     * @param portfolioReport portfolio report
     * @return length of the columns
     */
    private Map<String, Integer> calculateColumnWidth(PortfolioReport portfolioReport) {
        Map<String, Integer> widths = new HashMap<>();
        LabelCollection.PORTFOLIO_TABLE_HEADERS.forEach(x -> widths.put(x.name(), x.getLabel(language).length()));

        portfolioReport.getPortfolios().forEach((portfolioName, portfolio) -> {
            updateWidth(widths, Label.HEADER_PORTFOLIO, portfolio.getName());

            portfolio.getProducts().forEach((symbol, product) -> {
                if (BigDecimals.isNotZero(product.getQuantity())) {
                    updateWidth(widths, Label.HEADER_SYMBOL, product.getSymbol());
                    updateWidth(widths, Label.HEADER_QUANTITY, product.getQuantity());
                    updateWidth(widths, Label.HEADER_AVG_PRICE, product.getAveragePrice());
                    updateWidth(widths, Label.HEADER_MARKET_UNIT_PRICE, Objects.isNull(product.getMarketPrice())
                            ? null
                            : product.getMarketPrice().getUnitPrice());
                    updateWidth(widths, Label.HEADER_MARKET_VALUE, product.getMarketValue());
                    updateWidth(widths, Label.HEADER_INVESTED_AMOUNT, product.getInvestedAmount());
                    updateWidth(widths, Label.HEADER_PROFIT_LOSS, product.getProfitAndLoss());
                    updateWidth(widths, Label.HEADER_PROFIT_LOSS_PERCENT, product.getProfitAndLossPercent());
                    updateWidth(widths, Label.HEADER_COSTS, exchangeAndSum(
                            portfolioReport.getExchangeRates(),
                            product.getFees()));
                    updateWidth(widths, Label.HEADER_DEPOSITS, product.getDeposits());
                    updateWidth(widths, Label.HEADER_WITHDRAWALS, product.getWithdrawals());
                }
            });
        });
        return widths;
    }
}
