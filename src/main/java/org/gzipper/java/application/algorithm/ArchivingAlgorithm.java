/*
 * Copyright (C) 2018 Matthias Fussenegger
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

import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.gzipper.java.application.util.FileUtils;
import org.gzipper.java.application.util.StringUtils;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.util.Log;

import java.io.*;
import java.util.Arrays;

/**
 * @author Matthias Fussenegger
 */
public abstract class ArchivingAlgorithm extends AbstractAlgorithm {

    /**
     * Type of the archive stream.
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
     * @param archiveType     the type of the archive.
     * @param compressionType the type of the compressor stream.
     */
    protected ArchivingAlgorithm(String archiveType, String compressionType) {
        _archiveType = archiveType;
        _compressionType = compressionType;
        _archiveStreamFactory = new ArchiveStreamFactory();
        _compressorStreamFactory = new CompressorStreamFactory();
    }

    @Override
    public final void extract(String location, String fullname)
            throws IOException, ArchiveException, CompressorException {

        final File archive = new File(fullname);
        initAlgorithmProgress(archive);

        try (final FileInputStream fis = new FileInputStream(archive);
             final BufferedInputStream bis = new BufferedInputStream(fis);
             final CompressorInputStream cis = makeCompressorInputStream(bis);
             final ArchiveInputStream ais = cis != null
                     ? makeArchiveInputStream(cis)
                     : makeArchiveInputStream(bis)) {

            ArchiveEntry entry = ais.getNextEntry();
            fullname = FileUtils.normalize(fullname);

            final String displayName = FileUtils.getDisplayName(fullname);
            final String outputFolderName = FileUtils.combine(location, displayName);
            final File outputFolder = new File(outputFolderName);

            if (createOutputFolderIfNotExists(outputFolder)) {
                Log.e(I18N.getString("errorCreatingDirectory.text", FileUtils.getPath(outputFolder)));
                throw new IOException(String.format("%s could not be created", FileUtils.getPath(outputFolder)));
            }

            while (!interrupt && entry != null) {
                final String entryName = entry.getName();
                if (filterPredicate.test(entryName)) { // check predicate first
                    final String uniqueName = FileUtils.generateUniqueFilename(
                            FileUtils.getPath(outputFolder), entryName);
                    final File newFile = new File(uniqueName);
                    // check if entry contains a directory
                    if (entryName.indexOf('/') > -1) {
                        if (!newFile.getParentFile().exists()) {
                            // also create parent directories by calling "mkdirs"
                            if (!newFile.getParentFile().mkdirs()) {
                                final String parentFilePath = FileUtils.getPath(newFile.getParentFile());
                                Log.e(I18N.getString("errorCreatingDirectory.text", parentFilePath));
                                throw new IOException(String.format("%s could not be created", parentFilePath));
                            }
                        }
                    }
                    if (!entry.isDirectory()) {
                        // create new output stream and write bytes to file
                        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile))) {
                            final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                            int readBytes;
                            while (!interrupt && (readBytes = ais.read(buffer)) != -1) {
                                bos.write(buffer, 0, readBytes);
                                updateProgress(readBytes);
                            }
                        } catch (IOException ex) {
                            if (!interrupt) {
                                Log.e(ex.getLocalizedMessage(), ex);
                                Log.e("{0}\n{1}",
                                        I18N.getString("errorWritingFile.text"),
                                        newFile.getPath()
                                );
                            }
                            throw ex; // re-throw
                        }
                    }
                }
                if (!interrupt) {
                    entry = ais.getNextEntry();
                }
            }
        }
    }

    @Override
    public final void compress(File[] files, String location, String name)
            throws IOException, ArchiveException, CompressorException {

        String archiveName = FileUtils.combine(location, name);
        initAlgorithmProgress(files);

        try (final FileOutputStream fos = new FileOutputStream(archiveName);
             final BufferedOutputStream bos = new BufferedOutputStream(fos);
             final CompressorOutputStream cos = makeCompressorOutputStream(bos);
             final ArchiveOutputStream aos = cos != null
                     ? makeArchiveOutputStream(cos)
                     : makeArchiveOutputStream(bos)) {

            String basePath = StringUtils.EMPTY;
            compress(files, basePath, aos, archiveName);
        }
    }

    private void compress(File[] files, String base, ArchiveOutputStream aos, String archiveName) throws IOException {

        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int readBytes;

        if (files.length > 0) {
            for (int i = 0; !interrupt && i < files.length; ++i) {
                // create next file and define entry name based on folder level
                final File newFile = files[i];
                final String entryName = base + newFile.getName();
                if (newFile.isFile()) {
                    // check predicate first
                    if (!filterPredicate.test(newFile.getName())) {
                        continue; // skip entry
                    }
                    // read and compress the file
                    try (BufferedInputStream buf = new BufferedInputStream(new FileInputStream(newFile))) {
                        // create next archive entry and put it on output stream
                        ArchiveEntry entry = aos.createArchiveEntry(newFile, entryName);
                        aos.putArchiveEntry(entry);
                        // write bytes to file
                        while (!interrupt && (readBytes = buf.read(buffer)) != -1) {
                            aos.write(buffer, 0, readBytes);
                            updateProgress(readBytes);
                        }
                        aos.closeArchiveEntry();
                    } catch (IOException ex) {
                        if (!interrupt) {
                            Log.e(ex.getLocalizedMessage(), ex);
                            Log.e("{0}\n{1}", I18N.getString("errorReadingFile.text"), newFile.getPath());
                            throw ex; // re-throw
                        }
                    }
                } else if (newFile.isDirectory()) {
                    final File[] children = getChildrenExcludingArchiveToBeCreated(archiveName, newFile);
                    compress(children, entryName + "/", aos, archiveName);
                } else {
                    Log.i(I18N.getString("skippingUnsupportedFile.text"), true, FileUtils.getPath(newFile));
                }
            }
        }
    }

    private boolean createOutputFolderIfNotExists(File outputFolder) {
        return !outputFolder.exists() && !outputFolder.mkdir();
    }

    private File[] getChildrenExcludingArchiveToBeCreated(String archiveName, File directory) {

        File[] children = getFiles(FileUtils.getPath(directory));

        children = Arrays.stream(children)
                .filter(file -> !FileUtils.getPath(file).equals(archiveName))
                .toArray(File[]::new);

        return children;
    }

    /**
     * Creates a new instance of an {@link ArchiveInputStream}. This can be used
     * so that specific algorithms can e.g. skip the archive stream.
     *
     * @param stream the {@link InputStream} being used when creating a new
     *               {@link ArchiveInputStream}.
     * @return new instance of {@link ArchiveInputStream}.
     * @throws ArchiveException if an error related to the archiver occurs.
     */
    protected ArchiveInputStream makeArchiveInputStream(InputStream stream) throws ArchiveException {
        return _archiveStreamFactory.createArchiveInputStream(_archiveType, stream);
    }

    /**
     * Creates a new instance of {@link CompressorInputStream}. This can be used
     * so that specific algorithms can e.g. apply individual parameters.
     *
     * @param stream the {@link InputStream} being used when creating a new
     *               {@link CompressorInputStream}.
     * @return new instance of {@link CompressorInputStream}.
     * @throws java.io.IOException if an I/O error occurs.
     * @throws CompressorException if an error related to the compressor occurs.
     */
    protected CompressorInputStream makeCompressorInputStream(InputStream stream)
            throws IOException, CompressorException {
        return _compressorStreamFactory.createCompressorInputStream(_compressionType, stream);
    }

    /**
     * Creates a new instance of an {@link ArchiveOutputStream}. This can be
     * used so that specific algorithms can e.g. skip the archive stream.
     *
     * @param stream the {@link OutputStream} being used when creating a new
     *               {@link ArchiveOutputStream}.
     * @return new instance of {@link ArchiveOutputStream}.
     * @throws ArchiveException if an error related to the archiver occurs.
     */
    protected ArchiveOutputStream makeArchiveOutputStream(OutputStream stream) throws ArchiveException {
        return _archiveStreamFactory.createArchiveOutputStream(_archiveType, stream);
    }

    /**
     * Creates a new instance of {@link CompressorOutputStream}. This can be
     * used so that specific algorithms can e.g. apply individual parameters.
     *
     * @param stream the {@link OutputStream} being used when creating a new
     *               {@link CompressorOutputStream}.
     * @return new instance of {@link CompressorOutputStream}.
     * @throws IOException         if an I/O error occurs.
     * @throws CompressorException if an error related to the compressor occurs.
     */
    protected CompressorOutputStream makeCompressorOutputStream(OutputStream stream)
            throws IOException, CompressorException {
        return _compressorStreamFactory.createCompressorOutputStream(_compressionType, stream);
    }
}
