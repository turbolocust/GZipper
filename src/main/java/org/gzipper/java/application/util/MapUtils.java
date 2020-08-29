/*
 * Copyright (C) 2020 Matthias Fussenegger
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

import java.util.Map;

/**
 * Utility class that provides methods for instances of {@link Map}.
 *
 * @author Matthias Fussenegger
 */
public final class MapUtils {

    private MapUtils() {
        throw new AssertionError("Holds static members only");
    }

    /**
     * Checks whether the given list is {@code null} or empty.
     *
     * @param <K> the type of the key of this map.
     * @param <V> the type of the value that is mapped to the key.
     * @param map the map to be checked.
     * @return true if map is {@code null} or empty, false otherwise.
     */
    public static <K, V> boolean isNullOrEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }
}