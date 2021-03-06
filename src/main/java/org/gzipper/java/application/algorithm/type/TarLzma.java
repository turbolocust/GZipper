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
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents the TAR+LZMA archive type.
 *
 * @author Matthias Fussenegger
 */
public class TarLzma extends Tar {

    /**
     * Constructs a new instance of this class using the TAR constant of
     * {@link ArchiveStreamFactory} and the LZMA constant of
     * {@link CompressorStreamFactory}.
     */
    public TarLzma() {
        super(ArchiveStreamFactory.TAR, CompressorStreamFactory.LZMA);
    }

    @Override
    protected CompressorOutputStream makeCompressorOutputStream(OutputStream stream) throws IOException {
        return new LZMACompressorOutputStream(stream);
    }

    @Override
    protected CompressorInputStream makeCompressorInputStream(InputStream stream) throws IOException {
        return new LZMACompressorInputStream(stream);
    }
}
