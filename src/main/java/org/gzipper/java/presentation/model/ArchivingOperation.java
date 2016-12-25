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
package org.gzipper.java.presentation.model;

import java.io.File;
import java.util.List;
import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.application.model.CompressionStrength;

/**
 * POJO class that hold information required for archiving operations.
 *
 * @author Matthias Fussenegger
 */
public class ArchivingOperation {

    private final ArchiveType _archiveType;

    private final CompressionStrength _strength;

    private final List<File> _files;

    private final String _archiveName;

    private final String _outputPath;

    private final boolean _compress;

    public ArchivingOperation(ArchiveType archiveType, String archiveName, boolean compress,
            CompressionStrength strength, List<File> files, String outputPath) {
        _archiveType = archiveType;
        _archiveName = archiveName;
        _compress = compress;
        _strength = strength;
        _files = files;
        _outputPath = outputPath;
    }

    public ArchiveType getArchiveType() {
        return _archiveType;
    }

    public CompressionStrength getStrength() {
        return _strength;
    }

    public List<File> getFiles() {
        return _files;
    }

    public String getArchiveName() {
        return _archiveName;
    }

    public String getOutputPath() {
        return _outputPath;
    }

    public boolean isCompress() {
        return _compress;
    }

}
