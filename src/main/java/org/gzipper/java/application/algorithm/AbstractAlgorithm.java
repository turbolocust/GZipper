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
package org.gzipper.java.application.algorithm;

import java.io.File;
import java.io.IOException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.gzipper.java.application.observer.NotifierImpl;
import org.gzipper.java.application.pojo.ArchiveInfo;

/**
 * Abstract class that offers generally used attributes and methods for
 * archiving algorithms. Any class that represents an archiving algorithm should
 * derive from this class.
 *
 * @author Matthias Fussenegger
 */
public abstract class AbstractAlgorithm extends NotifierImpl<Integer> implements CompressionAlgorithm {

    /**
     * True if this algorithm is performing an operation, false otherwise.
     */
    protected volatile boolean _interrupt = false;

    /**
     * The compression level. Will only be considered if supported by algorithm.
     */
    protected int _compressionLevel;

    /**
     * Object used to update the progress of the algorithm.
     */
    protected AlgorithmProgress _algorithmProgress;

    /**
     * Retrieves files from a specific directory; mandatory for compression.
     *
     * @param path the path that contains the files to be compressed.
     * @return an array of files from the specified path.
     * @throws IOException if an I/O error occurs.
     */
    protected final File[] getFiles(String path) throws IOException {
        final File dir = new File(path);
        File[] files = dir.listFiles();
        return files;
    }

    /**
     * Initializes {@link #_algorithmProgress} with the specified files.
     *
     * @param files the files to be used for initialization.
     */
    protected final void initAlgorithmProgress(File... files) {
        _algorithmProgress = new AlgorithmProgress(files);
    }

    /**
     * Updates the progress of the current operation and notifies all attached
     * listeners if the new progress using {@code Math.rint(double)} is greater
     * than the previous one. This behavior may be changed by overriding this
     * method.
     *
     * @param readBytes the amount of bytes read so far.
     */
    protected void updateProgress(long readBytes) {
        _algorithmProgress.updateProgress(readBytes);
        changeValue(_algorithmProgress.getProgress());
    }

    @Override
    public void compress(ArchiveInfo info) throws IOException, ArchiveException, CompressorException {
        final File[] files = new File[info.getFiles().size()];
        _compressionLevel = info.getLevel();
        compress(info.getFiles().toArray(files), info.getOutputPath(), info.getArchiveName());
    }

    @Override
    public void extract(ArchiveInfo info) throws IOException, ArchiveException, CompressorException {
        extract(info.getOutputPath(), info.getArchiveName());
    }

    @Override
    public final void interrupt() {
        _interrupt = true;
    }
}
