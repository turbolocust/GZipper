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
package org.gzipper.java.application.observer;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @param <T> type of the value.
 * @author Matthias Fussenegger
 */
public class NotifierImpl<T> implements Notifier<T> {

    /**
     * Unique identifier of this instance.
     */
    private final int _id = hashCode();

    /**
     * Listeners to be notified if the associated value has been updated.
     */
    private final List<Listener<T>> _listeners = new CopyOnWriteArrayList<>();

    /**
     * True if value has changed, false otherwise.
     */
    private boolean _hasChanged = false;

    /**
     * The associated value which may change.
     */
    private T _value;

    @Override
    public int getId() {
        return _id;
    }

    @Override
    public final synchronized T getValue() {
        return _value;
    }

    @Override
    public final synchronized void setValue(T value) {
        _value = value;
    }

    @Override
    public final void notifyListeners() {
        if (hasChanged()) {
            _listeners.forEach((listener) -> listener.update(this, _value));
            clearChanged();
        }
    }

    @Override
    public final synchronized boolean hasChanged() {
        return _hasChanged;
    }

    @Override
    public final synchronized void setChanged() {
        _hasChanged = true;
    }

    @Override
    public final synchronized void clearChanged() {
        _hasChanged = false;
    }

    @Override
    public final void attach(Listener<T> listener) {
        Objects.requireNonNull(listener);
        if (!_listeners.contains(listener)) {
            _listeners.add(listener);
        }
    }

    @Override
    public final boolean detach(Listener<T> listener) {
        return _listeners.remove(listener);
    }

    @Override
    public final void clearListeners() {
        _listeners.clear();
    }
}
