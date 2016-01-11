/*
 * Copyright (C) 2015 Matthias Fussenegger
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

import Graphics.GUI;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

/**
 *
 * @author Matthias Fussenegger
 */
public class Zipper implements Runnable {

    /**
     * The output stream for creating a TarArchive
     */
    private TarArchiveOutputStream _tos;

    /**
     * The output stream for creating a classic ZipArchive
     */
    private ZipArchiveOutputStream _zos;

    /**
     * The name of the archive to be processed
     */
    private String _archiveName;

    /**
     * The path of the archive to be processed
     */
    private final String _path;

    /**
     * To calculate the elapsed time for archiving operation
     */
    private long _elapsedTime;

    /**
     * True for creating an archive, false for extracting an archive
     */
    private final boolean _createArchive;

    /**
     * True for creating a ZIP-archive, false for creating a TAR.GZ-Archive
     */
    private final boolean _makeZip;

    /**
     * The selected files to be put in an archive by FileChooser of GUI
     */
    private final File[] _selectedFiles;

    /**
     * The run flag to keep zipperThread alive
     */
    private boolean _runFlag;

    /**
     * The thread of this class
     */
    private Thread _zipperThread;

    /**
     * To avoid overwriting recent archives, if any exist
     */
    private static int _nameIndex;

    /**
     * Creates a new Zipper object for zip/unzip operations
     *
     * @param path The path of the output directory
     * @param name The name of the target archive
     * @param files The selected files from GUI
     * @param zipMode True if zip, false if unzip
     * @param zipType True for zip-archive, false for tar-archive
     */
    public Zipper(String path, String name, File[] files, boolean zipMode, boolean zipType) {
        _path = path;
        _archiveName = name;
        _createArchive = zipMode;
        _makeZip = zipType;
        _selectedFiles = files;
    }

    /**
     * Creates a new thread and starts it
     *
     * @throws IOException If an error occurred
     */
    public void start() throws IOException {
        _runFlag = true;
        _zipperThread = new Thread(this);
        _zipperThread.start();
    }

