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
 *
 * @author Matthias Fussenegger
 */
public class SevenZip extends AbstractAlgorithm implements ArchiveType {

    private SevenZip() {
        super(ArchiveStreamFactory.SEVEN_Z, CompressorStreamFactory.LZMA);
    }

    public static SevenZip getInstance() {
        return SevenZipHolder.INSTANCE;
    }

    private static class SevenZipHolder {

        private static final SevenZip INSTANCE = new SevenZip();
    }
}
