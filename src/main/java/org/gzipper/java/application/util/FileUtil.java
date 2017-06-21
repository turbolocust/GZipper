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
     * Validates the specified path, which has to exist.
     *
     * @param path the path as string to be validated.
     * @return true if file exists, false otherwise.
     */
    public static boolean isValid(String path) {
        final File file = new File(path);
        return file.exists();
    }

    /**
     * Validates the specified path, which has to be a normal file.
     *
     * @param path the path as string to be validated.
     * @return true if file is a file, false otherwise.
     */
    public static boolean isValidFile(String path) {
        final File file = new File(path);
        return file.isFile();
    }

    /**
     * Validates the specified path, which has to be the full name of a file
     * that shall be created and therefore does not exist yet.
     *
     * @param path the path as string to be validated.
     * @return true if the parent file is a directory, false otherwise.
     */
    public static boolean isValidOutputFile(String path) {
        final File file = new File(path);
        return file.getParentFile().isDirectory();
    }

    /**
     * Validates the specified path, which has to be the path of a directory.
     *
     * @param path the path as string to be validated.
     * @return true if file exists and is a directory, false otherwise.
     */
    public static boolean isValidDirectory(String path) {
        final File file = new File(path);
        return file.isDirectory();
    }

    /**
     * Checks whether the filename contains illegal characters.
     *
     * @param filename the name to be checked for illegal characters.
     * @return true if filename contains illegal characters, false otherwise.
     */
    public static boolean containsIllegalChars(String filename) {
        final File file = new File(filename);
        if (!file.isDirectory()) {
            final String name = file.getName();
            return name.contains("<") || name.contains(">") || name.contains("/")
                    || name.contains("\\") || name.contains("|") || name.contains(":")
                    || name.contains("*") || name.contains("\"") || name.contains("?");
        } else { // is directory
            return filename.contains("<") || filename.contains(">") || filename.contains("|")
                    || filename.contains("*") || filename.contains("\"") || filename.contains("?");
        }
    }

    /**
     * Concatenates a file path and file name. Before doing so, a check will be
     * performed whether the path ends with an separator. If the separator is
     * missing it will be added. As a result, a valid absolute path is returned,
     * although it is not guaranteed that this file exists.
     *
     * @param path location of a folder as string.
     * @param file the file name as string.
     * @return {@code null} if either any of the parameters is {@code null} or
     * empty. Otherwise the concatenated absolute path is returned.
     */
    public static String combinePathAndFilename(String path, String file) {
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
     * Returns the file extension of a specified string.
     *
     * @param filename the name of the file as string.
     * @return the file extension including period or an empty string if the
     * specified filename has no file extension.
     */
    public static String getFileExtension(String filename) {
        int period = new File(filename).getName().indexOf('.');
        return period > 0 ? filename.substring(period) : "";
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
    public static synchronized Path copy(String src, String dst,
            CopyOption... options) throws IOException {
        if (options == null) {
            options = new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};
        }
        return Files.copy(Paths.get(src), Paths.get(dst), options);
    }

    /**
     * Generates a unique file name using the specified parameters.
     *
     * @param path the file path including only the directory.
     * @param name the name of the file of which to generate a unique version.
     * @param ext the name of the file extension.
     * @return a unique filename including path, name, suffix and extension.
     */
    public static String generateUniqueFilename(String path, String name, String ext) {
        String uniqueFilename;
        int suffix = 0; // will be appended to file name
        final StringBuilder filename = new StringBuilder();

        if (ext.startsWith("*")) { // ignore asterisk if any
            ext = ext.substring(1);
        }

        do {
            ++suffix;
            filename.append(name).append(suffix).append(ext);
            uniqueFilename = FileUtil.combinePathAndFilename(path, filename.toString());
            filename.setLength(0); // clear
        } while (FileUtil.isValidFile(uniqueFilename));

        return uniqueFilename;
    }
}
