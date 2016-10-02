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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 * Offers algorithms to compress and decompress {@code tar.gz} archives
 *
 * @author Matthias Fussenegger
 */
public class Gzip extends AbstractAlgorithm implements CompressionAlgorithm {

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
    public void extract(String path, String name) throws IOException {
        try (TarArchiveInputStream tis
                = new TarArchiveInputStream(
                        new GZIPInputStream(
                                new BufferedInputStream(
                                        new FileInputStream(path + name))))) {

            ArchiveEntry entry = tis.getNextEntry();

            /*create main folder of gzip archive*/
            File folder = new File(Settings._outputPath + name.substring(0, 7));
            if (!folder.exists()) {
                folder.mkdir();
            }
            while (entry != null) {
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
    public void compress(File[] files, String base) throws IOException {
        try (TarArchiveOutputStream tos
                = new TarArchiveOutputStream(new GZIPOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(_path + _archiveName + ".tar.gz"))))) {

            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);

            byte[] buffer = new byte[4096];
            int readBytes;
            if (files.length >= 1) {
                for (int i = 0; i < files.length; ++i) {
                    /*create next file and define entry name based on folder level*/
                    File newFile = files[i];
                    String entryName = base + newFile.getName();
                    /*start compressing the file*/
                    if (newFile.isFile()) {
                        try (BufferedInputStream buf = new BufferedInputStream(
                                new FileInputStream(newFile))) {
                            /*create next archive entry and put it on output stream*/
                            ArchiveEntry entry = tos.createArchiveEntry(newFile, entryName);
                            tos.putArchiveEntry(entry);
                            /*write bytes to file*/
                            while ((readBytes = buf.read(buffer)) != -1) {
                                tos.write(buffer, 0, readBytes);
                            }
                            tos.closeArchiveEntry();
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
