package com.remal.portfolio.util;

import com.remal.portfolio.model.FileType;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

/**
 * Tool that works with files and filenames.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class Files {

    /**
     * Determine the file type based on the filename.
     *
     * @param fileName filename with extension
     * @return the file type
     */
    public static FileType getFileType(String fileName) {
        var fileType = FileType.NOT_SUPPORTED;

        if (Objects.nonNull(fileName)) {
            // remove the ' char from the end of the filename
            // e.g. 'coinbase-pro_'yyyy-MM-dd'.md'
            var escapedFileName = fileName.endsWith("'")
                    ? fileName.substring(0, fileName.length() - 1)
                    : fileName;

            if (escapedFileName.matches("^.*\\.(md|txt)$")) {
                fileType = FileType.MARKDOWN;

            } else if (escapedFileName.matches("^.*\\.(csv)$")) {
                fileType = FileType.CSV;
            }
        } else {
            fileType = FileType.NOT_DEFINED;
        }
        return fileType;
    }

    /**
     * Reads the first line of the file.
     *
     * @param file the file to read
     * @return the first line of the file
     */
    public static String getFirstLine(File file) {
        String firstLine = "";
        try (FileReader fileReader = new FileReader(file)) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            firstLine = bufferedReader.readLine();
        } catch (IOException e) {
            Logger.logErrorAndExit("Error while reading the \"{}\" file: {}", e);
        }

        return firstLine;
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private Files() {
        throw new UnsupportedOperationException();
    }
}
