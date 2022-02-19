package com.remal.portfolio.picocli.converter;

import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Picocli type converter implementation.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class HeaderConverter implements CommandLine.ITypeConverter<List<String>> {

    /**
     * Converts comma separated string to list.
     *
     * @param values user defined value from the command line interface
     * @return the list with trimmed values
     */
    @Override
    public List<String> convert(String values) {
        List<String> valueList = new ArrayList<>();
        var valuePieces = values.split(",");
        Arrays.stream(valuePieces).forEach(x -> valueList.add(x.trim().toUpperCase()));
        return valueList;
    }
}
