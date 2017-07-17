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

    /**
     * The file that will be compressed.
     */
    private File _file;

    public Gzip() {
        super(null, CompressorStreamFactory.GZIP);
    }

    @Override
    public CompressorOutputStream makeCompressorOutputStream(OutputStream stream)
            throws IOException, CompressorException {
        // set additional parameters for compressor stream
        GzipParameters params = getDefaultGzipParams(_file.getName());
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

        initAlgorithmProgress(files);
        final String fullname = FileUtils.combinePathAndFilename(location, name);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fullname));
        if (files.length > 0 && files[0].isFile()) { // directories are not supported
            final File file = _file = files[0];
            try (BufferedInputStream bis
                    = new BufferedInputStream(new FileInputStream(file))) {
                try (CompressorOutputStream cos = makeCompressorOutputStream(bos)) {
                    final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    long totalBytesRead = 0L;
                    int readBytes = 0;
                    while (!_interrupt && (readBytes = bis.read(buffer)) != -1) {
                        cos.write(buffer, 0, readBytes);
                        totalBytesRead += readBytes;
                        updateProgress(totalBytesRead);
                    }
                }
            }
        } else {
            throw new IOException(new GZipperException("Directories are not supported!"));
        }
    }

    @Override
    public void extract(String location, String fullname)
            throws IOException, ArchiveException, CompressorException {

        File archive = new File(fullname);
        initAlgorithmProgress(archive);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(archive));
        try (GzipCompressorInputStream gcis = new GzipCompressorInputStream(bis)) {
            // create output file without last file name extension
            File outputFile = new File(location + gcis.getMetaData().getFilename());
            if (outputFile.getAbsolutePath().equals(fullname)) {
                // generate unique file as input file has no file name extension
                outputFile = new File(FileUtils.generateUniqueFilename(
                        location, outputFile.getName()));
            }
            try (BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(outputFile))) {
                final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int readBytes = 0;
                while (!_interrupt && (readBytes = gcis.read(buffer)) != -1) {
                    bos.write(buffer, 0, readBytes);
                    updateProgress(gcis.getBytesRead());
                }
            }
        }
    }

    /**
     * Returns {@link GzipParameters} with the operating system, modification
     * time (which is the current time in milliseconds) and the specified
     * filename without the directory path already set.
     *
     * @param filename the name of the file without directory path.
     * @return the default {@link GzipParameters}.
     */
    public static GzipParameters getDefaultGzipParams(String filename) {
        GzipParameters params = new GzipParameters();
        Settings settings = Settings.getInstance();
        int osValue = settings.getOs().getOsInfo().getValue();
        params.setOperatingSystem(osValue);
        params.setModificationTime(System.currentTimeMillis());
        if (filename != null && !filename.contains(File.separator)) {
            params.setFilename(filename);
        }
        return params;
    }
}
