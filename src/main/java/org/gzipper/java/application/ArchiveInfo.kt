/*
 * Copyright (C) 2020 Matthias Fussenegger
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
package org.gzipper.java.application

import org.gzipper.java.application.model.ArchiveType
import java.io.File

/**
 * Object that holds information required for archiving operations.
 *
 * @author Matthias Fussenegger
 */
class ArchiveInfo internal constructor(
    /**
     * The type of the archive.
     */
    val archiveType: ArchiveType,
    /**
     * The full name of the archive.
     */
    var archiveName: String,
    /**
     * The compression level of the archive.
     */
    var level: Int,
    /**
     * The files to be compressed. May be <code>null</code> if decompression.
     */
    var files: List<File>?,
    /**
     * The output path either of the archive or the decompressed file(s).
     */
    var outputPath: String
) {

    override fun toString(): String {
        return """
            ArchiveInfo{
            Archive type : ${archiveType},
            Compression level : ${level},
            Files : ${files},
            Archive name : ${archiveName},
            Output path : ${outputPath}}
            """.trimIndent()
    }
}