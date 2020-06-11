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
package org.gzipper.java.application;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.gzipper.java.exceptions.GZipperException;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.util.Log;
import org.gzipper.java.application.algorithm.CompressionAlgorithm;
import org.gzipper.java.application.observer.Listener;
import org.gzipper.java.application.concurrency.Interruptible;

/**
 * Object that represents an archiving operation.
 *
 * @author Matthias Fussenegger
 */
public final class ArchiveOperation implements Callable<Boolean>, Interruptible {

    /**
     * The aggregated {@link ArchiveInfo}.
     */
    private final ArchiveInfo _archiveInfo;

    /**
     * The algorithm that is used for either creating or extracting an archive.
     * It will be determined during the initialization of an object.
     */
    private final CompressionAlgorithm _algorithm;

    /**
     * Describes what kind of operation shall be performed.
     */
    private final CompressionMode _compressionMode;

    /**
     * The elapsed time of the operation which will be stored after its end.
     */
    private final AtomicLong _elapsedTime = new AtomicLong();

    /**
     * The start time of this operation.
     */
    private long _startTime = 0L;

    /**
     * True if a request for interruption has been received.
     */
    private boolean _interrupt = false;

    /**
     * True if operation is completed, false otherwise.
     */
    private boolean _completed = false;

    private ArchiveOperation(final ArchiveOperation.Builder builder) {
        _archiveInfo = builder._archiveInfo;
        _algorithm = builder._algorithm;
        _compressionMode = builder._compressionMode;
        _algorithm.setPredicate(builder._filterPredicate);
    }

    private void setElapsedTime() {
        if (_elapsedTime.get() == 0) {
            _elapsedTime.set(System.nanoTime() - _startTime);
        }
    }

    /**
     * Calculates the elapsed time in seconds.
     *
     * @return the elapsed time in seconds.
     */
    public double calculateElapsedTime() {
        return _startTime > 0L ? (_elapsedTime.doubleValue() / 1E9) : 0d;
    }

    /**
     * Returns the aggregated instance of {@link ArchiveInfo}.
     *
     * @return the aggregated instance of {@link ArchiveInfo}.
     */
    public ArchiveInfo getArchiveInfo() {
        return _archiveInfo;
    }

    /**
     * Returns true if this operation is completed, false otherwise.
     *
     * @return true if this operation is completed, false otherwise.
     */
    public boolean isCompleted() {
        return _completed;
    }

    @Override
    public Boolean call() throws Exception {
        if (_completed) {
            throw new IllegalStateException("Operation already completed.");
        }
        boolean success = false;
        _startTime = System.nanoTime();
        try {
            switch (_compressionMode) {
                case COMPRESS:
                    _algorithm.compress(_archiveInfo);
                    break;
                case DECOMPRESS:
                    _algorithm.extract(_archiveInfo);
                    break;
                default:
                    throw GZipperException.createWithReason(
                            GZipperException.Reason.ILLEGAL_MODE,
                            "Mode could not be determined.");
            }
            success = true;
        }
        catch (IOException ex) {
            if (!_interrupt) {
                final Throwable cause = ex.getCause();
                if (cause instanceof GZipperException) {
                    GZipperException inner = (GZipperException) cause;
                    if (inner.getReason() == GZipperException.Reason.NO_DIR_SUPPORTED) {
                        Log.w(I18N.getString("noDirSupported.text"), true);
                    }
                }
                Log.e(ex.getLocalizedMessage(), ex);
                Log.w(I18N.getString("corruptArchive.text"), true);
            }
        }
        catch (CompressorException | ArchiveException ex) {
            Log.e(ex.getLocalizedMessage(), ex);
            Log.w(I18N.getString("wrongFormat.text"), true);
        }
        finally {
            setElapsedTime();
            _completed = true;
            _algorithm.clearListeners();
        }
        return success;
    }

    @Override
    public void interrupt() {
        if (!_completed) {
            _interrupt = true;
            _algorithm.clearListeners();
            _algorithm.interrupt();
            setElapsedTime(); // eliminates race condition
        }
    }

    @Override
    public String toString() {
        return Integer.toString(hashCode());
    }

    /**
     * Builder class for {@link ArchiveOperation}.
     */
    public static class Builder {

        // required parameters
        private final ArchiveInfo _archiveInfo;

        private final CompressionAlgorithm _algorithm;

        private final CompressionMode _compressionMode;

        // optional parameters
        private Predicate<String> _filterPredicate = null;

        /**
         * Constructs a new instance of this class using the specified values.
         *
         * @param info the {@link ArchiveInfo} to be aggregated.
         * @param compressionMode the {@link CompressionMode} for this
         * operation.
         * @throws org.gzipper.java.exceptions.GZipperException if determination
         * of archiving algorithm has failed.
         */
        public Builder(ArchiveInfo info, CompressionMode compressionMode)
                throws GZipperException {
            // check parameters first
            Objects.requireNonNull(info);
            Objects.requireNonNull(compressionMode);
            // set fields and initialize algorithm
            _archiveInfo = info;
            _compressionMode = compressionMode;
            if ((_algorithm = init(info)) == null) {
                throw new GZipperException(new NullPointerException(
                        "Algorithm could not be determined."));
            }
        }

        private CompressionAlgorithm init(ArchiveInfo info) {
            return info.getArchiveType().getAlgorithm();
        }

        /**
         * Sets the predicate to be used by the operation.
         *
         * @param predicate the {@link Predicate} to be used.
         * @return a reference to this to allow method chaining.
         */
        public final Builder filterPredicate(Predicate<String> predicate) {
            _filterPredicate = predicate;
            return this;
        }

        /**
         * Attaches a new listener to the algorithm instance of the operation.
         *
         * @param listener listener to be attached to this algorithm instance.
         * @return a reference to this to allow method chaining.
         */
        public final Builder addListener(Listener<Integer> listener) {
            _algorithm.attach(listener);
            return this;
        }

        /**
         * Builds a new instance of {@link ArchiveOperation}.
         *
         * @return a new instance of {@link ArchiveOperation}.
         */
        public final ArchiveOperation build() {
            return new ArchiveOperation(this);
        }
    }
}
