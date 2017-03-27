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
package org.gzipper.java.application.model;

import org.apache.commons.compress.compressors.gzip.GzipParameters;

/**
 * Enumeration for operating systems.
 *
 * @author Matthias Fussenegger
 */
public enum OS {

    UNIX("Unix", 3), WINDOWS("Windows", 11);

    /**
     * The name of the operating system.
     */
    private final String _name;

    /**
     * The defined value as of {@link GzipParameters}.
     */
    private final int _value;

    OS(String name, int value) {
        _name = name;
        _value = value;
    }

    /**
     * Returns the name of the operating system.
     *
     * @return the name of the operating system.
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns the value that is defined as of {@link GzipParameters}.
     *
     * @return the value that is defined as of {@link GzipParameters}.
     */
    public int getValue() {
        return _value;
    }
}
