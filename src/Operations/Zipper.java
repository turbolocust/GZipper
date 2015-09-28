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

/**
 *
 * @author Matthias Fussenegger
 */
public class Zipper implements Runnable {

    //ATTRIBUTES
    private TarArchiveOutputStream _tos;
    private String _archiveName;
    private final String _path;
    private long _elapsedTime;
    private final boolean _zipMode;
    private final File[] _selectedFiles;

    private boolean _runFlag;
    private Thread _zipperThread;
    private static int _nameIterator; //to avoid overwriting recent archives

    //CONSTRUCTOR
    public Zipper(String path, String name, File[] files, boolean zipMode) {
        _path = path;
        _archiveName = name;
        _zipMode = zipMode;
        _selectedFiles = files;
    }

    //METHODS
    public void start() throws IOException {
        _runFlag = true;
        _zipperThread = new Thread(this);
        _zipperThread.start();
    }

    public void stop() {
        _runFlag = false;
        if (_tos != null) {
            try {
                _tos.flush();
                _tos.close();
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.WARNING, "Archive entry still open", ex);
                File file = new File(_path + _archiveName + ".tar.gz");
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    public void interrupt() {
        _runFlag = false;
        _zipperThread.interrupt();
    }

    /*compress files using TAR/GZIP-algorithm and create archive;
     note that GZIPOutputStream already has a built-in buffer*/
    private void makeGzip(File[] files, String base) throws IOException {
        long startTime = System.nanoTime();
        byte[] buffer = new byte[4096];
        int readBytes;
        if (files.length >= 1) {
            for (int i = 0; i < files.length & _runFlag != false; ++i) {
                File newFile = files[i];
                String entryName = base + newFile.getName();
                if (newFile.isFile()) {
                    try (BufferedInputStream buf = new BufferedInputStream(new FileInputStream(newFile))) {
                        ArchiveEntry entry = _tos.createArchiveEntry(newFile, entryName);
                        _tos.putArchiveEntry(entry);
                        while ((readBytes = buf.read(buffer)) != -1) {
                            _tos.write(buffer, 0, readBytes);
                        }
                        _tos.closeArchiveEntry();
                    }
                } else { //child is a directory
                    File[] children = getFiles(newFile.getAbsolutePath());
                    makeGzip(children, entryName + "/"); //the slash indicates a folder, see JavaDoc
                }
            }
        }
        _elapsedTime = System.nanoTime() - startTime;
    }

    /*extract a TAR/GZIP-archive; note that GZIPInputStream already has a built-in buffer*/
    private void extractGzip(String path, String name) throws IOException {
        long startTime = System.nanoTime();
        try (TarArchiveInputStream tis = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(path + name)))) {
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
                    if (GUI._isUnix) {//check OS for correct file path
                        newFile = new File(folder.getAbsolutePath() + "/" + entryName);
                    } else {
                        newFile = new File(folder.getAbsolutePath() + "\\" + entryName);
                    }
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
                /*create new OutputStream and write bytes to new file*/
                try (BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(newFilePath))) {
                    byte[] buffer = new byte[4096];
                    int readBytes;
                    while ((readBytes = tis.read(buffer)) != -1) {
                        buf.write(buffer, 0, readBytes);
                    }
                }
                entry = tis.getNextEntry();
            }
        }
        _elapsedTime = System.nanoTime() - startTime;
    }

    /*retrieve files from a specific directory; mandatory for compression*/
    private File[] getFiles(String path) throws IOException {
        File dir = new File(path);
        File[] files = dir.listFiles();
        return files;
    }

    /*called by other classes to wait for thread to die*/
    public boolean waitForExecEnd() throws InterruptedException {
        if (_zipperThread != null) {
            _zipperThread.join();
            return true;
        }
        return false;
    }

    public long getElapsedTime() {
        return _elapsedTime;
    }

    @Override
    public void run() {
        /*in current implementation multiple jobs are not yet supported*/
        if (_zipMode != false) {
            try {
                /*check whether archive with given name already exists;
                 if so, add iterator to file name an re-check*/
                File file = new File(_path + _archiveName + ".tar.gz");
                while (file.exists()) {
                    ++_nameIterator;
                    _archiveName = _archiveName.substring(0, 7) + _nameIterator;
                    file = new File(_path + _archiveName + ".tar.gz");
                }
                _tos = new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(_path + _archiveName + ".tar.gz")));
                _tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Error creating output stream", ex);
                System.exit(1);
            }
        }
        while (_runFlag) {
            try {
                if (_zipMode != false & _selectedFiles != null) {
                    makeGzip(_selectedFiles, "");
                } else { //unzip
                    extractGzip(_path, _archiveName);
                }
                stop();
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Error compressing archive", ex);
                System.exit(1);
            }
        }
    }
}
