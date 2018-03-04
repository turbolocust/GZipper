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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.Deflater;
import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.application.util.FileUtils;
import org.gzipper.java.exceptions.GZipperException;

/**
 * Factory class that offers static methods for creating {@link ArchiveInfo}.
 *
 * @author Matthias Fussenegger
 */
public final class ArchiveInfoFactory {

    private ArchiveInfoFactory() {
        throw new AssertionError("Holds static members only.");
    }

    /**
     * Creates a new {@link ArchiveInfo} for compression operation.
     *
     * @param archiveType the type of the archive, see {@link ArchiveType}.
     * @param archiveName the name of the archive to be created.
     * @param level the compression level of the archive.
     * @param files the files to be compressed.
     * @param outputPath the path where to save the archive.
     * @return {@link ArchiveInfo} that may be used for an operation.
     * @throws GZipperException if archive type could not be determined.
     */
    public static ArchiveInfo createArchiveInfo(ArchiveType archiveType, String archiveName,
            int level, List<File> files, String outputPath) throws GZipperException {

        if (archiveType == null) {
            throw new NullPointerException("Archive type must not be null.");
        } else if (level < Deflater.DEFAULT_COMPRESSION || level > Deflater.BEST_COMPRESSION) {
            throw GZipperException.createWithReason(
                    GZipperException.Reason.FAULTY_COMPRESSION_LVL,
                    "Faulty compression level specified.");
        }

        boolean hasExtension = false;
        final String[] extNames = archiveType.getExtensionNames(false);
        for (String extName : extNames) {
            if (archiveName.endsWith(extName)) {
                hasExtension = true;
                break;
            }
        }

        if (!hasExtension) {
            // add extension to archive name if missing and ignore the asterisk
            archiveName = archiveName + extNames[0];
        }

        return new ArchiveInfo(archiveType, archiveName, level, files, outputPath);
    }

    /**
     * Creates a new {@link ArchiveInfo} for compression operation. This will
     * generate a unique archive name for each file provided.
     *
     * @param archiveType the type of the archive, see {@link ArchiveType}.
     * @param archiveName the name of the archive to be created.
     * @param level the compression level of the archive.
     * @param files the files to be compressed.
     * @param outputPath the path where to save the archive.
     * @return List consisting of {@link ArchiveInfo} objects.
     * @throws GZipperException if archive type could not be determined.
     */
    public static List<ArchiveInfo> createArchiveInfos(ArchiveType archiveType, String archiveName,
            int level, List<File> files, String outputPath) throws GZipperException {

        final Set<String> names = new HashSet<>(); // to avoid name collisions
        List<ArchiveInfo> infos = new ArrayList<>(files.size());
        List<File> fileList; // used to be compatible with current API
        String name; // holds the name of the archive
        int nameSuffix = -1; // will be appended if necessary

        for (File next : files) {
            fileList = new LinkedList<>();
            fileList.add(next);
            do {
                ++nameSuffix;
                name = new File(FileUtils.generateUniqueFilename(
                        outputPath, archiveName, nameSuffix)).getName();
            } while (names.contains(name));
            names.add(name);
            ArchiveInfo info = createArchiveInfo(
                    archiveType, name, level, fileList, outputPath);
            infos.add(info);
        }
        return infos;
    }

    /**
     * Creates a new {@link ArchiveInfo} for decompression operation.
     *
     * @param archiveType the type of the archive, see {@link ArchiveType}.
     * @param archiveName the name of the archive to be extracted.
     * @param outputPath the path where to extract the archive.
     * @return {@link ArchiveInfo} that may be used for an operation.
     * @throws GZipperException if archive type could not be determined.
     */
    public static ArchiveInfo createArchiveInfo(ArchiveType archiveType,
            String archiveName, String outputPath) throws GZipperException {

        if (archiveType == null) {
            throw new NullPointerException("Archive type must not be null.");
        }
        return new ArchiveInfo(archiveType, archiveName, 0, null, outputPath);
    }
}
