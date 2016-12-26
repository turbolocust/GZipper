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
package org.gzipper.java.presentation.util;

import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.application.model.CompressionStrength;
import org.gzipper.java.application.pojo.ArchiveInfo;
import org.gzipper.java.exceptions.GZipperException;

/**
 *
 * @author Matthias Fussenegger
 */
public class ArchiveInfoFactory {

    public static ArchiveInfo createArchiveInfo(String archiveType, int strengthIndex) throws GZipperException {

        ArchiveInfo info = new ArchiveInfo();
        ArchiveType type = ArchiveType.determineArchiveType(archiveType);
        CompressionStrength strength = CompressionStrength.determineCompressionStrength(strengthIndex);

        if (type == null || strength == null) {
            throw new GZipperException("Archive type or compression strength could not be determined.");
        }

        return info;
    }

    public static ArchiveInfo createArchiveInfo(String archiveType) throws GZipperException {

        ArchiveInfo info = new ArchiveInfo();
        ArchiveType type = ArchiveType.determineArchiveType(archiveType);

        if (type == null) {
            throw new GZipperException("Archive type could not be determined.");
        }

        return info;
    }

}
