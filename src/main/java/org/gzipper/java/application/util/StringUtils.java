/*
 * Copyright (C) 2017 Matthias Fussenegger
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gzipper.java.application.util;

/**
 * Utility class that provides methods for instances of {@link String}.
 *
 * @author Matthias Fussenegger
 */
public final class StringUtils {

    /**
     * An empty string.
     */
    public static final String EMPTY = "";

    /**
     * Checks whether the given string is {@code null} or empty.
     *
     * @param string the string to be checked.
     * @return true if string is {@code null} or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
}
