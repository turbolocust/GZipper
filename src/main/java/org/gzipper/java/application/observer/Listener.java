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
 * Simple observer which supports generic types.
 *
 * @param <T> type of the value.
 * @author Matthias Fussenegger
 */
public interface Listener<T> {

    /**
     * Called by a {@link Notifier} if its associated value has been updated.
     *
     * @param notifier the source of this call.
     * @param value    the updated value.
     */
    void update(Notifier<T> notifier, T value);
}
