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
package org.gzipper.java.application.model;

import org.gzipper.java.application.algorithm.type.*;
import org.gzipper.java.application.algorithm.CompressionAlgorithm;

/**
 * Enumeration for archive types.
 *
 * @author Matthias Fussenegger
 */
public enum ArchiveType {

    ZIP("Zip", "ZIP", new String[]{"*.zip"}) {
        @Override
        public CompressionAlgorithm getAlgorithm() {
            return new Zip();
        }
    },
    JAR("Jar", "JAR", new String[]{"*.jar"}) {
        @Override
        public CompressionAlgorithm getAlgorithm() {
            return new Jar();
        }
    },
    GZIP("Gzip", "GZIP", new String[]{"*.gz", "*.gzip"}) {
        @Override
        public CompressionAlgorithm getAlgorithm() {
            return new Gzip();
        }
    },
    TAR_GZ("TarGz", "TAR+GZIP", new String[]{"*.tar.gz", "*.tar.gzip", "*.tgz"}) {
        @Override
        public CompressionAlgorithm getAlgorithm() {
            return new Tarball();
        }
    },
    TAR_BZ2("TarBz2", "TAR+BZIP2", new String[]{"*.tar.bz2", "*.tar.bzip2", "*.tbz2"}) {
        @Override
        public CompressionAlgorithm getAlgorithm() {
            return new TarBzip2();
        }
    },
    TAR_LZ("TarLz", "TAR+LZMA", new String[]{"*.tar.lzma", "*.tar.lz", "*.tlz"}) {
        @Override
        public CompressionAlgorithm getAlgorithm() {
            return new TarLzma();
        }
    };

    /**
     * The name of the archive type.
     */
    private final String _name;

    /**
     * The display (friendly) name of the archive type.
     */
    private final String _displayName;

    /**
     * An array of strings consisting of the file extensions for the archive
     * type.
     */
    private final String[] _extensionNames;

    ArchiveType(String name, String displayName, String[] extensionNames) {
        _name = name;
        _displayName = displayName;
        _extensionNames = extensionNames;
    }

    /**
     * Returns the algorithm that belongs to this type.
     *
     * @return the algorithm that belongs to this type.
     */
    public abstract CompressionAlgorithm getAlgorithm();

    /**
     * Returns the name of this archive type.
     *
     * @return the name of this archive type.
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns the display (friendly) name of this archive type.
     *
     * @return the display (friendly) name of this archive type.
     */
    public String getDisplayName() {
        return _displayName;
    }

    /**
     * Returns the file extensions of this archive type.
     *
     * @param includeAsterisks true to include asterisks, false to remove them.
     * @return the file extensions of this archive type as string array.
     */
    public String[] getExtensionNames(boolean includeAsterisks) {

        String[] extensionNames;

        if (includeAsterisks) {
            extensionNames = _extensionNames;
        } else {
            extensionNames = new String[_extensionNames.length];
            for (int i = 0; i < _extensionNames.length; ++i) {
                extensionNames[i] = _extensionNames[i].substring(1);
            }
        }

        return extensionNames;
    }

    /**
     * Returns the default file extension of this archive type. The default file
     * extension is the first one that has been specified in the array of file
     * extensions.
     *
     * @param includeAsterisk true to include asterisk, false to remove it.
     * @return the default file extension of this archive type as string.
     */
    public String getDefaultExtensionName(boolean includeAsterisk) {
        return _extensionNames.length > 0 ? _extensionNames[0].substring(1) : "";
    }

    @Override
    public String toString() {
        return _displayName + " (" + _extensionNames[0] + ")";
    }
}
