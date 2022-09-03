package com.remal.portfolio.model;

/**
 * Supported file types.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public enum FileType {

    /**
     * File type for *,txt and *.md files.
     */
    MARKDOWN,

    /**
     * File type for *.csv files.
     */
    CSV,

    /**
     * Used when an unsupported file type is given.
     */
    NOT_SUPPORTED,

    /**
     * Used when the given file name is empty or null.
     */
    NOT_DEFINED
}
