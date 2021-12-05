/*
 * Copyright (C) 2019 Matthias Fussenegger
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
import org.gzipper.java.application.util.StringUtils;
import org.gzipper.java.exceptions.GZipperException;

/**
 * Factory class that offers static methods for creating {@link ArchiveInfo}.
 *
 * @author Matthias Fussenegger
 */
public final class ArchiveInfoFactory {

    private ArchiveInfoFactory() {
        throw new AssertionError("Holds static members only");
    }

    /**
     * Creates a new {@link ArchiveInfo} for a compression operation.
     *
     * @param archiveType the type of the archive, see {@link ArchiveType}.
     * @param archiveName the name of the archive to be created.
     * @param level       the compression level of the archive.
     * @param files       the files to be compressed.
     * @param outputPath  the path where to save the archive.
     * @return a new {@link ArchiveInfo} object.
     * @throws GZipperException if archive type could not be determined.
     */
    public static ArchiveInfo createArchiveInfo(
            ArchiveType archiveType, String archiveName,
            int level, List<File> files, String outputPath) throws GZipperException {

        if (archiveType == null) {
            throw new NullPointerException("Archive type must not be null");
        } else throwGZipperExceptionIfFaultyCompressionLevelSpecified(level);

        final String properName = checkAddExtension(archiveName, archiveType);
        final String ext = FileUtils.getExtension(properName);
        final String displayName = properName.replace(ext, StringUtils.EMPTY);

        final String fullName = FileUtils.generateUniqueFilename(outputPath, displayName, ext, 0);
        String name = FileUtils.getName(fullName);

        return new ArchiveInfo(archiveType, name, level, files, outputPath);
    }

    /**
     * Creates a new {@link ArchiveInfo} for a compression operation. This will generate a unique archive name
     * for each file provided, using the specified archive name so that each file starts with the same name and
     * ends with a unique identifier (or number) starting from one ({@code 1}).
     *
     * @param archiveType the type of the archive, see {@link ArchiveType}.
     * @param archiveName the name of the archive to be created.
     * @param level       the compression level of the archive.
     * @param files       the files to be compressed.
     * @param outputPath  the path where to save the archive.
     * @return a list consisting of {@link ArchiveInfo} objects.
     * @throws GZipperException if archive type could not be determined.
     */
    public static List<ArchiveInfo> createArchiveInfos(
            ArchiveType archiveType, String archiveName,
            int level, List<File> files, String outputPath) throws GZipperException {

        if (archiveType == null) {
            throw new NullPointerException("Archive type must not be null");
        }

        throwGZipperExceptionIfFaultyCompressionLevelSpecified(level);

        final String properName = checkAddExtension(archiveName, archiveType);
        final String ext = FileUtils.getExtension(properName);
        final String displayName = properName.replace(ext, StringUtils.EMPTY);

        final Set<String> names = new HashSet<>(); // to avoid name collisions
        List<ArchiveInfo> archiveInfos = new ArrayList<>(files.size());
        String fullName, name; // hold the names of the (next) archive
        int nameSuffix = 0; // will be appended if necessary

        for (File nextFile : files) {

            List<File> fileList = new LinkedList<>();
            fileList.add(nextFile);

            do {
                ++nameSuffix;
                fullName = displayName + nameSuffix;
                fullName = FileUtils.generateUniqueFilename(outputPath, fullName, ext, nameSuffix);
                name = FileUtils.getName(fullName);
            } while (names.contains(name));

            names.add(name);
            ArchiveInfo info = new ArchiveInfo(archiveType, name, level, fileList, outputPath);
            archiveInfos.add(info);
        }

        return archiveInfos;
    }

    /**
     * Creates a new {@link ArchiveInfo} for a compression operation. This will generate a unique archive name
     * for each file provided if it already exists, so that the file ends with a unique identifier (or number).
     *
     * @param archiveType the type of the archive, see {@link ArchiveType}.
     * @param level       the compression level of the archive.
     * @param files       the files to be compressed.
     * @param outputPath  the path where to save the archive.
     * @return a list consisting of {@link ArchiveInfo} objects.
     * @throws GZipperException if archive type could not be determined.
     */
    public static List<ArchiveInfo> createArchiveInfos(ArchiveType archiveType, int level, List<File> files,
                                                       String outputPath) throws GZipperException {

        if (archiveType == null) {
            throw new NullPointerException("Archive type must not be null");
        }

        throwGZipperExceptionIfFaultyCompressionLevelSpecified(level);

        final String ext = archiveType.getDefaultExtensionName();
        final int nameSuffix = 1; // will be appended if necessary
        List<ArchiveInfo> archiveInfos = new ArrayList<>(files.size());

        for (File nextFile : files) {

            List<File> fileList = new LinkedList<>();
            fileList.add(nextFile);

            String fullName = FileUtils.generateUniqueFilename(outputPath, nextFile.getName(), ext, nameSuffix);
            String name = FileUtils.getName(fullName);

            ArchiveInfo info = new ArchiveInfo(archiveType, name, level, fileList, outputPath);
            archiveInfos.add(info);
        }

        return archiveInfos;
    }

    /**
     * Creates a new {@link ArchiveInfo} for a decompression operation.
     *
     * @param archiveType the type of the archive, see {@link ArchiveType}.
     * @param archiveName the name of the archive to be extracted.
     * @param outputPath  the path where to extract the archive.
     * @return {@link ArchiveInfo} that may be used for an operation.
     */
    public static ArchiveInfo createArchiveInfo(ArchiveType archiveType, String archiveName, String outputPath) {

        if (archiveType == null) {
            throw new NullPointerException("Archive type must not be null");
        }

        return new ArchiveInfo(archiveType, archiveName, 0, null, outputPath);
    }

    private static String checkAddExtension(String archiveName, ArchiveType archiveType) {

        String name = archiveName;
        boolean hasExtension = false;

        final String[] extNames = archiveType.getExtensionNames(false);

        for (String extName : extNames) {
            if (archiveName.endsWith(extName)) {
                hasExtension = true;
                break;
            }
        }

        if (!hasExtension) {
            name = archiveName + extNames[0]; // add extension to archive name if missing and ignore the asterisk
        }

        return name;
    }

    private static void throwGZipperExceptionIfFaultyCompressionLevelSpecified(int level) throws GZipperException {

        if (level < Deflater.DEFAULT_COMPRESSION || level > Deflater.BEST_COMPRESSION) {
            throw GZipperException.createWithReason(
                    GZipperException.Reason.FAULTY_COMPRESSION_LVL,
                    "Faulty compression level specified");
        }
    }
}
