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

import org.gzipper.java.application.algorithm.ArchivingAlgorithm;
import org.gzipper.java.application.algorithm.type.TarBzip2;
import org.gzipper.java.application.algorithm.type.Tarball;
import org.gzipper.java.application.algorithm.type.Zip;

/**
 *
 * @author Matthias Fussenegger
 */
public enum ArchiveType {

    ZIP("Zip", "ZIP", new String[]{"*.zip"}),
    TAR_GZ("TarGz", "TAR+GZ", new String[]{"*.tar.gz", "*.tgz"}),
    TAR_BZ("TarBz2", "TAR+BZ2", new String[]{"*.tar.bz2", "*.tbz2"});

    private final String _name;

    private final String _displayName;

    private final String[] _extensionNames;

    ArchiveType(String name, String displayName, String[] extensionNames) {
        _name = name;
        _displayName = displayName;
        _extensionNames = extensionNames;
    }

    public static synchronized ArchiveType determineArchiveType(String name) {

        ArchiveType archiveType = null;
        for (ArchiveType type : values()) {
            if (type.getName().equals(name)) {
                archiveType = type;
                break;
            }
        }
        return archiveType;
    }

    public ArchivingAlgorithm determineArchivingAlgorithm() {

        final ArchivingAlgorithm algorithm;
        switch (this) {
            case ZIP:
                algorithm = new Zip();
                break;
            case TAR_GZ:
                algorithm = new Tarball();
                break;
            case TAR_BZ:
                algorithm = new TarBzip2();
                break;
            default:
                algorithm = null;
        }
        return algorithm;
    }

    public String getName() {
        return _name;
    }

    public String getDisplayName() {
        return _displayName;
    }

    public String[] getExtensionNames() {
        return _extensionNames;
    }

    @Override
    public String toString() {
        return _displayName + " (" + _extensionNames[0] + ")";
    }
}
