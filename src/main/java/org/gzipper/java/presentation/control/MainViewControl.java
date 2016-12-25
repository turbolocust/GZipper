/*
 * Copyright (c) Matthias Fussenegger
 */
package org.gzipper.java.presentation.control;

import java.io.File;
import java.io.IOException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.gzipper.java.application.algorithm.ArchivingAlgorithm;
import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.application.util.Settings;
import org.gzipper.java.presentation.model.ArchivingOperation;

/**
 *
 * @author Matthias Fussenegger
 */
public final class MainViewControl {

    private final Settings _settings;

    MainViewControl(Settings settings) {
        _settings = settings;
    }

    void performStartButtonAction(ArchivingOperation op) throws IOException, ArchiveException, CompressorException {

        ArchiveType archiveType = op.getArchiveType();
        ArchivingAlgorithm algorithm = archiveType.determineArchivingAlgorithm();

        File[] files = new File[op.getFiles().size()];

        if (op.isCompress()) {
            algorithm.compress(op.getFiles().toArray(files), op.getOutputPath(), op.getArchiveName());
        } else {
            algorithm.extract(op.getOutputPath(), op.getArchiveName());
        }
    }

}
