/*
 * Copyright (C) 2016 Matthias Fussenegger
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

/**
 * Used to validate files, e.g. file paths.
 *
 * @author Matthias Fussenegger
 */
public class FileUtil {

    /**
     * Validates the specified path, which has to be a directory.
     *
     * @param path The path as string to be validated.
     * @return True if path exists and is a directory, false otherwise.
     */
    public static boolean isValidDirectory(String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    public static String generateFileName(String name) {

        File file = new File(name);
        int suffix = 2; // will be appended to file name if it already exists

        while (file.exists()) {
            String fileName = file.getName() + " (" + suffix + ")";
            file = new File(fileName);
            ++suffix;
        }

        return file.getAbsolutePath();
    }

}
