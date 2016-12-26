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
package org.gzipper.java.application.pojo;

import java.io.File;
import java.util.List;
import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.application.model.CompressionStrength;

/**
 * POJO class that hold information required for archiving operations.
 *
 * @author Matthias Fussenegger
 */
public class ArchiveInfo {

    private ArchiveType _archiveType;

    private CompressionStrength _strength;

    private List<File> _files;

    private String _archiveName;

    private String _outputPath;

    public ArchiveInfo() {
    }

    public ArchiveInfo(ArchiveType archiveType, String archiveName,
            CompressionStrength strength, List<File> files, String outputPath) {
        _archiveType = archiveType;
        _archiveName = archiveName;
        _strength = strength;
        _files = files;
        _outputPath = outputPath;
    }

    public ArchiveType getArchiveType() {
        return _archiveType;
    }

    public void setArchiveType(ArchiveType archiveType) {
        _archiveType = archiveType;
    }

    public CompressionStrength getStrength() {
        return _strength;
    }

    public void setStrength(CompressionStrength strength) {
        _strength = strength;
    }

    public List<File> getFiles() {
        return _files;
    }

    public void setFiles(List<File> files) {
        _files = files;
    }

    public String getArchiveName() {
        return _archiveName;
    }

    public void setArchiveName(String archiveName) {
        _archiveName = archiveName;
    }

    public String getOutputPath() {
        return _outputPath;
    }

    public void setOutputPath(String outputPath) {
        _outputPath = outputPath;
    }

}
