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
package org.gzipper.java.presentation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages progress values and calculates the total progress on an update. Also
 * holds the current progress, which can be a sentinel value. Besides the
 * calculation of the total progress, the purpose of this class is to provide a
 * solution for the flooding of the UI thread, which can be caused by too many
 * frequent updates of the progress value itself. With this solution, the UI may
 * only be updated if the current progress is set to the sentinel value. The UI
 * thread should then update the progress visually and reset the progress value
 * to the sentinel value. Basically, this behavior is similar to a lock.
 *
 * <p>
 * To achieve the latter of the above explanation this class provides the
 * {@link #getAndSetProgress(double)} method to get a new value while setting
 * another one with the same call.
 * </p>
 *
 * <blockquote><pre>
 * <b>Example:</b>
 *    if (progress.getAndSetProgress(newValue) == Progress.SENTINEL) {
 *      Platform.runLater((){@literal ->} {
 *          double value = progress.getAndSetProgress(Progress.SENTINEL);
 *          // perform UI update
 *      });
 *    }
 * </pre></blockquote>
 *
 * @author Matthias Fussenegger
 */
public class ProgressManager {

    /**
     * Pre-defined sentinel value.
     */
    public static final double SENTINEL = -1d;

    /**
     * Holds the current progress.
     */
    private final AtomicLong _progress;

    /**
     * Maps progress values to identifiers (keys).
     */
    private final ConcurrentMap<Integer, Double> _progressMap = new ConcurrentHashMap<>();

    /**
     * Constructs a new instance of this class and initializes the progress with
     * {@link #SENTINEL}.
     */
    public ProgressManager() {
        _progress = new AtomicLong(Double.doubleToLongBits(SENTINEL));
    }

    /**
     * Constructs a new instance of this class and initializes the progress with
     * the specified value.
     *
     * @param value the value to initialize the progress with.
     */
    public ProgressManager(double value) {
        _progress = new AtomicLong(Double.doubleToLongBits(value));
    }

    /**
     * Gets the current value of the progress and sets a new value.
     *
     * @param value the value to be set.
     * @return the previous value.
     */
    public final double getAndSetProgress(double value) {
        final long newValue = Double.doubleToLongBits(value);
        final long oldValue = _progress.getAndSet(newValue);
        return Double.longBitsToDouble(oldValue);
    }

    /**
     * Atomically updates the progress of the specified identifier using the
     * specified value and also calculates and then returns the total progress
     * considering all previously added identifiers. If an identifier does not
     * yet exist it will be created and mapped to the specified id.
     *
     * @param id the identifier to be mapped to the progress value.
     * @param value the updated progress value.
     * @return the total progress of all stored progress values.
     */
    public double updateProgress(Integer id, double value) {
        return calculateProgress(id, value);
    }

    /**
     * Removes (clears) all progress mappings.
     */
    public void reset() {
        _progressMap.clear();
    }

    private double calculateProgress(Integer id, double value) {
        _progressMap.merge(id, value, (oldValue, newValue) -> newValue);
        double totalProgress = _progressMap.values().stream()
                .mapToDouble(Double::doubleValue).sum();
        return (totalProgress /= _progressMap.size()) / 100d;
    }
}
