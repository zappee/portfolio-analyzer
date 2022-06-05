package com.remal.portfolio.picocli.arggroup;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

/**
 * Command line interface argument group for defining input options.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Getter
@Setter
public class SummaryInputArgGroup extends InputArgGroup {

    /**
     * Set the file that contains the transactions.
     */
    @CommandLine.Option(
            order = 10,
            names = {"-i", "--input-file"},
            description = "File with transactions. Accepted extensions: .txt, .md, .csv and .xlsx",
            required = true)
    private String file;

    /**
     * Set the data provider properties file.
     */
    @CommandLine.Option(
            order = 10,
            names = {"-l", "--provider-file"},
            description = "Path to a *.properties file to get the data provider name "
                    + "used to retrieve the market price.")
    private String providerFile;
}
