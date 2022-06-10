package com.remal.portfolio.parser;

import com.remal.portfolio.model.ProductSummaryCollection;

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
public class SummaryParser extends Parser<ProductSummaryCollection> {

    /**
     * Process a CSV file.
     *
     * @param file path to the data file
     * @return     the list of the parsed items
     */
    @Override
    protected List<ProductSummaryCollection> parseCsvFile(String file) {
        return Collections.emptyList();
    }

    /**
     * Process an Excel file.
     *
     * @param file path to the data file
     * @return     the list of the parsed items
     */
    @Override
    protected List<ProductSummaryCollection> parseExcelFile(String file) {
        return Collections.emptyList();
    }

    /**
     * Process a Text/Markdown file.
     *
     * @param file path to the data file
     * @return     the list of the parsed items
     */
    @Override
    protected List<ProductSummaryCollection> parseMarkdownFile(String file) {
        return Collections.emptyList();
    }
}
