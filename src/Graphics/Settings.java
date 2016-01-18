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
package Graphics;

import java.awt.image.BufferedImage;

/**
 *
 * @author Matthias Fussenegger
 */
public abstract class Settings {

    /**
     * To determine whether operating system is Unix-based or not
     */
    public static boolean _isUnix;

    /**
     * To store the default icon of each frame
     */
    protected static BufferedImage _frameIcon;

    /**
     * True if logging via options menu has been enabled
     */
    protected static boolean _loggingEnabled;

    /**
     * True if user prefers to make classic zip-file instead of tar-archive
     */
    protected static boolean _useClassicZipMode;
}
