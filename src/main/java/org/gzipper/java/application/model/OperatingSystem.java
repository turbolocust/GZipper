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

/**
 * Abstract class that represents an operating system. This class basically acts
 * as a wrapper for an {@link OS} enumeration while offering additional
 * functionalities that may be required for archiving operations.
 *
 * @author Matthias Fussenegger
 */
public abstract class OperatingSystem {

    /**
     * The aggregated enumeration which represents the operating system.
     */
    protected final OS _operatingSystem;

    /**
     * Constructs a new instance of this class using the specified enumeration.
     *
     * @param operatingSystem the operating system to aggregate.
     */
    protected OperatingSystem(OS operatingSystem) {
        this._operatingSystem = operatingSystem;
    }

    /**
     * Returns the default user directory of the system.
     *
     * @return the default user directory as string.
     */
    public String getDefaultUserDirectory() {
        return System.getProperty("user.home");
    }

    /**
     * Returns the enumeration for the current operating system.
     *
     * @return the enumeration for the current operating system.
     */
    public OS getOSInfo() {
        return _operatingSystem;
    }
}
