package com.remal.portfolio.picocli.arggroup;

import com.remal.portfolio.picocli.converter.StringToListConverter;
import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

import java.util.Collections;
import java.util.List;

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
public class CombineInputArgGroup extends InputArgGroup {

    @CommandLine.Option(
            order = 10,
            names = {"-i", "--input-files"},
            description = "Comma separated list of files with transactions to be combined. "
                    + "Accepted extensions: .txt, .md and .csv",
            arity = "1..*",
            converter = StringToListConverter.class)
    private final List<String> files = Collections.emptyList();

    @CommandLine.Option(
            order = 10,
            names = {"-o", "--overwrite"},
            description = "Overwrite the same transactions while combining them.")
    private boolean overwrite;
}
