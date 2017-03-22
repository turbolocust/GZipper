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
import java.nio.file.StandardCopyOption;

/**
 * Used to validate files, e.g. file paths.
 *
 * @author Matthias Fussenegger
 */
public class FileUtil {

    /**
     * Validates the specified path, which has to be the path of a directory.
     *
     * @param path the path as string to be validated.
     * @return true if path exists and is a directory, false otherwise.
     */
    public static boolean isValidDirectory(String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    /**
     * Validates the specified path, which has to be the path of a file.
     *
     * @param path the path as string to be validated.
     * @return true if path exists and is not a directory, false otherwise.
     */
    public static boolean isValidFileName(String path) {
        File file = new File(path);
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
     * Copies a file from the specified source to destination. If no copy
     * options are specified, the file at the destination will not be replaced
     * in case it already exists.
     *
     * @param src the source path.
     * @param dst the destination path.
     * @param options optional copy options.
     * @throws IOException if an I/O error occurs.
     */
    public static void copy(Path src, Path dst, CopyOption... options) throws IOException {
        if (options == null) {
            options = new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};
        }
        Files.copy(src, dst, options);
    }
}
