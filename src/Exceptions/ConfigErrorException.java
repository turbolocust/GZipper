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
 * Class to handle errors that can occur while trying to parse the configuration
 * file. The {@code ConfigFileParser} class makes use of this
 *
 * @author Matthias Fussenegger
 */
public class ConfigErrorException extends Exception {

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "Configuration error, check \"gzipper.ini\" file,\n"
                + "line must end with \"false\" or \"true\" "
                + "or with a valid String depending on prefix!";
    }
}
