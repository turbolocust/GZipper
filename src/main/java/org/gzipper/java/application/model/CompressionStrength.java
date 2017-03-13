/*
 * Copyright (C) 2016 Matthias Fussenegger
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
package org.gzipper.java.application.model;

/**
 *
 * @author Matthias Fussenegger
 */
public enum CompressionStrength {

    VERY_LOW(0), LOW(1), MEDIUM(2), GOOD(3), BEST(4);

    private final int _compressionLevel;

    private CompressionStrength(int compressionLevel) {
        _compressionLevel = compressionLevel;
    }

    public int getCompressionLevel() {
        return _compressionLevel;
    }

    public static CompressionStrength determineCompressionStrength(int value) {

        CompressionStrength compressionStrength = null;
        for (CompressionStrength strength : values()) {
            if (strength.ordinal() == value) {
                compressionStrength = strength;
                break;
            }
        }
        return compressionStrength;
    }
}
