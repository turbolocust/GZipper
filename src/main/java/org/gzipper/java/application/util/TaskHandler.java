/*
 * Copyright (C) 2018 Matthias Fussenegger
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gzipper.java.application.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Handler used to execute tasks via {@link ExecutorService}.
 *
 * @author Matthias Fussenegger
 */
public final class TaskHandler {

    /**
     * The executor service which executes tasks.
     */
    private static final ExecutorService EXECUTOR_SERVICE;

    static {
        EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    }

    private TaskHandler() {
        throw new AssertionError("Holds static members only.");
    }

    /**
     * Executes the specified task.
     *
     * @param task the task to be executed.
     * @return a {@link Future} which can be used to manipulate the task.
     */
    public static synchronized Future<?> submit(Runnable task) {
        return EXECUTOR_SERVICE.submit(task);
    }

    /**
     * Executes the specified task.
     *
     * @param <T> the type of the task's result.
     * @param task the task to be executed.
     * @return a {@link Future} which can be used to manipulate the task.
     */
    public static synchronized <T> Future<T> submit(Callable<T> task) {
        return EXECUTOR_SERVICE.submit(task);
    }
}
