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
package Operations;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Matthias Fussenegger
 */
public abstract class AbstractAlgorithm implements Runnable {

    /**
     * The name of the archive to be processed
     */
    protected String _archiveName;

    /**
     * The path of the archive to be processed
     */
    protected final String _path;

    /**
     * To calculate the elapsed time for archiving operation
     */
    protected long _elapsedTime;

    /**
     * True for compressing, false for extracting an archive
     */
    protected final boolean _createArchive;

    /**
     * The selected files to be put in an archive by file chooser
     */
    protected final File[] _selectedFiles;

    /**
     * The run flag to keep thread alive
     */
    protected boolean _runFlag;

    /**
     * To avoid overwriting recent archives, if any exist. The index will be
     * appended at the end of the filename
     */
    protected static int _nameIndex;

    /**
     * Creates a new object of the child class for archiving operations
     *
     * @param path The path of the output directory
     * @param name The name of the target archive
     * @param files The selected files from GUI
     * @param zipMode True if zip, false if unzip
     */
    protected AbstractAlgorithm(String path, String name, File[] files, boolean zipMode) {
        _path = path;
        _archiveName = name;
        _createArchive = zipMode;
        _selectedFiles = files;
    }

    /**
     * Retrieves files from a specific directory; mandatory for compression
     *
     * @param path The path that contains files to be compressed
     * @return And array of files from the specified path
     * @throws IOException If an error occurred
     */
    protected File[] getFiles(String path) throws IOException {
        File dir = new File(path);
        File[] files = dir.listFiles();
        return files;
    }

    /**
     * Returns the elapsed time of the chosen archiving operation
     *
     * @return The elapsed time of the chosen operation
     */
    public long getElapsedTime() {
        return _elapsedTime;
    }

    /**
     * Creates a new thread and starts it
     */
    public abstract void start();

    /**
     * Stops the thread and tries to close any open file streams
     */
    public abstract void stop();

    /**
     * Tries to interrupt the current thread
     */
    public abstract void interrupt();

    /**
     * Called by other classes to wait for this thread to die
     *
     * @return True if thread is dead
     * @throws InterruptedException If an error occurred
     */
    public abstract boolean waitForExecutionEnd() throws InterruptedException;

    /**
     * Extracts archive using defined algorithm of class to the specified path
     *
     * @param path The absolute path of the archive
     * @param name The filename of the archive
     * @throws IOException If an error during extraction error occurred
     */
    protected abstract void extract(String path, String name) throws IOException;

    /**
     * Compresses files using defined algorithm of class with default settings
     * and creates an archive to the specified path
     *
     * @param files The files selected from the file chooser
     * @param base The root path of the specified folder
     * @throws IOException If an error during compressing occurred
     */
    protected abstract void compress(File[] files, String base) throws IOException;
}
