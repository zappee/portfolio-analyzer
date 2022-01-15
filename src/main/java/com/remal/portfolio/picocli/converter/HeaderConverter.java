package com.remal.portfolio.picocli.converter;

import com.remal.portfolio.i18n.Header;
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
public class HeaderConverter implements CommandLine.ITypeConverter<List<Header>> {

    /**
     * Converts comma separated string to list of Headers.
     *
     * @param s user defined value from the command line interface
     * @return the list with Headers
     */
    @Override
    public List<Header> convert(String s) {
        List<Header> headers = new ArrayList<>();
        var params = s.split(",");
        Arrays.stream(params).forEach(x -> headers.add(Header.valueOf(x.trim())));
        return headers;
    }
}
