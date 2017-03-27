/*
 * Copyright (C) 2017 Matthias Fussenegger
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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;

/**
 * Singleton used to execute tasks using an {@link Executor}.
 *
 * @author Matthias Fussenegger
 */
public class TaskHandler {

    /**
     * The executor which executes tasks.
     */
    private final Executor _executor;

    private TaskHandler() {
        _executor = Executors.newCachedThreadPool();
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return the singleton instance of this class.
     */
    public static TaskHandler getInstance() {
        return TaskHandlerHolder.INSTANCE;
    }

    /**
     * Executes the specified task.
     *
     * @param task the task to be executed.
     */
    public void execute(Task<?> task) {
        _executor.execute(task);
    }

    /**
     * Holder class for singleton instance.
     */
    private static class TaskHandlerHolder {

        /**
         * The actual singleton instance of the outer class.
         */
        private static final TaskHandler INSTANCE = new TaskHandler();
    }
}
