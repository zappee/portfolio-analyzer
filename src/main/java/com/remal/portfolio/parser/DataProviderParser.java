package com.remal.portfolio.parser;

import com.remal.portfolio.model.DataProviderConfiguration;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import java.util.List;

/**
 * Parse files keep data provider information. This file is used to determine
 * name of the data provider for tickers.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class DataProviderParser extends Parser<DataProviderConfiguration> {

    /**
     * Process a CSV file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<DataProviderConfiguration> parseCsvFile(String file) {
        throw new NotImplementedException(null);
    }

    /**
     * Process an Excel file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<DataProviderConfiguration> parseExcelFile(String file) {
        throw new NotImplementedException(null);
    }

    /**
     * Process a Text/Markdown file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    @Override
    protected List<DataProviderConfiguration> parseMarkdownFile(String file) {
        throw new NotImplementedException(null);
    }
}
