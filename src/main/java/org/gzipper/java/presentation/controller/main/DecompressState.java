package org.gzipper.java.presentation.controller.main;

import org.gzipper.java.application.ArchiveInfoFactory;
import org.gzipper.java.application.ArchiveOperation;
import org.gzipper.java.application.CompressionMode;
import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.application.util.FileUtils;
import org.gzipper.java.application.util.ListUtils;
import org.gzipper.java.exceptions.GZipperException;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A state which is set when the user wishes to decompress (or extract) files.
 */
final class DecompressState extends ArchivingState {

    /**
     * Creates a new instance of {@link DecompressState}.
     *
     * @param controller the controller to be aggregated.
     */
    DecompressState(MainViewController controller) {
        super(controller);
    }

    private boolean isOutputFileSetAndFilesToBeDecompressedPresent() {
        return controller.getOutputFile() != null && !ListUtils.isNullOrEmpty(controller.getSelectedFiles());
    }

    @Override
    public boolean checkUpdateOutputPath() {
        return FileUtils.isValidDirectory(controller.getTextOfOutputPathTextField());
    }

    @Override
    public void performOperations(ArchiveOperation... operations) {
        if (isOutputFileSetAndFilesToBeDecompressedPresent()) {
            super.performOperations(operations);
        } else {
            Log.e("Operation(s) cannot be started, because an invalid path has been specified");
            Log.w(I18N.getString("outputPathWarning.text"), true);
            controller.requestFocusOnOutputPathTextField();
        }
    }

    @Override
    public List<ArchiveOperation> initOperation(ArchiveType archiveType) throws GZipperException {

        var selectedFiles = controller.getSelectedFiles();
        List<ArchiveOperation> operations = new ArrayList<>(selectedFiles.size());

        for (File file : selectedFiles) {
            var info = ArchiveInfoFactory.createArchiveInfo(archiveType, FileUtils.getPath(file),
                    FileUtils.getPath(controller.getOutputFile()) + File.separator);
            var builder = new ArchiveOperation.Builder(info, CompressionMode.DECOMPRESS);
            builder.addListener(this).filterPredicate(_filterPredicate);
            operations.add(builder.build());
        }

        return operations;
    }
}
