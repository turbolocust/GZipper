package org.gzipper.java.presentation.controller.main;

import org.gzipper.java.application.ArchiveInfo;
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
 * A state which is set when the user wishes to compress files.
 */
final class CompressState extends ArchivingState {

    /**
     * The default archive name of an archive if not explicitly specified.
     */
    static final String DEFAULT_ARCHIVE_NAME = "gzipper_out";

    /**
     * Creates a new instance of {@link CompressState}.
     *
     * @param controller the controller to be aggregated.
     */
    CompressState(MainViewController controller) {
        super(controller);
    }

    private String determineOutputPath(File outputFile) {
        if (!outputFile.exists() || outputFile.isFile()) {
            return outputFile.getParent();
        }

        return FileUtils.getPath(outputFile);
    }

    private List<ArchiveOperation> createArchiveOperationsForEachSelectedFile(
            List<File> selectedFiles, ArchiveType archiveType) throws GZipperException {

        List<ArchiveOperation> operations;
        final List<ArchiveInfo> infos;
        final String outputPath = determineOutputPath(controller.getOutputFile());
        operations = new ArrayList<>(selectedFiles.size());

        if (controller.isPutFilesIntoSeparateArchives()) {
            infos = ArchiveInfoFactory.createArchiveInfos(archiveType,
                    controller.getCompressionLevel(), selectedFiles, outputPath);
        } else if (selectedFiles.size() == 1) {
            var info = ArchiveInfoFactory.createArchiveInfo(archiveType,
                    controller.getArchiveName(), controller.getCompressionLevel(),
                    selectedFiles, controller.getOutputFile().getParent());
            infos = new ArrayList<>(1);
            infos.add(info);
        } else {
            infos = ArchiveInfoFactory.createArchiveInfos(archiveType, controller.getArchiveName(),
                    controller.getCompressionLevel(), selectedFiles, outputPath);
        }

        for (ArchiveInfo info : infos) {
            var builder = new ArchiveOperation.Builder(info, CompressionMode.COMPRESS);
            builder.addListener(this).filterPredicate(_filterPredicate);
            operations.add(builder.build());
        }

        return operations;
    }

    @Override
    public boolean checkUpdateOutputPath() {

        var selectedFiles = controller.getSelectedFiles();
        String outputPath = controller.getTextOfOutputPathTextField();
        String extName = controller.getSelectedArchiveType().getDefaultExtensionName();

        if (FileUtils.isValidDirectory(outputPath) && !controller.isPutFilesIntoSeparateArchives()) {

            String archiveName = DEFAULT_ARCHIVE_NAME;

            if (selectedFiles.size() == 1) {
                var firstFile = selectedFiles.get(0);
                archiveName = firstFile.getName();
            }
            // user has not specified output filename
            outputPath = FileUtils.generateUniqueFilename(outputPath, archiveName, extName);
        }

        controller.setArchiveFileExtension(extName);

        if (FileUtils.isValidOutputFile(outputPath)) {
            controller.setTextOfOutputPathTextField(outputPath);
            return true;
        }

        return false;
    }

    @Override
    public void performOperations(ArchiveOperation... operations) {
        if (!ListUtils.isNullOrEmpty(controller.getSelectedFiles())) {
            super.performOperations(operations);
        } else {
            Log.e("Operation(s) cannot be started, because no files have been specified");
            Log.i(I18N.getString("noFilesSelectedWarning.text"), true);
        }
    }

    @Override
    public List<ArchiveOperation> initOperation(ArchiveType archiveType) throws GZipperException {

        List<ArchiveOperation> operations;
        var selectedFiles = controller.getSelectedFiles();

        if (controller.getSelectedArchiveType() == ArchiveType.GZIP || controller.isPutFilesIntoSeparateArchives()) {

            operations = createArchiveOperationsForEachSelectedFile(selectedFiles, archiveType);
        } else {
            operations = new ArrayList<>(1);
            var info = ArchiveInfoFactory.createArchiveInfo(archiveType, controller.getArchiveName(),
                    controller.getCompressionLevel(), selectedFiles, controller.getOutputFile().getParent());
            var archiveName = info.getArchiveName();
            controller.setArchiveName(archiveName);
            controller.setTextOfOutputPathTextField(FileUtils.combine(info.getOutputPath(), archiveName));
            var builder = new ArchiveOperation.Builder(info, CompressionMode.COMPRESS);
            builder.addListener(this).filterPredicate(_filterPredicate);
            operations.add(builder.build());
        }

        return operations;
    }
}
