package com.remal.portfolio.parser;

import com.remal.portfolio.model.PortfolioCollection;

import java.util.Collections;
import java.util.List;

/**
 * Summary report file parser.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class PortfolioParser extends Parser<PortfolioCollection> {

    /**
     * Process a CSV file.
     *
     * @param file path to the data file
     * @return     the list of the parsed items
     */
    @Override
    protected List<PortfolioCollection> parseCsvFile(String file) {
        return Collections.emptyList();
    }

    /**
     * Process an Excel file.
     *
     * @param file path to the data file
     * @return     the list of the parsed items
     */
    @Override
    protected List<PortfolioCollection> parseExcelFile(String file) {
        return Collections.emptyList();
    }

    /**
     * Process a Text/Markdown file.
     *
     * @param file path to the data file
     * @return     the list of the parsed items
     */
    @Override
    protected List<PortfolioCollection> parseMarkdownFile(String file) {
        return Collections.emptyList();
    }
}
