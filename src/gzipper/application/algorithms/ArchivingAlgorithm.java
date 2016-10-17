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
package gzipper.application.algorithms;

import java.io.File;
import java.io.IOException;

/**
 * A marker interface to mark the classes that contain compression algorithms
 *
 * @author Matthias Fussenegger
 */
public interface ArchivingAlgorithm {

    /**
     * Extracts archive using defined algorithm of class to the specified path.
     *
     * @param path The absolute path of the archive
     * @param name The filename of the archive
     * @throws java.io.IOException On any IO error during extraction.
     */
    void extract(String path, String name) throws IOException;

    /**
     * Compresses files using defined algorithm of class with default settings
     * and creates an archive to the specified path.
     *
     * @param files The files selected from the file chooser
     * @param base The root path of the specified folder
     * @throws java.io.IOException On any IO error during compression.
     */
    void compress(File[] files, String base) throws IOException;

}
