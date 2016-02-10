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
package Operations;

import Graphics.Settings;

/**
 * Currently used to validate file paths depending on operating system
 *
 * @author Matthias Fussenegger
 */
public class FileValidator {

    /**
     * Validates the file path depending on operating system
     *
     * @param path The path to be validated
     * @return The new valid path
     */
    public String validatePath(String path) {
        String validPath = "";
        if (Settings._isUnix) {
            for (int i = 0; i < path.length(); ++i) {
                if (path.charAt(i) == '\\') {
                    validPath = validPath + '/';
                } else {
                    validPath = validPath + path.charAt(i);
                }
            }
            if (validPath.charAt(validPath.length() - 1) != '/') {
                validPath = validPath + '/';
            }
        } else {
            for (int i = 0; i < path.length(); ++i) {
                if (path.charAt(i) == '/') {
                    validPath = validPath + '\\';
                } else {
                    validPath = validPath + path.charAt(i);
                }
            }
            if (validPath.charAt(validPath.length() - 1) != '\\') {
                validPath = validPath + '\\';
            }
        }
        return validPath;
    }
}
