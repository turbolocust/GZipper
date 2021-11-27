/*
 * Copyright (C) 2019 Matthias Fussenegger
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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;

import org.gzipper.java.util.Log;

/**
 * Utility class that provides methods for different kinds of file operations.
 *
 * @author Matthias Fussenegger
 */
public final class FileUtils {

    private FileUtils() {
        throw new AssertionError("Holds static members only");
    }

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
        final File parentFile = file.getParentFile();
        return parentFile.isDirectory() && !parentFile.getName().endsWith(" ");
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
        }
        return filename.contains("<") || filename.contains(">") || filename.contains("|")
                || filename.contains("*") || filename.contains("\"") || filename.contains("?");

    }

    /**
     * Concatenates two file names. Before doing so, a check will be performed
     * whether the first filename ends with a separator. If the separator is
     * missing it will be added.
     *
     * @param filename filename as string.
     * @param append   the filename to be appended.
     * @return an empty string if either any of the parameters is {@code null}
     * or empty. Otherwise, the concatenated absolute path is returned.
     */
    public static String combine(String filename, String append) {
        if (StringUtils.isNullOrEmpty(filename) || StringUtils.isNullOrEmpty(append)) {
            return StringUtils.EMPTY;
        }
        // check if location ends with separator and add it if missing
        final String absolutePath;
        if (filename.endsWith(File.separator)) {
            absolutePath = filename;
        } else {
            absolutePath = filename + File.separator;
        }
        return absolutePath + append;
    }

    /**
     * Returns the filename extension of a specified filename.
     *
     * @param filename the name of the file as string.
     * @return filename extension including period or an empty string if the
     * specified filename has no filename extension.
     */
    public static String getExtension(String filename) {
        int period = new File(filename).getName().indexOf('.');
        return period > 0 ? filename.substring(period) : StringUtils.EMPTY;
    }

    /**
     * Returns the name of the specified filename including its extension.
     *
     * @param filename the name of the file as string.
     * @return the name of the file including its file name extension.
     */
    public static String getName(String filename) {
        int lastSeparatorIndex = filename.lastIndexOf(File.separator);

        if (lastSeparatorIndex == -1) {
            lastSeparatorIndex = 0; // no separator present
        } else {
            ++lastSeparatorIndex;
        }

        return filename.substring(lastSeparatorIndex);
    }

    /**
     * Returns the display name of the specified filename without its extension.
     *
     * @param filename the name of the file as string.
     * @return the display name of the file without its file name extension.
     */
    public static String getDisplayName(String filename) {
        int lastSeparatorIndex = filename.lastIndexOf(File.separator);
        int lastPeriodIndex = filename.lastIndexOf('.');

        if (lastSeparatorIndex == -1) {
            lastSeparatorIndex = 0; // no separator present
        } else {
            ++lastSeparatorIndex;
        }

        if (lastPeriodIndex == -1) {
            lastPeriodIndex = filename.length();
        }

        return filename.substring(lastSeparatorIndex, lastPeriodIndex);
    }

    /**
     * Returns the canonical path if possible or otherwise the absolute path.
     *
     * @param file the file of which to get the path of.
     * @return the canonical path if possible or otherwise the absolute path.
     */
    public static String getPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException ex) {
            Log.e("Canonical path could not be computed", ex);
            return file.getAbsolutePath();
        }
    }

    /**
     * Returns the full name of the parent directory of the specified file name.
     *
     * @param filename the file name of which to receive the parent directory.
     * @return the parent directory of the specified file name or an empty
     * string if the specified file name does not have a parent.
     */
    public static String getParent(String filename) {
        final File file = new File(filename);
        String parent = file.getParent();
        return parent != null ? parent : StringUtils.EMPTY;
    }

    /**
     * Copies a file from the specified source to destination. If no copy
     * options are specified, the file at the destination will not be replaced
     * in case it already exists.
     *
     * @param src     the source path.
     * @param dst     the destination path.
     * @param options optional copy options.
     * @throws IOException if an I/O error occurs.
     */
    public static void copy(String src, String dst, CopyOption... options) throws IOException {
        if (options == null) {
            options = new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};
        }
        Files.copy(Paths.get(src), Paths.get(dst), options);
    }

    /**
     * Returns the file size of the specified file.
     *
     * @param file   the file whose size is to be returned.
     * @param filter the filter to be applied.
     * @return the size of the file or {@code 0} if the specified predicate
     * evaluates to {@code false}.
     */
    public static long fileSize(File file, Predicate<String> filter) {
        return filter.test(file.getName()) ? file.length() : 0;
    }

    /**
     * Traverses the specified path and returns the size of all children.
     *
     * @param path   the path to be traversed.
     * @param filter the filter to be applied.
     * @return the size of all children.
     */
    public static long fileSizes(Path path, Predicate<String> filter) {

        final SizeValueHolder holder = new SizeValueHolder();

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String name = file.getFileName().toString();
                    if (filter.test(name)) {
                        holder._size += attrs.size();
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException ex) {
                    Log.e(file + " skipped. Progress may be inaccurate", ex);
                    return FileVisitResult.CONTINUE; // skip folder that can't be traversed
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException ex) {
                    if (ex != null) {
                        Log.e(dir + " could not be traversed. Progress may be inaccurate", ex);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }

        return holder._size;
    }

    /**
     * Generates a unique file name using the specified parameters.
     *
     * @param path the file path including only the directory.
     * @param name the name of the file of which to generate a unique version.
     * @return a unique filename that consists of the path, name, suffix and
     * filename extension (if any).
     */
    public static String generateUniqueFilename(String path, String name) {
        return generateUniqueFilename(path, name, 1);
    }

    /**
     * Generates a unique file name using the specified parameters.
     *
     * @param path        the file path including only the directory.
     * @param name        the name of the file of which to generate a unique version.
     * @param beginSuffix the suffix to begin with (will be incremented).
     * @return a unique filename that consists of the path, name, suffix and
     * filename extension (if any).
     */
    public static String generateUniqueFilename(String path, String name, int beginSuffix) {
        final String ext = name.contains(".")
                ? getExtension(name) : StringUtils.EMPTY;
        if (!ext.isEmpty()) {
            name = getDisplayName(name);
        }
        return generateUniqueFilename(path, name, ext, beginSuffix);
    }

    /**
     * Generates a unique file name using the specified parameters.
     *
     * @param path the file path including only the directory.
     * @param name the name of the file of which to generate a unique version.
     * @param ext  the name of the file extension.
     * @return a unique filename that consists of the path, name, suffix and
     * filename extension.
     */
    public static String generateUniqueFilename(String path, String name, String ext) {
        return generateUniqueFilename(path, name, ext, 1);
    }

    /**
     * Generates a unique file name using the specified parameters.
     *
     * @param path        the file path including only the directory.
     * @param name        the name of the file of which to generate a unique version.
     * @param ext         the name of the file extension.
     * @param beginSuffix the suffix to begin with (will be incremented). This
     *                    parameter will be ignored if its value is less or equal zero.
     * @return a unique filename that consists of the path, name, suffix and
     * filename extension.
     */
    public static String generateUniqueFilename(String path, String name, String ext, int beginSuffix) {
        int suffix = beginSuffix > 0 ? beginSuffix : 1; // will be appended
        boolean isFirst = true; // to ignore suffix on first check
        final StringBuilder filename = new StringBuilder();

        if (ext.startsWith("*")) { // ignore asterisk if any
            ext = ext.substring(1);
        }

        final String trimmedPath = path.trim();

        String uniqueFilename = FileUtils.combine(trimmedPath, name + ext);
        if (!FileUtils.isValid(uniqueFilename)) {
            return uniqueFilename; // return as it is if not exists
        }

        do { // as long as file exists
            if (isFirst && beginSuffix <= 0) {
                filename.append(name).append(ext);
            } else {
                filename.append(name).append(suffix).append(ext);
                ++suffix;
            }
            isFirst = false;
            uniqueFilename = FileUtils.combine(trimmedPath, filename.toString());
            filename.setLength(0); // clear
        } while (FileUtils.isValidFile(uniqueFilename));

        return uniqueFilename;
    }

    private static class SizeValueHolder {

        private long _size;

        SizeValueHolder() {
            _size = 0;
        }
    }
}
