/*
 * Copyright (C) 2018 Matthias Fussenegger
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
package org.gzipper.java.application;

import java.io.File;
import java.util.List;
import java.util.zip.Deflater;
import org.gzipper.java.application.model.ArchiveType;

/**
 * Object that holds information required for archiving operations.
 *
 * @author Matthias Fussenegger
 */
public final class ArchiveInfo {

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
     * The output path either of the archive or the decompressed file(s).
     */
    private String _outputPath;

    ArchiveInfo(ArchiveType archiveType, String archiveName,
            int level, List<File> files, String outputPath) {
        _archiveType = archiveType;
        _archiveName = archiveName;
        _level = level;
        _files = files;
        _outputPath = outputPath;
    }

    /**
     * Returns the aggregated {@link ArchiveType}.
     *
     * @return the aggregated {@link ArchiveType}.
     */
    public ArchiveType getArchiveType() {
        return _archiveType;
    }

    /**
     * Returns the level for the compression as of {@link Deflater}.
     *
     * @return the level for compression as of {@link Deflater}.
     */
    public int getLevel() {
        return _level;
    }

    /**
     * Sets the level for the compression as of {@link Deflater}.
     *
     * @param level the level for the compression as of {@link Deflater}.
     */
    public void setLevel(final int level) {
        _level = level;
    }

    /**
     * Returns a list of files to be compressed.
     *
     * @return a list of files to be compressed.
     */
    public List<File> getFiles() {
        return _files;
    }

    /**
     * Sets the files to be compressed.
     *
     * @param files the files to be compressed.
     */
    public void setFiles(final List<File> files) {
        _files = files;
    }

    /**
     * Returns the name of the archive to be decompressed.
     *
     * @return the name of the archive to be decompressed.
     */
    public String getArchiveName() {
        return _archiveName;
    }

    /**
     * Sets the name of the archive to be decompressed.
     *
     * @param archiveName the name of the archive to be decompressed.
     */
    public void setArchiveName(final String archiveName) {
        _archiveName = archiveName;
    }

    /**
     * Returns the output path for either the archive or the decompressed files.
     *
     * @return the output path for either the archive or the decompressed files.
     */
    public String getOutputPath() {
        return _outputPath;
    }

    /**
     * Sets the output path for either the archive or the decompressed files.
     *
     * @param outputPath the output path for either the archive or the
     * decompressed files.
     */
    public void setOutputPath(final String outputPath) {
        _outputPath = outputPath;
    }

    @Override
    public String toString() {
        return "ArchiveInfo{" +
                "\nArchive type : " + _archiveType +
                ",\nCompression level : " + _level +
                ",\nFiles : " + _files +
                ",\nArchive name : " + _archiveName +
                ",\nOutput path : " + _outputPath + '}';
    }
}
