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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.gzipper.java.application.pojo.ArchiveInfo;
import org.gzipper.java.application.util.FileUtil;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.presentation.control.MainViewController;

/**
 * Abstract class that offers generally used attributes and methods for
 * archiving algorithms. Any class that represents an archiving algorithm will
 * derive from this class.
 *
 * @author Matthias Fussenegger
 */
public abstract class AbstractAlgorithm implements ArchivingAlgorithm {

    /**
     * The logger this class uses to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(MainViewController.class.getName());

    /**
     * True if this algorithm is performing an operation, false otherwise.
     */
    private volatile boolean _interrupt = false;

    /**
     * The compression level. Will only be considered if supported by algorithm.
     */
    protected int _compressionLevel;

    /**
     * Type of the archive.
     */
    protected final String _archiveType;

    /**
     * Type of compressor stream.
     */
    protected final String _compressionType;

    /**
     * Factory to create archive streams.
     */
    protected final ArchiveStreamFactory _archiveStreamFactory;

    /**
     * Factory to create compressor streams.
     */
    protected final CompressorStreamFactory _compressorStreamFactory;

    /**
     * Creates a new instance for archiving operations.
     *
     * @param archiveType the type of the archive.
     * @param compressionType the type of the compressor stream.
     */
    protected AbstractAlgorithm(String archiveType, String compressionType) {
        _archiveType = archiveType;
        _compressionType = compressionType;
        _archiveStreamFactory = new ArchiveStreamFactory();
        _compressorStreamFactory = new CompressorStreamFactory();
    }

    @Override
    public void extract(String location, String name) throws IOException, ArchiveException, CompressorException {

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(name));
        try (ArchiveInputStream inputStream = _archiveStreamFactory.createArchiveInputStream(_archiveType, bis)) {

            ArchiveEntry entry = inputStream.getNextEntry();

            // create main folder of archive without file extension
            File folder = new File(location + name.substring(
                    name.lastIndexOf(File.separator) + 1,
                    name.lastIndexOf('.')));

            if (!folder.exists()) {
                folder.mkdir();
            }

            while (!_interrupt && entry != null) {
                String entryName = entry.getName();

                LOGGER.log(Level.INFO, "{0}{1}{2}", new Object[]{
                    I18N.getString("extracting.text"), " ", entryName});

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
                    while (!_interrupt && (readBytes = inputStream.read(buffer)) != -1) {
                        buf.write(buffer, 0, readBytes);
                    }
                }

                LOGGER.log(Level.INFO, "{0}{1}{2}", new Object[]{
                    entryName, " ", I18N.getString("extracted.text")});
                entry = inputStream.getNextEntry();
            }
        }
    }

    @Override
    public void extract(ArchiveInfo info) throws IOException, ArchiveException, CompressorException {
        extract(info.getOutputPath(), info.getArchiveName());
    }

    @Override
    public void compress(File[] files, String location, String name)
            throws IOException, ArchiveException, CompressorException {

        final String fullName = FileUtil.combinePathAndFileName(location, name);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fullName));

        CompressorOutputStream cos = makeCompressorOutputStream(bos);
        try (ArchiveOutputStream aos = cos != null
                ? makeArchiveOutputStream(cos)
                : makeArchiveOutputStream(bos)) {
            compress(files, "", aos);
        }
    }

    @Override
    public void compress(ArchiveInfo info) throws IOException, ArchiveException, CompressorException {
        final File[] files = new File[info.getFiles().size()];
        _compressionLevel = info.getLevel();
        compress(info.getFiles().toArray(files), info.getOutputPath(), info.getArchiveName());
    }

    /**
     * Private method for compression and recursive call.
     *
     * @param files the files to compress.
     * @param base the base path to store files to.
     * @param outputStream the output stream to use.
     * @throws IOException if an I/O error occurs.
     */
    private void compress(File[] files, String base, ArchiveOutputStream outputStream) throws IOException {

        byte[] buffer = new byte[4096];
        int readBytes;

        if (files.length > 0) {
            for (int i = 0; !_interrupt && i < files.length; ++i) {
                // create next file and define entry name based on folder level
                File newFile = files[i];
                String entryName = base + newFile.getName();
                // start compressing the file
                if (newFile.isFile()) {
                    LOGGER.log(Level.INFO, "{0}{1}{2}", new Object[]{
                        I18N.getString("compressing.text"),
                        " ",
                        newFile.getName()});
                    try (BufferedInputStream buf = new BufferedInputStream(
                            new FileInputStream(newFile))) {
                        // create next archive entry and put it on output stream
                        ArchiveEntry entry = outputStream.createArchiveEntry(newFile, entryName);
                        outputStream.putArchiveEntry(entry);
                        // write bytes to file
                        while (!_interrupt && (readBytes = buf.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, readBytes);
                        }
                        outputStream.closeArchiveEntry();
                    }
                    LOGGER.log(Level.INFO, "{0}{1}{2}", new Object[]{
                        newFile.getName(),
                        " ",
                        I18N.getString("compressed.text")});
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
     * @param path the path that contains the files to be compressed.
     * @return an array of files from the specified path.
     * @throws IOException if an I/O error occurs.
     */
    private File[] getFiles(String path) throws IOException {
        final File dir = new File(path);
        File[] files = dir.listFiles();
        return files;
    }

    @Override
    public ArchiveOutputStream makeArchiveOutputStream(
            OutputStream stream) throws IOException, ArchiveException {
        return _archiveStreamFactory.createArchiveOutputStream(_archiveType, stream);
    }

    @Override
    public CompressorOutputStream makeCompressorOutputStream(
            OutputStream stream) throws IOException, CompressorException {
        return _compressorStreamFactory.createCompressorOutputStream(_compressionType, stream);
    }

    @Override
    public void interrupt() {
        _interrupt = true;
    }
}
