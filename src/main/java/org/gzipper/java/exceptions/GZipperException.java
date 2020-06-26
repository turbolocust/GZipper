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
package org.gzipper.java.exceptions;

/**
 * Class to handle application specific errors.
 *
 * @author Matthias Fussenegger
 */
public class GZipperException extends Exception {

    private static final long serialVersionUID = 5822293002523982761L;

    private Reason _reason = Reason.UNKNOWN;

    /**
     * Delegates exception to its super class {@link Exception}.
     */
    public GZipperException() {
        super();
    }

    /**
     * Delegates error message to its super class {@link Exception}.
     *
     * @param errorMessage the specified error message.
     */
    public GZipperException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Delegates exception cause to its super class {@link Exception}.
     *
     * @param cause the cause of this exception.
     */
    public GZipperException(Throwable cause) {
        super(cause);
    }

    /**
     * Delegates error message and cause to its super class {@link Exception}.
     *
     * @param errorMessage the specified error message.
     * @param cause the cause of this exception.
     */
    public GZipperException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

    /**
     * Creates a new {@link GZipperException} including a reason.
     *
     * @param reason the reason of this exception.
     * @return a new instance of {@link GZipperException}.
     */
    public static GZipperException createWithReason(Reason reason) {
        GZipperException ex = new GZipperException();
        ex.setReason(reason);
        return ex;
    }

    /**
     * Creates a new {@link GZipperException} including a reason.
     *
     * @param reason the reason of this exception.
     * @param msg the specified error message.
     * @return a new instance of {@link GZipperException}.
     */
    public static GZipperException createWithReason(Reason reason, String msg) {
        GZipperException ex = new GZipperException(msg);
        ex.setReason(reason);
        return ex;
    }

    /**
     * Creates a new {@link GZipperException} including a reason.
     *
     * @param reason the reason of this exception.
     * @param cause the cause of this exception.
     * @return a new instance of {@link GZipperException}.
     */
    public static GZipperException createWithReason(Reason reason, Throwable cause) {
        GZipperException ex = new GZipperException(cause);
        ex.setReason(reason);
        return ex;
    }

    /**
     * Creates a new {@link GZipperException} including a reason.
     *
     * @param reason the reason of this exception.
     * @param msg the specified error message.
     * @param cause the cause of this exception.
     * @return a new instance of {@link GZipperException}.
     */
    public static GZipperException createWithReason(Reason reason, String msg, Throwable cause) {
        GZipperException ex = new GZipperException(msg, cause);
        ex.setReason(reason);
        return ex;
    }

    /**
     * Returns the reason of this exception.
     *
     * @return the reason of this exception.
     */
    public Reason getReason() {
        return _reason;
    }

    /**
     * Sets the reason of this exception if it is not {@code null}. If the
     * provided parameter is {@code null}, the reason will be set to its default
     * value, which is {@code UNKNOWN}.
     *
     * @param reason the reason of this exception.
     */
    public void setReason(Reason reason) {
        _reason = reason == null ? Reason.UNKNOWN : reason;
    }

    /**
     * The reason of the exception.
     */
    public enum Reason {
        NO_DIR_SUPPORTED, FAULTY_COMPRESSION_LVL, ILLEGAL_MODE, UNKNOWN
    }
}
