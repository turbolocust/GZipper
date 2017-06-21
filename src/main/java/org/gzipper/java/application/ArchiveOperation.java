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
package org.gzipper.java.application;

import java.io.IOException;
import java.util.concurrent.Callable;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.gzipper.java.application.algorithm.ArchivingAlgorithm;
import org.gzipper.java.application.concurrency.Interruptable;
import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.application.pojo.ArchiveInfo;
import org.gzipper.java.exceptions.GZipperException;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.util.Log;

/**
 * Object that represents an archiving operation.
 *
 * @author Matthias Fussenegger
 */
public class ArchiveOperation implements Callable<Boolean>, Interruptable {

    /**
     * The aggregated {@link ArchiveInfo}.
     */
    private final ArchiveInfo _archiveInfo;

    /**
     * The algorithm that is used for either creating or extracting an archive.
     * It will be determined during the initialization of an object.
     */
    private final ArchivingAlgorithm _algorithm;

    /**
     * True if operation is being used for compression, false otherwise.
     */
    private final boolean _compress;

    /**
     * True if a request for interruption has been received.
     */
    private volatile boolean _interrupt;

    /**
     * The elapsed time of the operation which will be stored after its end.
     */
    private long _elapsedTime;

    /**
     * Constructs a new instance of this class using the specified values.
     *
     * @param info the {@link ArchiveInfo} to be aggregated.
     * @param compress true for compression, false for decompression.
     * @throws org.gzipper.java.exceptions.GZipperException if determination of
     * archiving algorithm has failed.
     */
    public ArchiveOperation(ArchiveInfo info, boolean compress)
            throws GZipperException {
        _archiveInfo = info;
        _compress = compress;
        if ((_algorithm = init(info)) == null) {
            throw new GZipperException();
        }
    }

    /**
     * Initializes this object by determining the right archiving algorithm. See
     * {@link #_algorithm} for more information.
     *
     * @param info the {@link ArchiveInfo} to use for initialization.
     * @return the determined {@link ArchivingAlgorithm} or {@code null} if it
     * could not be determined.
     */
    private ArchivingAlgorithm init(ArchiveInfo info) {
        ArchiveType archiveType = info.getArchiveType();
        return archiveType.determineArchivingAlgorithm();
    }

    /**
     * Calculates the elapsed time in seconds.
     *
     * @return the elapsed time in seconds.
     */
    public double calculateElapsedTime() {
        return ((double) _elapsedTime / 1E9);
    }

    /**
     * Returns the aggregated instance of {@link ArchiveInfo}.
     *
     * @return the aggregated instance of {@link ArchiveInfo}.
     */
    public ArchiveInfo getArchiveInfo() {
        return _archiveInfo;
    }

    @Override
    public Boolean call() throws Exception {
        boolean success = false;

        if (_algorithm != null) {
            long startTime = System.nanoTime();
            try {
                if (_compress) {
                    _algorithm.compress(_archiveInfo);
                } else {
                    _algorithm.extract(_archiveInfo);
                }
                success = true;
            } catch (IOException ex) {
                if (!_interrupt) { // considered to be critical
                    Log.e(ex.getLocalizedMessage(), ex);
                    Log.w(I18N.getString("missingAccessRights.text"), true);
                }
            } catch (CompressorException | ArchiveException ex) {
                Log.e(ex.getLocalizedMessage(), ex);
            }
            _elapsedTime = System.nanoTime() - startTime;
        }
        return success;
    }

    /**
     * Checks whether this operation is for compression or decompression.
     *
     * @return true if operation is for compression, false for decompression.
     */
    public boolean isCompress() {
        return _compress;
    }

    @Override
    public void interrupt() {
        _interrupt = true;
        _algorithm.interrupt();
    }
}
