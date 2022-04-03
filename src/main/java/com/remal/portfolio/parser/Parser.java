package com.remal.portfolio.parser;

import com.remal.portfolio.util.Files;
import com.remal.portfolio.util.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * Parser common functions and method definitions that all parsers
 * must implement.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
@Setter
@Getter
public abstract class Parser<T> {

    /**
     * Log message.
     */
    protected static final String LOG_ERROR = "Error while parsing the '{}' file, error: {}";

    /**
     * Log message.
     */
    private static final String LOG_BEFORE_EXECUTION = "parsing the '{}' {} file...";

    /**
     * True if the file to parse has a header at the first row.
     */
    @Accessors(fluent = true)
    protected boolean hasHeader = true;

    /**
     * Date/time pattern that is used for converting string to LocalDateTime.
     */
    protected String dateTimePattern = "yyyy-MM-dd HH:mm:ss";

    /**
     * Markdown separator.
     */
    protected String markdownSeparator = "\\|";

    /**
     * CSV separator.
     */
    protected String csvSeparator = ",";

    /**
     * Parse the file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    public List<T> parse(String file) {
        List<T> items;
        var fileType = Files.getFileType(file);
        switch (fileType) {
            case CSV -> {
                log.debug(LOG_BEFORE_EXECUTION, file, "CSV");
                items = parseCsvFile(file);
            }
            case EXCEL -> {
                log.debug(LOG_BEFORE_EXECUTION, file, "Excel");
                items = parseExcelFile(file);
            }
            case MARKDOWN -> {
                log.debug(LOG_BEFORE_EXECUTION, file, "Markdown");
                items = parseMarkdownFile(file);
            }
            default -> {
                Logger.logErrorAndExit("Unsupported input file type: '{}'", file);
                items = Collections.emptyList();
            }
        }

        log.debug("{} items have been loaded", items.size());
        return items;
    }

    /**
     * Process a CSV file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    protected abstract List<T> parseCsvFile(String file);

    /**
     * Process an Excel file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    protected abstract List<T> parseExcelFile(String file);

    /**
     * Process a Text/Markdown file.
     *
     * @param file path to the data file
     * @return the list of the parsed items
     */
    protected abstract List<T> parseMarkdownFile(String file);
}
