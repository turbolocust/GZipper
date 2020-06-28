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

import java.util.List;

/**
 * Utility class that provides methods for instances of {@link List}.
 *
 * @author Matthias Fussenegger
 */
public final class ListUtils {

    private ListUtils() {
        throw new AssertionError("Holds static members only");
    }

    /**
     * Checks whether the given list is {@code null} or empty.
     *
     * @param <T> types of which this list consists of.
     * @param list the list to be checked.
     * @return true if list is {@code null} or empty, false otherwise.
     */
    public static <T> boolean isNullOrEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }
}
