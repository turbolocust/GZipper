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

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.gzipper.java.application.ArchiveInfo;
import org.gzipper.java.application.observer.NotifierImpl;
import org.gzipper.java.application.predicates.Predicates;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Abstract class that offers generally used attributes and methods for
 * archiving algorithms. Any class that represents an archiving algorithm should
 * derive from this class.
 *
 * @author Matthias Fussenegger
 */
public abstract class AbstractAlgorithm extends NotifierImpl<Integer> implements CompressionAlgorithm {

    /**
     * If set to true the currently running operation will be interrupted.
     */
    protected volatile boolean interrupt = false;

    /**
     * The compression level. Will only be considered if supported by algorithm.
     */
    protected int compressionLevel;

    /**
     * Object used to update the progress of the algorithm.
     */
    protected AlgorithmProgress algorithmProgress;

    /**
     * Predicate used to filter files or entries when processing archives.
     */
    protected Predicate<String> filterPredicate;

    /**
     * The default constructor of this class.
     */
    public AbstractAlgorithm() {
        // accepts all files/entries since the test result is never false
        filterPredicate = Predicates.createAlwaysTrue();
    }

    /**
     * Retrieves files from a specific directory; mandatory for compression.
     *
     * @param path the path that contains the files to be compressed.
     * @return an array of files from the specified path.
     */
    protected final File[] getFiles(String path) {
        final File dir = new File(path);
        return dir.listFiles();
    }

    /**
     * Initializes {@link #algorithmProgress} with the specified files.
     *
     * @param files the files to be used for initialization.
     */
    protected final void initAlgorithmProgress(File... files) {
        algorithmProgress = new AlgorithmProgress(filterPredicate, files);
    }

    /**
     * Updates the progress of the current operation and notifies all attached
     * listeners if the new progress using {@code Math.rint(double)} is greater
     * than the previous one. This behavior may be changed by overriding this
     * method.
     *
     * @param readBytes the amount of bytes that have been read so far.
     */
    protected final void updateProgress(long readBytes) {
        algorithmProgress.updateProgress(readBytes);
        changeValue(algorithmProgress.getProgress());
    }

    @Override
    public final void compress(ArchiveInfo info) throws IOException, ArchiveException, CompressorException {
        final File[] files = new File[Objects.requireNonNull(info.getFiles()).size()];
        compressionLevel = info.getLevel();
        compress(info.getFiles().toArray(files), info.getOutputPath(), info.getArchiveName());
    }

    @Override
    public final void extract(ArchiveInfo info) throws IOException, ArchiveException, CompressorException {
        extract(info.getOutputPath(), info.getArchiveName());
    }

    @Override
    public final void setPredicate(Predicate<String> predicate) {
        if (predicate != null) {
            filterPredicate = predicate;
        }
    }

    @Override
    public final void interrupt() {
        interrupt = true;
    }
}
