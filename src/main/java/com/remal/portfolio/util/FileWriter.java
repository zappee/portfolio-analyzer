package com.remal.portfolio.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * This is a tool that writes content to file..
 * <p>
 * Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
 * BSD (2-clause) licensed
 * </p>
 * @author arnold.somogyi@gmail.comm
 */
public class FileWriter {

    /**
     * The wat to persist data to file.
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
     *  Writes content  to file.
     *
     * @param writeMode controls how to open the file
     * @param pathToFile the path of the file to be created, e.g. "''yyyy-MM-dd'_portfolio-summary.md'"
     * @param zoneIdAsString used to convert timestamp between timezones
     * @param content the content that wil be written to the file
     */
    public static void write(FileWriter.WriteMode writeMode, String pathToFile, String zoneIdAsString, String content) {

        try {
            var zoneId = ZoneId.of(zoneIdAsString);
            var filename = LocaleDateTimes.toString(zoneId, pathToFile, LocalDateTime.now());
            var path = Paths.get(filename);

            switch (writeMode) {
                case OVERWRITE:
                    Files.writeString(path, content);
                    break;

                case APPEND:
                    if ((new File(filename)).createNewFile()) {
                        System.out.println("file has been created successfully");
                    }
                    if (isEmptyOrEndsWithNewline(filename)) {
                        Files.write(path, content.getBytes(), StandardOpenOption.APPEND);
                    } else {
                        Files.write(path, (System.lineSeparator() + content).getBytes(), StandardOpenOption.APPEND);
                    }
                    break;

                default:
                    Files.write(path, content.getBytes(), StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Checks whether the file ends with a new line character or not.
     *
     * @param pathToFile path to the file
     * @return true if the file ends with a new line character
     * @throws IOException throws in case of error
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
     */
    private FileWriter() {
        // do nothing
    }
}
