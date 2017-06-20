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
package org.gzipper.java.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.gzipper.java.presentation.GZipper;
import org.gzipper.java.presentation.control.MainViewController;

/**
 * Convenience class for application wide logging.
 *
 * @author Matthias Fussenegger
 */
public class Log {

    /**
     * Map that holds the loggers that are used by this application.
     */
    private static final Map<String, Logger> LOGGERS = new HashMap<>(2);

    /**
     * Default logger named {@code GZipper.class.getName()}.
     */
    public static final String DEFAULT_LOGGER_NAME;

    /**
     * UI logger named {@code MainViewController.class.getName()}.
     */
    public static final String UI_LOGGER_NAME;

    static {
        DEFAULT_LOGGER_NAME = GZipper.class.getName();
        UI_LOGGER_NAME = MainViewController.class.getName();
        LOGGERS.put(DEFAULT_LOGGER_NAME, Logger.getLogger(DEFAULT_LOGGER_NAME));
        LOGGERS.put(UI_LOGGER_NAME, Logger.getLogger(UI_LOGGER_NAME));
    }

    /**
     * If set to true, verbose logging is enabled. Verbose logging means that
     * the output in the UI is more detailed. This means that if logged, even
     * full exception messages will be displayed.
     */
    private static boolean _verboseUiLogging;

    /**
     * Enables verbose logging for the UI output.
     *
     * @param verboseUiLogging true to enable, false to disable.
     */
    public static void setVerboseUiLogging(boolean verboseUiLogging) {
        _verboseUiLogging = verboseUiLogging;
    }

    /**
     * Logs a new error message including an exception.
     *
     * @param msg the message to log.
     * @param thrown the exception to include.
     */
    public static void e(String msg, Throwable thrown) {
        msg += "\n" + stackTraceAsString(thrown);
        LogRecord record = new LogRecord(Level.SEVERE, msg);
        record.setThrown(thrown);
        log(record, _verboseUiLogging);
    }

    /**
     * Logs a new error message with an optional parameter.
     *
     * @param msg the message to log.
     * @param param the optional parameter.
     */
    public static void e(String msg, Object param) {
        LogRecord record = new LogRecord(Level.SEVERE, msg);
        record.setParameters(new Object[]{param});
        log(record, _verboseUiLogging);
    }

    /**
     * Logs a new error message with optional parameters.
     *
     * @param msg the message to log.
     * @param params the optional parameters.
     */
    public static void e(String msg, Object... params) {
        LogRecord record = new LogRecord(Level.SEVERE, msg);
        record.setParameters(params);
        log(record, _verboseUiLogging);
    }

    /**
     * Logs a new info message including an exception.
     *
     * @param msg the message to log.
     * @param thrown the exception to include.
     * @param uiLogging true to log to the UI as well.
     */
    public static void i(String msg, Throwable thrown, boolean uiLogging) {
        msg += "\n" + stackTraceAsString(thrown);
        LogRecord record = new LogRecord(Level.INFO, msg);
        record.setThrown(thrown);
        log(record, uiLogging);
    }

    /**
     * Logs a new info message with an optional parameter.
     *
     * @param msg the message to log.
     * @param param the optional parameter.
     * @param uiLogging true to log to the UI as well.
     */
    public static void i(String msg, Object param, boolean uiLogging) {
        LogRecord record = new LogRecord(Level.INFO, msg);
        record.setParameters(new Object[]{param});
        log(record, uiLogging);
    }

    /**
     * Logs a new info message with optional parameters.
     *
     * @param msg the message to log.
     * @param uiLogging true to log to the UI as well.
     * @param params the optional parameters.
     */
    public static void i(String msg, boolean uiLogging, Object... params) {
        LogRecord record = new LogRecord(Level.INFO, msg);
        record.setParameters(params);
        log(record, uiLogging);
    }

    /**
     * Logs a new warning message including an exception.
     *
     * @param msg the message to log.
     * @param thrown the exception to include.
     * @param uiLogging true to log to the UI as well.
     */
    public static void w(String msg, Throwable thrown, boolean uiLogging) {
        msg += "\n" + stackTraceAsString(thrown);
        LogRecord record = new LogRecord(Level.WARNING, msg);
        record.setThrown(thrown);
        log(record, uiLogging);
    }

    /**
     * Logs a new warning message with an optional parameter.
     *
     * @param msg the message to log.
     * @param param the optional parameter.
     * @param uiLogging true to log to the UI as well.
     */
    public static void w(String msg, Object param, boolean uiLogging) {
        LogRecord record = new LogRecord(Level.WARNING, msg);
        record.setParameters(new Object[]{param});
        log(record, uiLogging);
    }

    /**
     * Logs a new warning message with optional parameters.
     *
     * @param msg the message to log.
     * @param uiLogging true to log to the UI as well.
     * @param params the optional parameters.
     */
    public static void w(String msg, boolean uiLogging, Object... params) {
        LogRecord record = new LogRecord(Level.WARNING, msg);
        record.setParameters(params);
        log(record, uiLogging);
    }

    /**
     * Converts the stack trace of the specified {@link Throwable} to a string.
     *
     * @param thrown holds the stack trace.
     * @return string representation of the stack trace.
     */
    public static String stackTraceAsString(Throwable thrown) {
        StringWriter errors = new StringWriter();
        thrown.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    /**
     * Logs the specified {@link LogRecord} using both, the default logger and
     * the logger for UI output if {@code uiLogging} equals true.
     *
     * @param record the {@link LogRecord} to be logged.
     * @param uiLogging true to also log using the logger for UI.
     */
    private static void log(LogRecord record, boolean uiLogging) {
        LOGGERS.get(DEFAULT_LOGGER_NAME).log(record);
        if (uiLogging) {
            LOGGERS.get(UI_LOGGER_NAME).log(record);
        }
    }
}
