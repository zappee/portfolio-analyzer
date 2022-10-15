package com.remal.portfolio.parser;

import com.remal.portfolio.model.CurrencyType;
import com.remal.portfolio.model.Label;
import com.remal.portfolio.model.LabelCollection;
import com.remal.portfolio.model.PortfolioReport;
import com.remal.portfolio.picocli.arggroup.InputArgGroup;
import com.remal.portfolio.util.Logger;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Parse files that keep the portfolio summary report.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class PortfolioSummaryParser extends Parser<PortfolioReport> {

    /**
     * The base currency of the report
     */
    private final CurrencyType baseCurrency;

    /**
     * Builder that initializes a new writer instance.
     *
     * @param baseCurrency the base currency of the report
     * @param language an ISO 639 alpha-2 or alpha-3 language code, e.g. en
     * @param arguments input arguments
     * @return the parser instance
     */
    public static Parser<PortfolioReport> build(CurrencyType baseCurrency, String language, InputArgGroup arguments) {
        return build(PortfolioReport.class, arguments, language, baseCurrency);
    }

    /**
     * Constructor.
     *
     * @param baseCurrency the base currency of the report
     */
    public PortfolioSummaryParser(CurrencyType baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    /**
     * Process a CSV file.
     *
     * @param fileName path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<PortfolioReport> parseCsvFile(String fileName) {
        List<PortfolioReport> portfolioReports = new ArrayList<>();

        // validation
        File file = new File(fileName);
        if (!file.exists() || file.length() == 0) {
            return portfolioReports;
        }

        // read and process the header
        String firstLine = "";
        try (FileReader fileReader = new FileReader(fileName)) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            firstLine = bufferedReader.readLine();
        } catch (IOException e) {
            Logger.logErrorAndExit("Error while reading the \"{}\" file: {}", e);
        }

        LinkedList<String> map = new LinkedList<>();
        map.add(Label.HEADER_REQUEST_DATE.name());

        var labels = firstLine.split(Pattern.quote(csvSeparator));
        Arrays.stream(labels).forEach(label ->
                LabelCollection.PRODUCT_SUMMARY_FOOTER.forEach(labelFromCollection ->
                        Arrays.stream(CurrencyType.values()).forEach(currency -> {
                            var labelText = labelFromCollection.getLabel(language);
                            var expectedLabel = labelText.replace("{0}", currency.name());
                            var replacement = currency.name() + "-" + baseCurrency.name();
                            var expectedLabelForExchangeRate = labelText.replace("{0}", replacement);
                            if (label.equals(expectedLabel) || label.equals(expectedLabelForExchangeRate)) {
                                map.add(labelFromCollection.name() + ";" + currency.name());
                            }
                        })
                )
        );

        // read data
        try (Stream<String> stream = Files.lines(Path.of(fileName))) {
            var skipRows = 1;
            stream
                    .skip(skipRows)
                    .forEach(line -> {
                        var cells = line.split(Pattern.quote(csvSeparator), -1);
                        var generated = getLocalDateTime(new AtomicInteger(), cells);
                        var portfolioReport = new PortfolioReport(baseCurrency, generated);
                        AtomicInteger index = new AtomicInteger();
                        Arrays.stream(cells).forEach(cell -> {
                            var mapEntry = map.get(index.get());
                            var cellConfig = mapEntry.split(";");

                            switch (Label.valueOf(cellConfig[0])) {
                                case LABEL_TOTAL_CASH_PER_CURRENCY -> portfolioReport
                                        .getCashInPortfolio()
                                        .put(cellConfig[1], getBigDecimal(index, cells, Label.HEADER_EMPTY));
                                case LABEL_TOTAL_EXCHANGE_RATE -> portfolioReport
                                        .getExchangeRates()
                                        .put(
                                                cellConfig[1] + "-" + baseCurrency,
                                                getBigDecimal(index, cells, Label.HEADER_EMPTY));
                                case LABEL_TOTAL_DEPOSIT_PER_CURRENCY -> portfolioReport
                                        .getDeposits()
                                        .put(cellConfig[1], getBigDecimal(index, cells, Label.HEADER_EMPTY));
                                case LABEL_TOTAL_WITHDRAWAL_PER_CURRENCY -> portfolioReport
                                        .getWithdrawals()
                                        .put(cellConfig[1], getBigDecimal(index, cells, Label.HEADER_EMPTY));
                                case LABEL_TOTAL_INVESTMENT_PER_CURRENCY -> portfolioReport
                                        .getInvestments()
                                        .put(cellConfig[1], getBigDecimal(index, cells, Label.HEADER_EMPTY));
                                case LABEL_TOTAL_MARKET_VALUE_PER_CURRENCY -> portfolioReport
                                        .getMarketValues()
                                        .put(cellConfig[1], getBigDecimal(index, cells, Label.HEADER_EMPTY));
                                case LABEL_TOTAL_PROFIT_LOSS_PER_CURRENCY -> portfolioReport
                                        .getProfitLoss()
                                        .put(cellConfig[1], getBigDecimal(index, cells, Label.HEADER_EMPTY));
                                default -> index.getAndIncrement();
                            }
                        });
                        portfolioReports.add(portfolioReport);
                    });
        } catch (Exception e) {
            Logger.logErrorAndExit("Error while parsing the \"{}\" file: {}", fileName, e);
        }
        return portfolioReports;
    }

    /**
     * Process a Text/Markdown file.
     *
     * @param fileName path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<PortfolioReport> parseMarkdownFile(String fileName) {
        return new ArrayList<>();
    }
}
