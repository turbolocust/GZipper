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
package org.gzipper.java.application.algorithm.type;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.gzipper.java.application.algorithm.AbstractAlgorithm;
import org.gzipper.java.application.util.FileUtils;
import org.gzipper.java.exceptions.GZipperException;
import org.gzipper.java.util.Settings;

/**
 * Represents the GZIP archive type.
 *
 * @author Matthias Fussenegger
 */
public class Gzip extends AbstractAlgorithm {

    public Gzip() {
        super(null, CompressorStreamFactory.GZIP);
    }

    @Override
    public CompressorOutputStream makeCompressorOutputStream(OutputStream stream)
            throws IOException, CompressorException {
        // set additional parameters for compressor stream
        GzipParameters params = new GzipParameters();
        Settings settings = Settings.getInstance();
        params.setOperatingSystem(settings.getOperatingSystem().getOSInfo().getValue());
        params.setCompressionLevel(_compressionLevel);
        return new GzipCompressorOutputStream(stream, params);
    }

    @Override
    public ArchiveOutputStream makeArchiveOutputStream(OutputStream stream)
            throws IOException, ArchiveException {
        return null;
    }

    @Override
    public void compress(File[] files, String location, String name)
            throws IOException, ArchiveException, CompressorException {

        final String fullname = FileUtils.combinePathAndFilename(location, name);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fullname));
        if (files.length > 0 && files[0].isFile()) { // directories are not supported
            final File file = files[0];
            try (BufferedInputStream bis
                    = new BufferedInputStream(new FileInputStream(file))) {
                try (CompressorOutputStream cos = makeCompressorOutputStream(bos)) {
                    final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    int readBytes = 0;
                    while (!_interrupt && (readBytes = bis.read(buffer)) != -1) {
                        cos.write(buffer, 0, readBytes);
                    }
                }
            }
        } else {
            throw new IOException(new GZipperException("Directories are not supported!"));
        }
    }

    @Override
    public void extract(String location, String name)
            throws IOException, ArchiveException, CompressorException {

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(name));
        try (GzipCompressorInputStream gcis = new GzipCompressorInputStream(bis)) {
            // create output file without last file name extension
            File outputFile = new File(location + name.substring(
                    name.lastIndexOf(File.separator) + 1, name.lastIndexOf('.')));
            if (outputFile.getAbsolutePath().equals(name)) {
                // generate unique file as input file has no file name extension
                outputFile = new File(FileUtils.generateUniqueFilename(
                        location, outputFile.getName(), ""));
            }
            try (BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(outputFile))) {
                final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int readBytes = 0;
                while (!_interrupt && (readBytes = gcis.read(buffer)) != -1) {
                    bos.write(buffer, 0, readBytes);
                }
            }
        }
    }
}
