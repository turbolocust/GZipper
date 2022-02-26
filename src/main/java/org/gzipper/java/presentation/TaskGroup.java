/*
 * Copyright (C) 2022 Matthias Fussenegger
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.gzipper.java.application.util.MapUtils;
import org.gzipper.java.util.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

/**
 * Represents a group of tasks (or operations).
 *
 * @author Matthias Fussenegger
 */
public final class TaskGroup {

    private final ConcurrentMap<Integer, Future<?>> _tasks;

    private final BooleanProperty _isAnyTasksPresent;

    /**
     * Creates a new task group with zero tasks present.
     */
    public TaskGroup() {
        _tasks = new ConcurrentHashMap<>();
        _isAnyTasksPresent = new SimpleBooleanProperty();
    }

    /**
     * {@code true} if any tasks are present (or known), {@code false} otherwise.
     */
    public BooleanProperty anyTasksPresentProperty() {
        return _isAnyTasksPresent;
    }

    /**
     * Cancels all currently present (or known) tasks.
     */
    public void cancelTasks() {
        if (!MapUtils.isNullOrEmpty(_tasks)) {
            _tasks.keySet().stream().map(_tasks::get)
                    .filter((task) -> (!task.cancel(true))).forEachOrdered((task) -> {
                        // log error message only when cancellation failed
                        Log.e("Task cancellation failed for {0}", task.hashCode());
                    });
        }
    }

    /**
     * Adds the specified task to this group if it is not already present (or known).
     *
     * @param id   the id of the task to be added.
     * @param task the task to be added.
     */
    public void put(int id, Future<?> task) {
        boolean isAdded = _tasks.putIfAbsent(id, task) == null;

        if (isAdded) {
            _isAnyTasksPresent.setValue(true);
        }
    }

    /**
     * Removes the task with the given id from this group.
     *
     * @param id the id of the task to remove be removed.
     */
    public void remove(int id) {
        boolean isRemoved = _tasks.remove(id) != null;

        if (isRemoved && _tasks.isEmpty()) {
            _isAnyTasksPresent.setValue(false);
        }
    }

    /**
     * Returns {@code true} if this group is empty, {@code false} otherwise.
     *
     * @return {@code true} if this group is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return _tasks.isEmpty();
    }
}
