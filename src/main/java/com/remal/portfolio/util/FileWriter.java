package com.remal.portfolio.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * This is a tool that writes content to file.
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
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
     * @param filename the file to write the content to
     * @param content content that wil be written to the file
     */
    public static void write(FileWriter.WriteMode writeMode, String filename, byte[] content) {
        try {
            var path = Paths.get(filename);
            log.debug("writing the report to '{}', write-mode: {}...", filename, writeMode);

            switch (writeMode) {
                case OVERWRITE:
                    Files.write(path, content);
                    break;

                case APPEND:
                    if ((new File(filename)).createNewFile()) {
                        log.debug("The '{}' file has been created successfully.", filename);
                    }
                    if (!isEmptyOrEndsWithNewline(filename)) {
                        Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                    }
                    Files.write(path, content, StandardOpenOption.APPEND);
                    break;

                case STOP_IF_EXIST:
                default:
                    Files.write(path, content, StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException e) {
            var message = "An unexpected error has occurred while writing to '{}' file. Error: {}";
            Logger.logErrorAndExit(message, filename, e.toString());
        }
    }

    /**
     * Check how the file ends.
     *
     * @param pathToFile path to the file
     * @return true if the file ends with a new line character
     * @throws IOException case of error
     */
    private static boolean isEmptyOrEndsWithNewline(String pathToFile) throws IOException {
        File file = new File(pathToFile);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            if (randomAccessFile.length() == 0) {
                return true;
            } else {
                randomAccessFile.seek(file.length() - 1);
                byte b = randomAccessFile.readByte();
                return b == '\n' || b == '\r';
            }
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
