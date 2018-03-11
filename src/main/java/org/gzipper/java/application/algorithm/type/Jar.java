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

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 * Represents the JAR archive type.
 *
 * @author Matthias Fussenegger
 */
public class Jar extends Zip {

    /**
     * Constructs a new instance of this class using the JAR constant of
     * {@link ArchiveStreamFactory} and the DEFLATE constant of
     * {@link CompressorStreamFactory}.
     */
    public Jar() {
        super(ArchiveStreamFactory.JAR, CompressorStreamFactory.DEFLATE);
    }

    @Override
    protected ArchiveOutputStream makeArchiveOutputStream(OutputStream stream)
            throws IOException, ArchiveException {
        JarArchiveOutputStream jaos = new JarArchiveOutputStream(stream);
        jaos.setLevel(_compressionLevel);
        jaos.setUseZip64(Zip64Mode.AsNeeded);
        jaos.setFallbackToUTF8(true);
        return jaos;
    }
}
