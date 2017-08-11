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
import java.util.Objects;
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

    /**
     * The current progress.
     */
    private double _progress;

    AlgorithmProgress(File... files) {
        Objects.requireNonNull(files);
        setTotalSize(files);
    }

    /**
     * Sets the total file size. This method is only called once when this
     * object is being constructed.
     *
     * @param files the files which are used to calculate {@link #_totalSize}.
     */
    private void setTotalSize(File... files) {
        for (File file : files) {
            _totalSize += file.isDirectory()
                    ? FileUtils.fileSizes(file.toPath())
                    : file.length();
        }
    }

    /**
     * Returns the current progress without modifying it.
     *
     * @return the current progress.
     */
    double getProgress() {
        return _progress;
    }

    /**
     * Updates the current progress and returns it. The progress is only updated
     * if it is less or equal {@code 100d}.
     *
     * @param readBytes the amount of bytes read so far.
     * @return the current progress.
     */
    double updateProgress(long readBytes) {
        _totalBytesRead += readBytes;
        double percentage = ((double) _totalBytesRead / (double) _totalSize) * 100;
        return _progress <= 100d ? _progress = percentage : _progress;
    }
}
