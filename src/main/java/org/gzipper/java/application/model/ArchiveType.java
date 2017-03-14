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

import org.gzipper.java.application.algorithm.ArchivingAlgorithm;
import org.gzipper.java.application.algorithm.type.SevenZip;
import org.gzipper.java.application.algorithm.type.TarBzip2;
import org.gzipper.java.application.algorithm.type.TarGz;
import org.gzipper.java.application.algorithm.type.Zip;

/**
 *
 * @author Matthias Fussenegger
 */
public enum ArchiveType {

    ZIP("Zip", "ZIP (.zip)"),
    TAR_GZ("TarGz", "TAR+GZ (.tar.gz)"),
    TAR_BZ("TarBz2", "TAR+BZ2 (.tar.bz2)"),
    RAR("Rar", "RAR (.rar)"),
    SEVEN_Z("SevenZip", "SEVEN ZIP (.7z)");

    private final String _name;

    private final String _friendlyName;

    ArchiveType(String name, String friendlyName) {
        _name = name;
        _friendlyName = friendlyName;
    }

    public static ArchiveType determineArchiveType(String name) {

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
                algorithm = Zip.getInstance();
                break;
            case TAR_GZ:
                algorithm = TarGz.getInstance();
                break;
            case TAR_BZ:
                algorithm = TarBzip2.getInstance();
                break;
            case SEVEN_Z:
                algorithm = SevenZip.getInstance();
                break;
            default:
                algorithm = null;
        }
        return algorithm;
    }

    public String getName() {
        return _name;
    }

    public String getFriendlyName() {
        return _friendlyName;
    }

}
