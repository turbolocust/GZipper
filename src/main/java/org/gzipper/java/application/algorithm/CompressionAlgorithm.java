/*
 * Copyright (C) 2017 Matthias Fussenegger
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gzipper.java.application.algorithm;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.gzipper.java.application.observer.Notifier;
import org.gzipper.java.application.ArchiveInfo;
import org.gzipper.java.application.concurrency.Interruptible;

/**
 * Any implementing class offers methods for compression and decompression.
 *
 * @author Matthias Fussenegger
 */
public interface CompressionAlgorithm extends Interruptible, Notifier<Integer> {

    /**
     * The default buffer size for chunks.
     */
    int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Compresses files using the algorithm of the concrete class with default
     * settings and stores an archive to the specified path.
     *
     * @param files    the files selected from the file chooser.
     * @param location defines where to store the archive.
     * @param name     the name of the archive without the directory path.
     * @throws IOException         if an I/O error occurs.
     * @throws ArchiveException    if an error related to the archiver occurs.
     * @throws CompressorException if an error related to the compressor occurs.
     */
    void compress(File[] files, String location, String name)
            throws IOException, ArchiveException, CompressorException;

    /**
     * Compresses files using the algorithm of the concrete class with default
     * settings and stores an archive to the path specified in
     * {@link ArchiveInfo}.
     *
     * @param info POJO that holds information required for compression.
     * @throws IOException         if an I/O error occurs.
     * @throws ArchiveException    if an error related to the archiver occurs.
     * @throws CompressorException if an error related to the compressor occurs.
     */
    void compress(ArchiveInfo info)
            throws IOException, ArchiveException, CompressorException;

    /**
     * Extracts an archive using the algorithm of the concrete class and stores
     * the files of the archive to the specified path.
     *
     * @param location the location where to extract the archive.
     * @param fullname the filename of the archive to extract.
     * @throws IOException         if an I/O error occurs.
     * @throws ArchiveException    if an error related to the archiver occurs.
     * @throws CompressorException if an error related to the compressor occurs.
     */
    void extract(String location, String fullname)
            throws IOException, ArchiveException, CompressorException;

    /**
     * Extracts an archive using the algorithm of the concrete class and stores
     * the files of the archive to the path specified in {@link ArchiveInfo}.
     *
     * @param info POJO that holds information required for extraction.
     * @throws IOException         if an I/O error occurs.
     * @throws ArchiveException    if an error related to the archiver occurs.
     * @throws CompressorException if an error related to the compressor occurs.
     */
    void extract(ArchiveInfo info)
            throws IOException, ArchiveException, CompressorException;

    /**
     * Sets the specified {@link Predicate} which will be used as a filter when
     * compressing files or decompressing archive entries by evaluating the name
     * of the file or entry.
     *
     * @param predicate the {@link Predicate} to be used.
     */
    void setPredicate(Predicate<String> predicate);

}
