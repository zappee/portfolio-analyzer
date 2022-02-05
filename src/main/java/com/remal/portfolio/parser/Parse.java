package com.remal.portfolio.parser;

import com.remal.portfolio.model.Transaction;
import com.remal.portfolio.parser.csv.CsvParser;
import com.remal.portfolio.parser.excel.ExcelParser;
import com.remal.portfolio.parser.markdown.MarkdownParser;
import com.remal.portfolio.util.Files;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Collections;
import java.util.List;

/**
 * This helper parses the input file and transform the data to a transaction list.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class Parse {

    /**
     * Extracts the transactions from the given file.
     *
     * @param sourceFile path to the file
     * @param dateTimePattern pattern used for converting string to LocalDateTime
     * @return list of the transactions
     */
    public static List<Transaction> file(String sourceFile, String dateTimePattern) {
        var filetype = Files.getFileType(sourceFile);
        Parser parser;

        switch (filetype) {
            case TEXT:
                parser = new MarkdownParser(sourceFile, dateTimePattern);
                return parser.parse();

            case CSV:
                parser = new CsvParser(sourceFile, dateTimePattern);
                return parser.parse();

            case EXCEL:
                parser = new ExcelParser(sourceFile, dateTimePattern);
                return parser.parse();

            default:
                log.error("Unsupported input file type: '{}'", sourceFile);
                System.exit(CommandLine.ExitCode.SOFTWARE);
                return Collections.emptyList();
        }
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private Parse() {
        throw new UnsupportedOperationException();
    }
}
