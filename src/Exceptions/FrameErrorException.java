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
package Exceptions;

/**
 * Class to handle errors that can occur while trying to draw a {@code SubFrame}
 *
 * @author Matthias Fussenegger
 */
public class FrameErrorException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Delegates error message to its super class, which is {@code Exception}
     *
     * @param errorMessage The specified error message
     */
    public FrameErrorException(String errorMessage) {
        super(errorMessage);
    }
}
