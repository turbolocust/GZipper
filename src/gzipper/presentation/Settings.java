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
package gzipper.presentation;

import java.awt.image.BufferedImage;
import javafx.scene.image.Image;

/**
 * Abstract class with static members only to store various settings during the
 * usage of this application.
 *
 * @author Matthias Fussenegger
 */
public final class Settings {

    /**
     * Class holds static members only.
     */
    private Settings() {
    }

    /**
     * To determine whether operating system is Unix-based or not
     */
    public static boolean _isUnix;

    /**
     * True if user prefers to make classic zip-file instead of tar-archive
     */
    public static boolean _isClassicZipMode;

    /**
     * The output path of the compressed archive or the decompressed files
     */
    public static String _outputPath;

    /**
     * The decoded path of JAR-file
     */
    static String _initialPath;

    /**
     * To store the default icon of each frame
     */
    static BufferedImage _frameIcon;

    /**
     * To store the default image of each frame
     */
    static Image _frameImage;

}
