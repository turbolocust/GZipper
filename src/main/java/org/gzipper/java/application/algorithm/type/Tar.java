/*
 * Copyright (C) 2018 Matthias Fussenegger
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

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.gzipper.java.application.algorithm.ArchivingAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents the TAR archive type.
 *
 * @author Matthias Fussenegger
 */
public class Tar extends ArchivingAlgorithm {

    /**
     * Constructs a new instance of this class using the TAR constant of
     * {@link ArchiveStreamFactory} and {@code null}.
     */
    public Tar() {
        super(ArchiveStreamFactory.TAR, null);
    }

    /**
     * Constructs a new instance of this class using the specified values.
     *
     * @param archiveType     the archive type, which has to be a constant of
     *                        {@link ArchiveStreamFactory}.
     * @param compressionType the compression type, which has to be a constant
     *                        of {@link CompressorStreamFactory}.
     */
    public Tar(String archiveType, String compressionType) {
        super(archiveType, compressionType);
    }

    @Override
    protected ArchiveOutputStream makeArchiveOutputStream(OutputStream stream) {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(stream);
        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        return taos;
    }

    @Override
    protected CompressorOutputStream makeCompressorOutputStream(OutputStream stream) throws IOException {
        return null;
    }

    @Override
    protected CompressorInputStream makeCompressorInputStream(InputStream stream) throws IOException {
        return null;
    }
}
