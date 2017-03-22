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
package org.gzipper.java.application.pojo;

import java.io.File;
import java.util.List;
import org.gzipper.java.application.model.ArchiveType;

/**
 * POJO class that hold information required for archiving operations.
 *
 * @author Matthias Fussenegger
 */
public class ArchiveInfo {

    /**
     * The type of the archive.
     */
    private final ArchiveType _archiveType;

    /**
     * The compression level of the archive.
     */
    private int _level;

    /**
     * The files to be compressed.
     */
    private List<File> _files;

    /**
     * The full name of the archive.
     */
    private String _archiveName;

    /**
     * The output path either for the archive or the decompressed files.
     */
    private String _outputPath;

    public ArchiveInfo(ArchiveType archiveType, String archiveName,
            int level, List<File> files, String outputPath) {
        _archiveType = archiveType;
        _archiveName = archiveName;
        _level = level;
        _files = files;
        _outputPath = outputPath;
    }

    public ArchiveType getArchiveType() {
        return _archiveType;
    }

    public int getLevel() {
        return _level;
    }

    public void setLevel(int level) {
        _level = level;
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
