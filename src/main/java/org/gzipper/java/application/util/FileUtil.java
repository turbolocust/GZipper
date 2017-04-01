/*
 * Copyright (C) 2017 Matthias Fussenegger
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gzipper.java.application.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Convenience class for different kinds of file operations.
 *
 * @author Matthias Fussenegger
 */
public class FileUtil {

    /**
     * Validates the specified path, which has to be a file or directory.
     *
     * @param path the path as string to be validated.
     * @return true if path exists, false otherwise.
     */
    public static boolean isValidFile(String path) {
        final File file = new File(path);
        return file.exists();
    }

    /**
     * Validates the specified path, which has to be the path of a directory.
     *
     * @param path the path as string to be validated.
     * @return true if path exists and is a directory, false otherwise.
     */
    public static boolean isValidDirectory(String path) {
        final File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    /**
     * Validates the specified path, which has to be the path of a file.
     *
     * @param path the path as string to be validated.
     * @return true if path exists and is not a directory, false otherwise.
     */
    public static boolean isValidFileName(String path) {
        final File file = new File(path);
        return file.isAbsolute() && !file.isDirectory();
    }

    /**
     * Checks whether the file name contains illegal characters.
     *
     * @param fileName the name to be checked for illegal characters.
     * @return true if file name contains illegal characters, false otherwise.
     */
    public static boolean containsIllegalChars(String fileName) {
        return fileName.contains("<") || fileName.contains(">") || fileName.contains("/")
                || fileName.contains("\\") || fileName.contains("|") || fileName.contains(":")
                || fileName.contains("*") || fileName.contains("\"") || fileName.contains("?");
    }

    /**
     * Concatenates a file path and file name. Before doing so, a check will be
     * performed whether the path ends with an separator. If the separator is
     * missing it will be added. As a result, a valid absolute path will be
     * returned, although it is not guaranteed that this file exists.
     *
     * @param path location of a folder.
     * @param file the file name.
     * @return {@code null} if either any of the parameters is {@code null} or
     * empty. Otherwise the concatenated absolute path is returned.
     */
    public static String combinePathAndFileName(String path, String file) {
        // check if parameters are not null and not empty
        if (path == null || file == null || path.isEmpty() || file.isEmpty()) {
            return null;
        }
        // check if location ends with separator and add it if missing
        String absolutePath = path.endsWith(File.separator)
                ? path
                : path + File.separator;
        return absolutePath + file;
    }

    /**
     * Copies a file from the specified source to destination. If no copy
     * options are specified, the file at the destination will not be replaced
     * in case it already exists.
     *
     * @param src the source path.
     * @param dst the destination path.
     * @param options optional copy options.
     * @return the path to the target file.
     * @throws IOException if an I/O error occurs.
     */
    public static Path copy(String src, String dst, CopyOption... options) throws IOException {
        if (options == null) {
            options = new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};
        }
        return Files.copy(Paths.get(src), Paths.get(dst), options);
    }

    /**
     * Deletes a file from the specified source location if it exists.
     *
     * @param src the source file.
     * @return true if file was deleted, false if it did not exist.
     * @throws IOException if an I/O error occurs.
     */
    public static boolean delete(String src) throws IOException {
        return Files.deleteIfExists(Paths.get(src));
    }
}
