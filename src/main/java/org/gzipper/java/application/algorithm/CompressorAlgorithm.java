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
package org.gzipper.java.application.algorithm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.gzipper.java.application.util.FileUtils;
import org.gzipper.java.application.util.StringUtils;
import org.gzipper.java.exceptions.GZipperException;

/**
 *
 * @author Matthias Fussenegger
 */
public abstract class CompressorAlgorithm extends AbstractAlgorithm {

    @Override
    public final void compress(File[] files, String location, String name)
            throws IOException, ArchiveException, CompressorException {

        initAlgorithmProgress(files);
        final String fullname = FileUtils
                .combinePathAndFilename(location, name);

        if (files.length > 0 && files[0].isFile()) {
            // handling first file only, this way the current
            // API does not need to be changed/more complicated
            final File file = files[0];
            // check predicate first
            if (!_filterPredicate.test(file.getName())) {
                return; // ignore file
            }
            final CompressorOptions options = new CompressorOptions(
                    file.getName(), _compressionLevel);

            try (final FileInputStream fis = new FileInputStream(file);
                    final BufferedInputStream bis = new BufferedInputStream(fis);
                    final CompressorOutputStream cos = makeCompressorOutputStream(
                            new BufferedOutputStream(new FileOutputStream(fullname)), options)) {
                final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int readBytes = 0;
                while (!_interrupt && (readBytes = bis.read(buffer)) != -1) {
                    cos.write(buffer, 0, readBytes);
                    updateProgress(readBytes);
                }
            }
        } else {
            throw new IOException(GZipperException.createWithReason(
                    GZipperException.Reason.NO_DIR_SUPPORTED,
                    "Directories are not supported!"));
        }
    }

    @Override
    public final void extract(String location, String fullname)
            throws IOException, ArchiveException, CompressorException {

        final File archive = new File(fullname);

        // check predicate first
        if (!_filterPredicate.test(archive.getName())) {
            return; // ignore file
        }

        initAlgorithmProgress(archive);
        CompressorOptions options = new CompressorOptions();

        try (final CompressorInputStream gcis = makeCompressorInputStream(
                new BufferedInputStream(new FileInputStream(archive)), options)) {

            final File outputFile;
            if (StringUtils.isNullOrEmpty(options._name)) {
                // generate unique file name from display name
                outputFile = new File(FileUtils.generateUniqueFilename(
                        location, FileUtils.getDisplayName(fullname)));
            } else { // create output file with name as defined in header
                outputFile = new File(FileUtils.generateUniqueFilename(
                        location, options._name));
            }
            try (final BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(outputFile))) {
                final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int readBytes = 0;
                while (!_interrupt && (readBytes = gcis.read(buffer)) != -1) {
                    bos.write(buffer, 0, readBytes);
                    updateProgress(readBytes);
                }
            }
        }
    }

    /**
     * Creates a new instance of {@link CompressorInputStream}. This is used so
     * that specific algorithms can e.g. apply individual parameters.
     *
     * @param stream the {@link InputStream} being used when creating a new
     * {@link CompressorInputStream}.
     * @param options Options that may be applied.
     * @return new instance of {@link CompressorInputStream}.
     * @throws java.io.IOException if an I/O error occurs.
     */
    protected abstract CompressorInputStream makeCompressorInputStream(
            InputStream stream, CompressorOptions options) throws IOException;

    /**
     * Creates a new instance of {@link CompressorOutputStream}. This is used so
     * that specific algorithms can e.g. apply individual parameters.
     *
     * @param stream the {@link OutputStream} being used when creating a new
     * {@link CompressorOutputStream}.
     * @param options Options that may be applied.
     * @return new instance of {@link CompressorOutputStream}.
     * @throws IOException if an I/O error occurs.
     */
    protected abstract CompressorOutputStream makeCompressorOutputStream(
            OutputStream stream, CompressorOptions options) throws IOException;

    /**
     * Consists of additional information for the subclass and functions as a
     * data exchange object for e.g. the file name.
     */
    protected static class CompressorOptions {

        /**
         * The name of the archive or the file to be compressed.
         */
        private String _name;

        /**
         * The compression level to be applied if supported.
         */
        private int _level;

        public CompressorOptions() {
        }

        public CompressorOptions(String name, int level) {
            _name = name;
            _level = level;
        }

        /**
         * Returns the name of the archive or the file to be compressed.
         *
         * @return the name of the archive or the file to be compressed.
         */
        public final String getName() {
            return _name;
        }

        /**
         * Sets the name of the archive or the file to be compressed.
         *
         * @param name the name of the archive or the file to be compressed.
         */
        public final void setName(String name) {
            _name = name;
        }

        /**
         * Returns the compression level.
         *
         * @return the compression level.
         */
        public final int getLevel() {
            return _level;
        }

        /**
         * Sets the compression level.
         *
         * @param level the compression level.
         */
        public final void setLevel(int level) {
            _level = level;
        }
    }
}
