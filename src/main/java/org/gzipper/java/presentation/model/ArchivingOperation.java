/*
 * Copyright (C) 2016 Matthias Fussenegger
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
package org.gzipper.java.presentation.model;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.gzipper.java.application.algorithm.ArchivingAlgorithm;
import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.application.pojo.ArchiveInfo;
import org.gzipper.java.presentation.GZipper;

/**
 *
 * @author Matthias Fussenegger
 */
public class ArchivingOperation {

    private final ArchiveInfo _archiveInfo;

    private final boolean _compress;

    private long _elapsedTime;

    public ArchivingOperation(ArchiveInfo info, boolean compress) {
        _archiveInfo = info;
        _compress = compress;
    }

    public boolean performOperation() {

        ArchiveType archiveType = _archiveInfo.getArchiveType();
        ArchivingAlgorithm algorithm = archiveType.determineArchivingAlgorithm();

        long startTime = System.nanoTime();

        boolean success = false;
        try {
            if (_compress) {
                algorithm.compress(_archiveInfo);
            } else {
                algorithm.extract(_archiveInfo);
            }
            success = true;
        } catch (IOException | CompressorException | ArchiveException ex) {
            Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
        }

        _elapsedTime = System.nanoTime() - startTime;
        return success;
    }

    /**
     * Calculates the elapsed time in seconds.
     *
     * @return The elapsed time in seconds.
     */
    public double calculateElapsedTime() {
        return ((double) _elapsedTime / 1E9);
    }

    /**
     * Checks whether this operation is for compression or decompression.
     *
     * @return True for compression, false for decompression.
     */
    public boolean isCompress() {
        return _compress;
    }

}
