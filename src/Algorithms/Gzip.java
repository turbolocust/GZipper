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
package Algorithms;

import Graphics.GUI;
import Graphics.Settings;
import Interfaces.CompressionAlgorithm;
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

/**
 *
 * @author Matthias Fussenegger
 */
public class Gzip extends AbstractAlgorithm implements CompressionAlgorithm {

    /**
     * The output stream for creating a tar-archive
     */
    private TarArchiveOutputStream _tos;

    /**
     * The thread of this class
     */
    private Thread _gzipThread;

    /**
     * Creates a new object for zip/unzip operations on tar-archives
     *
     * @param path The path of the output directory
     * @param name The name of the target archive
     * @param files The selected files from GUI
     * @param zipMode True if zip, false if unzip
     */
    public Gzip(String path, String name, File[] files, boolean zipMode) {
        super(path, name, files, zipMode);
    }

    @Override
    public void start() {
        _runFlag = true;
        _gzipThread = new Thread(this);
        _gzipThread.start();
    }

    @Override
    public void stop() {
        _runFlag = false;
        try {
            if (_tos != null) {
                _tos.flush();
                _tos.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.WARNING, "Output stream could not be closed", ex);
            File file; //to delete previously created archive on error
            file = new File(_path + _archiveName + ".tar.gz");
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Override
    public void interrupt() {
        _runFlag = false;
        _gzipThread.interrupt();
    }

    @Override
    public boolean waitForExecutionEnd() throws InterruptedException {
        if (_gzipThread != null) {
            _gzipThread.join();
            return true;
        }
        return false;
    }

    @Override
    protected void extract(String path, String name) throws IOException {
        try (TarArchiveInputStream tis = new TarArchiveInputStream(
                new GZIPInputStream(new BufferedInputStream(new FileInputStream(path + name))))) {

            ArchiveEntry entry = tis.getNextEntry();

            /*create main folder of gzip archive*/
            File folder = new File(Settings._outputPath + name.substring(0, 7));
            if (!folder.exists()) {
                folder.mkdir();
            }
            while (entry != null & _runFlag) {
                String entryName = entry.getName();
                /*check if entry contains a directory*/
                if (entryName.contains("/")) {
                    File newFile;
                    if (Settings._isUnix) { //check OS for correct file path
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

                if (Settings._isUnix) { //check OS for correct file path
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

    @Override
    protected void compress(File[] files, String base) throws IOException {
        byte[] buffer = new byte[4096];
        int readBytes;
        if (files.length >= 1) {
            for (int i = 0; i < files.length & _runFlag; ++i) {
                /*create next file and define entry name based on folder level*/
                File newFile = files[i];
                String entryName = base + newFile.getName();
                /*start compressing the file*/
                if (newFile.isFile()) {
                    try (BufferedInputStream buf = new BufferedInputStream(
                            new FileInputStream(newFile))) {
                        /*create next archive entry and put it on output stream*/
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
                    compress(children, entryName + "/"); //the slash indicates a folder
                }
            }
        }
    }

    @Override
    public void run() {
        /*check whether archive with given name already exists; 
        if so, add index to file name an re-check*/
        if (_createArchive) {
            try {
                File file; //will be the output file of archive
                file = new File(_path + _archiveName + ".tar.gz");
                while (file.exists()) {
                    ++_nameIndex;
                    _archiveName = _archiveName.substring(0, 7) + _nameIndex;
                    file = new File(_path + _archiveName + ".tar.gz");
                }
                _tos = new TarArchiveOutputStream(new GZIPOutputStream(
                        new BufferedOutputStream(new FileOutputStream(
                                _path + _archiveName + ".tar.gz"))));
                _tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Error creating output stream", ex);
                System.exit(1);
            }
        }
        while (_runFlag) {
            try {

                long startTime = System.nanoTime();

                if (_createArchive != false & _selectedFiles != null) {
                    compress(_selectedFiles, "");
                } else {
                    extract(_path, _archiveName);
                }

                _elapsedTime = System.nanoTime() - startTime;
                stop(); //stop thread after successful operation

            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Error compressing archive", ex);
            }
        }
    }
}
