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
package org.gzipper.java.application.algorithm.type;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.gzipper.java.application.algorithm.AbstractAlgorithm;

/**
 * Offers algorithms to compress and decompress ZIP archives.
 *
 * @author Matthias Fussenegger
 */
public class Zip extends AbstractAlgorithm {

    public Zip() {
        super(ArchiveStreamFactory.ZIP, CompressorStreamFactory.DEFLATE);
    }

    @Override
    public ArchiveOutputStream makeArchiveOutputStream(OutputStream stream)
            throws IOException, ArchiveException {
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(stream);
        zaos.setLevel(_compressionLevel);
        zaos.setUseZip64(Zip64Mode.AsNeeded);
        return zaos;
    }

    @Override
    public CompressorOutputStream makeCompressorOutputStream(OutputStream stream)
            throws IOException, CompressorException {
        return null; // as no compressor stream is required
    }

}
