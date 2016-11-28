/*
 * Copyright (C) 2016 Matthias
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Matthias Fussenegger
 */
public class ThreadHandler {

    private final ExecutorService _executorService;

    private ThreadHandler() {
        _executorService = Executors.newSingleThreadExecutor();
    }

    public static ThreadHandler getInstance() {
        return ThreadHandlerHolder.INSTANCE;
    }

    public void execute(Runnable command) {
        _executorService.execute(command);
    }

    private static class ThreadHandlerHolder {

        private static final ThreadHandler INSTANCE = new ThreadHandler();
    }
}
