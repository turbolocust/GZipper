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
     * The progress as float that is equal to a mathematical integer.
     */
    private float _progressRint;

    AlgorithmProgress(File... files) {
        setTotalSize(files);
    }

    /**
     * Sets the total file size. This method is only called once when this
     * object is being constructed.
     *
     * @param files the files which are used to calculate {@link #_totalSize}.
     */
    private void setTotalSize(File... files) {
        if (files != null && files.length > 0) {
            for (File file : files) {
                _totalSize += file.length();
            }
        }
    }

    /**
     * Returns the current progress without modifying it.
     *
     * @return the current progress.
     */
    float getProgressRint() {
        return _progressRint;
    }

    /**
     * Updates the current progress and returns it. The progress is only updated
     * if a threshold has been exceeded. Otherwise the return parameter of this
     * method is the same as of {@link #getProgressRint()}.
     *
     * @param readBytes the amount of bytes read so far.
     * @return the current progress.
     */
    float updateProgress(long readBytes) {
        _totalBytesRead += readBytes;
        float percentage = ((float) _totalBytesRead / (float) _totalSize) * 100;
        if (Math.rint(percentage) > _progressRint && _progressRint < 100L) {
            ++_progressRint;
        }
        return _progressRint;
    }
}
