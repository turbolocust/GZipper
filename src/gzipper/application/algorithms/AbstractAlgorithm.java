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

import gzipper.presentation.Settings;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;

/**
 * Abstract class that offers generally used attributes and methods for
 * archiving algorithms. Any class that contains an archiving algorithm will
 * extend this class.
 *
 * @author Matthias Fussenegger
 */
public abstract class AbstractAlgorithm implements ArchivingAlgorithm {

    /**
     * The name of the archive.
     */
    protected final String Name;

    /**
     * The type of the archive.
     */
    protected final String ArchiveType;

    /**
     * The path of the archive.
     */
    protected final String Path;

    /**
     * The selected files to be put into an archive by file chooser.
     */
    private final File[] _selectedFiles;

    /**
     * Creates a new object of the child class for archiving operations.
     *
     * @param path The path of the output directory
     * @param name The name of the target archive
     * @param type The type of the archive
     * @param files The selected files from GUI
     */
    protected AbstractAlgorithm(String path, String name, String type, File[] files) {
        Path = path;
        Name = name;
        ArchiveType = type;
        _selectedFiles = files;
    }

    /**
     * Retrieves files from a specific directory; mandatory for compression.
     *
     * @param path The path that contains the files to be compressed
     * @return And array of files from the specified path
     * @throws IOException If an error occurred
     */
    protected File[] getFiles(String path) throws IOException {
        final File dir = new File(path);
        File[] files = dir.listFiles();
        return files;
    }

    /**
     * Returns a new archive input stream of the corresponding child class.
     *
     * @return A new {@link ArchiveInputStream}.
     * @throws IOException If e.g. the path or name of the archive is invalid.
     */
    protected abstract ArchiveInputStream getInputStream() throws IOException;

    /**
     * Returns a new archive output stream of the corresponding child class.
     *
     * @return A new {@link ArchiveOutputStream}.
     * @throws IOException If e.g. the path or name of the archive is invalid.
     */
    protected abstract ArchiveOutputStream getOutputStream() throws IOException;

    @Override
    public void extract(String path, String name) throws IOException {
        try (ArchiveInputStream inputStream = getInputStream()) {

            ArchiveEntry entry = inputStream.getNextEntry();

            /*create main folder of archive*/
            File folder = new File(Settings._outputPath + name.substring(0, 7));

            if (!folder.exists()) {
                folder.mkdir();
            }

            while (entry != null) {
                String entryName = entry.getName();
                /*check if entry contains a directory*/
                if (entryName.contains("/")) {

                    File newFile = new File(folder.getAbsolutePath() + "/" + entryName);

                    if (!newFile.getParentFile().exists()) {
                        newFile.getParentFile().mkdirs(); // also creates parent directories
                    }
                }

                final String newFilePath = folder.getAbsolutePath() + "/" + entryName;

                /*create new OutputStream and write bytes to file*/
                try (BufferedOutputStream buf = new BufferedOutputStream(
                        new FileOutputStream(newFilePath))) {
                    byte[] buffer = new byte[4096];
                    int readBytes;
                    while ((readBytes = inputStream.read(buffer)) != -1) {
                        buf.write(buffer, 0, readBytes);
                    }
                }

                entry = inputStream.getNextEntry();
            }
        }
    }

    @Override
    public void compress(File[] files, String base) throws IOException {
        try (ArchiveOutputStream outputStream = getOutputStream()) {

            byte[] buffer = new byte[4096];
            int readBytes;

            if (files.length > 0) {
                for (int i = 0; i < files.length; ++i) {
                    /*create next file and define entry name based on folder level*/
                    File newFile = files[i];
                    String entryName = base + newFile.getName();
                    /*start compressing the file*/
                    if (newFile.isFile()) {
                        try (BufferedInputStream buf = new BufferedInputStream(
                                new FileInputStream(newFile))) {
                            /*create next archive entry and put it on output stream*/
                            ArchiveEntry entry = outputStream.createArchiveEntry(newFile, entryName);
                            outputStream.putArchiveEntry(entry);
                            /*write bytes to file*/
                            while ((readBytes = buf.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, readBytes);
                            }
                            outputStream.closeArchiveEntry();
                        }
                    } else { //child is a directory
                        File[] children = getFiles(newFile.getAbsolutePath());
                        compress(children, entryName + "/"); //the slash indicates a folder
                    }
                }
            }
        }
    }

}