    /**
     * Stops the thread and tries to close any open file streams
     */
    public void stop() {
        _runFlag = false;
        try {
            if (_tos != null) {
                _tos.flush();
                _tos.close();
            } else if (_zos != null) {
                _zos.flush();
                _zos.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.WARNING, "Archive entry still open", ex);
            File file; //used to delete prevously created archive on error
            if (!_makeZip) {
                file = new File(_path + _archiveName + ".tar.gz");
            } else {
                file = new File(_path + _archiveName + ".zip");
            }
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * Tries to interrupt the current thread
     */
    public void interrupt() {
        _runFlag = false;
        _zipperThread.interrupt();
    }

    /**
     * Compresses files using TAR/GZIP-algorithm and creates an archive to the
     * specified path, which is the initialPath of GUI.class
     *
     * @param files The files selected from jFileChooser
     * @param base The root path of the specified folder
     * @throws IOException If an archiving error occurred
     */
    private void makeTarball(File[] files, String base) throws IOException {
        byte[] buffer = new byte[4096];
        int readBytes;
        if (files.length >= 1) {
            for (int i = 0; i < files.length & _runFlag != false; ++i) {
                /*create next file and define entry name based on folder level*/
                File newFile = files[i];
                String entryName = base + newFile.getName();
                /*start compressing the file*/
                if (newFile.isFile()) {
                    try (BufferedInputStream buf = new BufferedInputStream(
                            new FileInputStream(newFile))) {
                        /*create next archive entry and put it on output strom*/
                        ArchiveEntry entry = _tos.createArchiveEntry(newFile, entryName);
                        _tos.putArchiveEntry(entry);
                        /*write bytes to file*/
                        while ((readBytes = buf.read(buffer)) != -1) {
                            _tos.write(buffer, 0, readBytes);
                        }
                        _tos.closeArchiveEntry();
                    }
                } else { //child is a directory
                    File[] children = getFiles(newFile.getAbsolutePath());
                    makeTarball(children, entryName + "/"); //the slash indicates a folder
                }
            }
        }
    }

    /**
     * Extracts a TAR/GZIP-archive to the specified path, which is the
     * initialPath of GUI.class
     *
     * @param path The absolute path of the archive
     * @param name The filename of the archive
     * @throws IOException If an archiving error occurred
     */
    private void extractTarball(String path, String name) throws IOException {
        try (TarArchiveInputStream tis = new TarArchiveInputStream(
                new GZIPInputStream(new BufferedInputStream(new FileInputStream(path + name))))) {

            ArchiveEntry entry = tis.getNextEntry();

            /*create main folder of gzip archive*/
            File folder = new File(path + name.substring(0, 7));
            if (!folder.exists()) {
                folder.mkdir();
            }
            while (entry != null & _runFlag != false) {
                String entryName = entry.getName();
                /*check if entry contains a directory*/
                if (entryName.contains("/")) {
                    File newFile;
                    if (GUI._isUnix) { //check OS for correct file path
                        newFile = new File(folder.getAbsolutePath() + "/" + entryName);
                    } else {
                        newFile = new File(folder.getAbsolutePath() + "\\" + entryName);
                    }
                    /*mkdirs also creates parent directories*/
                    if (!newFile.getParentFile().exists()) {
                        newFile.getParentFile().mkdirs();
                    }
                }

                String newFilePath;

                if (GUI._isUnix) { //check OS for correct file path
                    newFilePath = folder.getAbsolutePath() + "/" + entryName;
                } else {
                    newFilePath = folder.getAbsolutePath() + "\\" + entryName;
                }
                /*create new OutputStream and write bytes to file*/
                try (BufferedOutputStream buf = new BufferedOutputStream(
                        new FileOutputStream(newFilePath))) {
                    byte[] buffer = new byte[4096];
                    int readBytes;
                    while ((readBytes = tis.read(buffer)) != -1) {
                        buf.write(buffer, 0, readBytes);
                    }
                }
                entry = tis.getNextEntry();
            }
        }
    }

    /**
     * Compresses files using ZIP-algorithm with default settings and creates an
     * archive to the specified path, which is the initialPath of GUI.class
     *
     * @param files The files selected from jFileChooser
     * @param base The root path of the specified folder
     * @throws IOException If an archiving error occurred
     */
    private void makeZip(File[] files, String base) throws IOException {
        byte[] buffer = new byte[4096];
        int readBytes;
        if (files.length >= 1) {
            for (int i = 0; i < files.length & _runFlag != false; ++i) {
                /*create next file and define entry name based on folder level*/
                File newFile = files[i];
                String entryName = base + newFile.getName();
                /*start compressing the file*/
                if (newFile.isFile()) {
                    try (BufferedInputStream buf = new BufferedInputStream(
                            new FileInputStream(newFile))) {
                        /*create next archive entry and put it on output strom*/
                        ArchiveEntry entry = _zos.createArchiveEntry(newFile, entryName);
                        _zos.putArchiveEntry(entry);
                        /*write bytes to file*/
                        while ((readBytes = buf.read(buffer)) != -1) {
                            _zos.write(buffer, 0, readBytes);
                        }
                        _zos.closeArchiveEntry();
                    }
                } else { //child is a directory
                    File[] children = getFiles(newFile.getAbsolutePath());
                    makeZip(children, entryName + "/"); //the slash indicates a folder
                }
            }
        }
    }

    /**
     * Extracts a ZIP-archive to the specified path, which is the initialPath of
     * GUI.class
     *
     * @param path The absolute path of the archive
     * @param name The filename of the archive
     * @throws IOException If an archiving error occurred
     */
    private void extractZip(String path, String name) throws IOException {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new BufferedInputStream(new FileInputStream(path + name)))) {

            ArchiveEntry entry = zis.getNextEntry();

            /*create main folder of gzip archive*/
            File folder = new File(path + name.substring(0, 7));
            if (!folder.exists()) {
                folder.mkdir();
            }
            while (entry != null & _runFlag != false) {
                String entryName = entry.getName();
                /*check if entry contains a directory*/
                if (entryName.contains("/")) {
                    File newFile;
                    if (GUI._isUnix) { //check OS for correct file path
                        newFile = new File(folder.getAbsolutePath() + "/" + entryName);
                    } else {
                        newFile = new File(folder.getAbsolutePath() + "\\" + entryName);
                    }
                    /*mkdirs also creates parent directories*/
                    if (!newFile.getParentFile().exists()) {
                        newFile.getParentFile().mkdirs();
                    }
                }

                String newFilePath;

                if (GUI._isUnix) { //check OS for correct file path
                    newFilePath = folder.getAbsolutePath() + "/" + entryName;
                } else {
                    newFilePath = folder.getAbsolutePath() + "\\" + entryName;
                }
                /*create new OutputStream and write bytes to file*/
                try (BufferedOutputStream buf = new BufferedOutputStream(
                        new FileOutputStream(newFilePath))) {
                    byte[] buffer = new byte[4096];
                    int readBytes;
                    while ((readBytes = zis.read(buffer)) != -1) {
                        buf.write(buffer, 0, readBytes);
                    }
                }
                entry = zis.getNextEntry();
            }
        }
    }

