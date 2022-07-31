package com.remal.portfolio.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * This is a tool that writes content to file.
 * <p>
 * Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
@Slf4j
public class FileWriter {

    /**
     * The mode how to open the file.
     */
    public enum WriteMode {

        /**
         * Overwriting is the rewriting or replacing of file with new data.
         * Saving the new one will overwrite the previous file.
         */
        OVERWRITE,

        /**
         * Appending a file refers to a process that involves adding new data
         * elements at the end of the file. It creates the file if file does
         * not exist.
         */
        APPEND,

        /**
         * This means that the program will abort and stop writing content to file
         * if the file you are trying to access exist.
         */
        STOP_IF_EXIST
    }

    /**
     * Write the content to a file.
     *
     * @param writeMode how to open the file
     * @param filename  the file to write the content to
     * @param content   content that wil be written to the file
     */
    public static void write(FileWriter.WriteMode writeMode, String filename, byte[] content) {
        try {
            var path = Path.of(filename);
            log.debug("> writing the report to \"{}\", write-mode: {}...", filename, writeMode);
            if (writeMode == WriteMode.STOP_IF_EXIST) {
                Files.write(path, content, StandardOpenOption.CREATE_NEW);
            } else {
                Files.write(path, content);
            }
        } catch (IOException e) {
            var message = "An unexpected error has occurred while writing to '{}' file. Error: {}";
            Logger.logErrorAndExit(message, filename, e.toString());
        }
    }

    /**
     * Utility classes should not have public constructors.
     *
     * @throws java.lang.UnsupportedOperationException if this method is called
     */
    private FileWriter() {
        throw new UnsupportedOperationException();
    }
}
