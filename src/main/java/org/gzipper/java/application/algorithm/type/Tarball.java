/*
 * Copyright (C) 2017 Matthias Fussenegger
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gzipper.java.application.algorithm.type;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.gzipper.java.application.algorithm.AbstractAlgorithm;
import org.gzipper.java.util.Settings;

/**
 *
 * Represents the TAR+GZ archive type.
 *
 * @author Matthias Fussenegger
 */
public class Tarball extends AbstractAlgorithm {

    /**
     * Constructs a new instance of this class using the TAR constant of
     * {@link ArchiveStreamFactory} and the GZIP constant of
     * {@link CompressorStreamFactory}.
     */
    public Tarball() {
        super(ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP);
    }

    /**
     * Constructs a new instance of this class using the specified values.
     *
     * @param archiveType the archive type, which has to be a constant of
     * {@link ArchiveStreamFactory}.
     * @param compressionType the compression type, which has to be a constant
     * of {@link CompressorStreamFactory}.
     */
    public Tarball(String archiveType, String compressionType) {
        super(archiveType, compressionType);
    }

    @Override
    public ArchiveOutputStream makeArchiveOutputStream(
            OutputStream stream) throws IOException, ArchiveException {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(stream);
        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        return taos;
    }

    @Override
    public CompressorOutputStream makeCompressorOutputStream(OutputStream stream)
            throws IOException, CompressorException {
        // configure additional parameters for compressor stream
        GzipParameters params = new GzipParameters();
        final Settings settings = Settings.getInstance();
        int value = settings.getOperatingSystem().getCurrentSystem().getValue();
        params.setOperatingSystem(value);
        params.setCompressionLevel(_compressionLevel);
        // apply additional parameters
        GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(stream, params);
        return gcos;
    }
}