    /**
     * Retrieves files from a specific directory; mandatory for compression
     *
     * @param path The path that contains files to be compressed
     * @return And array of files from the specified path
     * @throws IOException If an error occurred
     */
    private File[] getFiles(String path) throws IOException {
        File dir = new File(path);
        File[] files = dir.listFiles();
        return files;
    }

    /**
     * Called by other classes to wait for thread to die
     *
     * @return True if thread is dead
     * @throws InterruptedException
     */
    public boolean waitForExecEnd() throws InterruptedException {
        if (_zipperThread != null) {
            _zipperThread.join();
            return true;
        }
        return false;
    }

    /**
     * Returns the elapsed time of the chosen archiving operation
     *
     * @return The elapsed time of the chosen operation
     */
    public long getElapsedTime() {
        return _elapsedTime;
    }

    @Override
    public void run() {
        /*in current implementation multiple jobs are not supported*/
        if (_createArchive != false) {
            try {
                /*check whether archive with given name already exists;
                 if so, add index to file name an re-check*/
                File file; //will be the output file of archive
                if (!_makeZip) {
                    file = new File(_path + _archiveName + ".tar.gz");
                } else {
                    file = new File(_path + _archiveName + ".zip");
                }
                while (file.exists()) {
                    ++_nameIndex;
                    _archiveName = _archiveName.substring(0, 7) + _nameIndex;
                    if (!_makeZip) {
                        file = new File(_path + _archiveName + ".tar.gz");
                    } else {
                        file = new File(_path + _archiveName + ".zip");
                    }
                }
                if (!_makeZip) {
                    _tos = new TarArchiveOutputStream(new GZIPOutputStream(
                            new BufferedOutputStream(new FileOutputStream(
                                    _path + _archiveName + ".tar.gz"))));
                    _tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                } else { //create output stream for classic zip-archive
                    _zos = new ZipArchiveOutputStream(new BufferedOutputStream(
                            new FileOutputStream(
                                    _path + _archiveName + ".zip")));
                }
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Error creating output stream", ex);
                System.exit(1);
            }
        }
        while (_runFlag) {
            try {

                long startTime = System.nanoTime();

                if (_createArchive != false & _selectedFiles != null) {
                    if (!_makeZip) {
                        makeTarball(_selectedFiles, "");
                    } else {
                        makeZip(_selectedFiles, "");
                    }
                } else if (!_makeZip) { //unzip
                    extractTarball(_path, _archiveName);
                } else {
                    extractZip(_path, _archiveName);
                }

                _elapsedTime = System.nanoTime() - startTime;
                stop(); //stop thread after operation was successful

            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Error compressing archive", ex);
            }
        }
    }
}
