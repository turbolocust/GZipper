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

import java.io.File;
import java.util.Objects;
import java.util.function.Predicate;
import org.gzipper.java.application.predicates.Predicates;
import org.gzipper.java.application.util.FileUtils;

/**
 *
 * @author Matthias Fussenegger
 */
public class AlgorithmProgress {

    /**
     * Total size of the file(s).
     */
    private long _totalSize;

    /**
     * Total amount of bytes already read.
     */
    private long _totalBytesRead;

    AlgorithmProgress(File... files) {
        Objects.requireNonNull(files);
        setTotalSize(Predicates.createAlwaysTrue(), files);
    }

    AlgorithmProgress(Predicate<String> filter, File... files) {
        Objects.requireNonNull(filter);
        Objects.requireNonNull(files);
        setTotalSize(filter, files);
    }

    private void setTotalSize(Predicate<String> filter, File... files) {
        for (File file : files) {
            _totalSize += file.isDirectory()
                    ? FileUtils.fileSizes(file.toPath(), filter)
                    : file.length();
        }
    }

    /**
     * Returns the rounded progress.
     *
     * @return the current progress.
     */
    int getProgress() {
        return (int) Math.round(getProgressPrecise());
    }

    /**
     * Returns the precise progress.
     *
     * @return the current progress.
     */
    double getProgressPrecise() {
        return ((double) _totalBytesRead / _totalSize) * 100;
    }

    /**
     * Updates the current progress and returns it. The progress is only updated
     * if it is less or equal {@code 100}.
     *
     * @param readBytes the amount of bytes read so far.
     * @return the current progress.
     */
    void updateProgress(long readBytes) {
        _totalBytesRead += readBytes;
    }
}
