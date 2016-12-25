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

    ZIP("Zip"), TAR_GZ("TarGz"), TAR_BZ("TarBz2"), RAR("Rar"), SEVEN_Z("SevenZip");

    private final String _name;

    ArchiveType(String name) {
        _name = name;
    }

    public ArchiveType determineArchiveType(String name) {

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
}
