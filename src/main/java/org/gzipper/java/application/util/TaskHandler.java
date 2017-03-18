/*
 * Copyright (C) 2016 Matthias Fussenegger
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
 *
 * @author Matthias Fussenegger
 */
public class TaskHandler {

    private final Executor _executor;

    private TaskHandler() {
        _executor = Executors.newCachedThreadPool();
    }

    public static TaskHandler getInstance() {
        return TaskHandlerHolder.INSTANCE;
    }

    public void execute(Task<?> task) {
        _executor.execute(task);
    }

    private static class TaskHandlerHolder {

        private static final TaskHandler INSTANCE = new TaskHandler();
    }

}
