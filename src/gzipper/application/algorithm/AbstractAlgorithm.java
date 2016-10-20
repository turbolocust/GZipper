/*
 * Copyright (C) 2016 Matthias Fussenegger
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
package gzipper.application.algorithm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 * Abstract class that offers generally used attributes and methods for
 * archiving algorithms. Any class that contains an archiving algorithm will
 * extend this class.
 *
 * @author Matthias Fussenegger
 */
public abstract class AbstractAlgorithm implements ArchivingAlgorithm {

    /**
     * Type of the archive.
     */
    private final String _archiveType;

    /**
     * Type of compressor stream.
     */
    private final String _compressionType;

    /**
     * Factory to create archive streams.
     */
    private static final ArchiveStreamFactory ARCHIVE_STREAM_FACTORY;

    /**
     * Factory to create compressor streams.
     */
    private static final CompressorStreamFactory COMPRESSOR_STREAM_FACTORY;

    static {
        ARCHIVE_STREAM_FACTORY = new ArchiveStreamFactory();
        COMPRESSOR_STREAM_FACTORY = new CompressorStreamFactory();
    }

    /**
     * Creates a new object of the child class for archiving operations.
     *
     * @param archiveType The type of the archive
     * @param compressionType The type of the compressor stream
     */
    protected AbstractAlgorithm(String archiveType, String compressionType) {
        _archiveType = archiveType;
        _compressionType = compressionType;
    }

    @Override
    public void extract(String location, String name) throws IOException, ArchiveException, CompressorException {

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(location + name));
        CompressorInputStream cis = COMPRESSOR_STREAM_FACTORY.createCompressorInputStream(_compressionType, bis);

        try (ArchiveInputStream inputStream = ARCHIVE_STREAM_FACTORY.createArchiveInputStream(_archiveType, cis)) {

            ArchiveEntry entry = inputStream.getNextEntry();

            // create main folder of archive without file type
            File folder = new File(location + name.substring(0, name.lastIndexOf('.')));

            if (!folder.exists()) {
                folder.mkdir();
            }

            while (entry != null) {
                String entryName = entry.getName();
                // check if entry contains a directory
                if (entryName.contains(File.separator)) {

                    File newFile = new File(folder.getAbsolutePath() + File.separator + entryName);

                    if (!newFile.getParentFile().exists()) {
                        newFile.getParentFile().mkdirs(); // also creates parent directories
                    }
                }

                final String newFilePath = folder.getAbsolutePath() + File.separator + entryName;

                // create new output stream and write bytes to file
                try (BufferedOutputStream buf = new BufferedOutputStream(
                        new FileOutputStream(newFilePath))) {
                    byte[] buffer = new byte[4096];
                    int readBytes;
                    while ((readBytes = inputStream.read(buffer)) != -1) {
                        buf.write(buffer, 0, readBytes);
                    }
                }

                entry = inputStream.getNextEntry();
            }
        }
    }

    @Override
    public void compress(File[] files, String location, String name) throws IOException, ArchiveException, CompressorException {

        // check if location ends with separator, which is required for output stream
        String path = location.endsWith(File.separator) ? location : location + File.separator;

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path + name));
        CompressorOutputStream cos = COMPRESSOR_STREAM_FACTORY.createCompressorOutputStream(_compressionType, bos);

        try (ArchiveOutputStream outputStream
                = ARCHIVE_STREAM_FACTORY.createArchiveOutputStream(_archiveType, cos)) {
            compress(files, name, outputStream);
        }
    }

    /**
     * Internal method for compression and recursive call.
     *
     * @param files The files to compress
     * @param base The base path to store files to
     * @param outputStream The output stream to use
     * @throws IOException
     */
    private void compress(File[] files, String base, ArchiveOutputStream outputStream) throws IOException {

        byte[] buffer = new byte[4096];
        int readBytes;

        if (files.length > 0) {
            for (int i = 0; i < files.length; ++i) {
                // create next file and define entry name based on folder level
                File newFile = files[i];
                String entryName = base + newFile.getName();
                // start compressing the file
                if (newFile.isFile()) {
                    try (BufferedInputStream buf = new BufferedInputStream(
                            new FileInputStream(newFile))) {
                        // create next archive entry and put it on output stream
                        ArchiveEntry entry = outputStream.createArchiveEntry(newFile, entryName);
                        outputStream.putArchiveEntry(entry);
                        // write bytes to file
                        while ((readBytes = buf.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, readBytes);
                        }
                        outputStream.closeArchiveEntry();
                    }
                } else { // child is a directory
                    File[] children = getFiles(newFile.getAbsolutePath());
                    compress(children, entryName + File.separator, outputStream);
                }
            }
        }
    }

    /**
     * Retrieves files from a specific directory; mandatory for compression.
     *
     * @param path The path that contains the files to be compressed
     * @return And array of files from the specified path
     * @throws IOException If an error occurred
     */
    private File[] getFiles(String path) throws IOException {
        final File dir = new File(path);
        File[] files = dir.listFiles();
        return files;
    }
}
