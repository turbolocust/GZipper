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

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.gzipper.java.application.algorithm.AbstractAlgorithm;

/**
 * Offers algorithms to compress and decompress TAR+GZIP archives.
 *
 * @author Matthias Fussenegger
 */
public class TarGz extends AbstractAlgorithm implements ArchiveType {

    private TarGz() {
        super(ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP);
    }

    public static TarGz getInstance() {
        return TarGzHolder.INSTANCE;
    }

    private static class TarGzHolder {

        private static final TarGz INSTANCE = new TarGz();
    }
}
