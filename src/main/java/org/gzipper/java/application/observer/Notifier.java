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
package org.gzipper.java.application.observer;

/**
 * Simple observable which supports generic types.
 *
 * @author Matthias Fussenegger
 * @param <T> type of the value.
 */
public interface Notifier<T> {

    /**
     * Each notifier should provide a unique identifier so it can be easier
     * recognized by any listener.
     *
     * This is mainly used to avoid recalculations using the {@code hashCode()}
     * method, which provides a hash code for this object.
     *
     * @return the unique identifier of this instance.
     */
    int getId();

    /**
     * Returns the currently associated value.
     *
     * @return the currently associated value.
     */
    T getValue();

    /**
     * Replaces the currently associated value with the specified one.
     *
     * @param value the value to be set.
     */
    void setValue(T value);

    /**
     * Replaces the currently associated value with the specified one, sets the
     * current status to changed and then notifies all attached listeners.
     *
     * To be more detailed, this default implementation first calls
     * {@link #setValue(java.lang.Object)}, then {@link #setChanged()} and last
     * but not least {@link #notifyListeners()}.
     *
     * @param value the value to be changed.
     */
    default void changeValue(T value) {
        setValue(value);
        setChanged();
        notifyListeners();
    }

    /**
     * Notifies all attached listeners if {@link #hasChanged()} returns true. If
     * so, {@link #clearChanged()} will be called right after.
     */
    void notifyListeners();

    /**
     * Checks whether the value has changed or not.
     *
     * @return true if value has changed, false otherwise.
     */
    boolean hasChanged();

    /**
     * Sets the status to changed, {@link #hasChanged()} now returns true.
     */
    void setChanged();

    /**
     * Clears the status, {@link #hasChanged()} now returns false.
     */
    void clearChanged();

    /**
     * Attaches a new listener.
     *
     * @param listener the listener to be attached.
     */
    void attach(Listener<T> listener);

    /**
     * Detaches the specified listener.
     *
     * @param listener the listener to be detached.
     * @return true if listener was detached, false if listener had not been
     * attached before.
     */
    boolean detach(Listener<T> listener);

    /**
     * Removes all attached listeners.
     */
    void clearListeners();
}
