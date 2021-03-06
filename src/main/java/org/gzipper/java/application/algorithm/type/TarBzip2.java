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

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents the TAR+BZIP2 archive type.
 *
 * @author Matthias Fussenegger
 */
public class TarBzip2 extends Tar {

    /**
     * Constructs a new instance of this class using the TAR constant of
     * {@link ArchiveStreamFactory} and the BZIP2 constant of
     * {@link CompressorStreamFactory}.
     */
    public TarBzip2() {
        super(ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2);
    }

    @Override
    protected CompressorInputStream makeCompressorInputStream(InputStream stream) throws IOException {
        return new BZip2CompressorInputStream(stream);
    }

    @Override
    protected CompressorOutputStream makeCompressorOutputStream(OutputStream stream) throws IOException {
        return new BZip2CompressorOutputStream(stream);
    }
}
