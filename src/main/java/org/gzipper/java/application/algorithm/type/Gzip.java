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

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.gzipper.java.application.algorithm.CompressorAlgorithm;
import org.gzipper.java.application.util.FileUtils;
import org.gzipper.java.util.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents the GZIP archive type.
 *
 * @author Matthias Fussenegger
 */
public class Gzip extends CompressorAlgorithm {

    /**
     * Returns {@link GzipParameters} with the operating system, modification
     * time (which is the current time in milliseconds) and the specified
     * filename without the directory path already set.
     *
     * @param filename the name of the file without directory path.
     * @return the default {@link GzipParameters}.
     */
    public static GzipParameters getDefaultGzipParams(String filename) {
        GzipParameters params = new GzipParameters();
        Settings settings = Settings.getInstance();
        int osValue = settings.getOs().getOsInfo().getValue();
        params.setOperatingSystem(osValue);
        params.setModificationTime(System.currentTimeMillis());

        if (filename != null && !FileUtils.normalize(filename).contains("/")) {
            params.setFileName(filename);
        }

        return params;
    }

    @Override
    protected CompressorInputStream makeCompressorInputStream(
            InputStream stream, CompressorOptions options) throws IOException {
        GzipCompressorInputStream gcis = new GzipCompressorInputStream(stream);
        options.setName(gcis.getMetaData().getFileName());
        return gcis;
    }

    @Override
    protected CompressorOutputStream makeCompressorOutputStream(
            OutputStream stream, CompressorOptions options) throws IOException {
        // set additional parameters for compressor stream
        GzipParameters params = getDefaultGzipParams(options.getName());
        params.setCompressionLevel(options.getLevel());
        return new GzipCompressorOutputStream(stream, params);
    }
}
