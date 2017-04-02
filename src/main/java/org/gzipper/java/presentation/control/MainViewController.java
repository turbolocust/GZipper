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
package org.gzipper.java.presentation.control;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.Deflater;
import javafx.concurrent.Task;

import org.gzipper.java.application.model.OperatingSystem;
import org.gzipper.java.application.util.Settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.presentation.AlertDialog;
import org.gzipper.java.presentation.GZipper;
import org.gzipper.java.application.pojo.ArchiveInfo;
import org.gzipper.java.application.util.FileUtil;
import org.gzipper.java.application.util.TaskHandler;
import org.gzipper.java.exceptions.GZipperException;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.presentation.handler.TextAreaHandler;
import org.gzipper.java.presentation.util.ArchiveOperation;
import org.gzipper.java.presentation.util.ArchiveInfoFactory;
import org.gzipper.java.style.CSS;

/**
 * Controller for the FXML named "MainView.fxml".
 *
 * @author Matthias Fussenegger
 */
public class MainViewController extends BaseController {

    /**
     * The logger this class uses to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(MainViewController.class.getName());

    /**
     * Key constant used to access the properties map for menu items.
     */
    private static final String COMPRESSION_LEVEL_KEY = "compressionLevel";

    /**
     * The default archive name of an archive if not explicitly specified.
     */
    private static final String DEFAULT_ARCHIVE_NAME = "gzipper_out";

    /**
     * The currently active strategy for archiving operations. This can either
     * be a {@link CompressStrategy} or {@link DecompressStrategy}.
     */
    private ArchivingStrategy _strategy;

    /**
     * The {@link Future} object of the currently active task.
     */
    private Future<?> _activeTask;

    /**
     * The output file or directory that has been selected by the user.
     */
    private File _outputFile;

    /**
     * A list consisting of the files that have been selected by the user. These
     * can either be files to be packed or archives to be extracted.
     */
    private List<File> _selectedFiles;

    /**
     * The archive name specified by user.
     */
    private String _archiveName;

    /**
     * The compression level. Initialized with default compression level.
     */
    private int _compressionLevel = Deflater.DEFAULT_COMPRESSION;

    @FXML
    private MenuItem _noCompressionMenuItem;

    @FXML
    private MenuItem _bestSpeedCompressionMenuItem;

    @FXML
    private MenuItem _defaultCompressionMenuItem;

    @FXML
    private MenuItem _bestCompressionMenuItem;

    @FXML
    private MenuItem _closeMenuItem;

    @FXML
    private MenuItem _deleteMenuItem;

    @FXML
    private MenuItem _dropAddressesMenuItem;

    @FXML
    private RadioButton _compressRadioButton;

    @FXML
    private RadioButton _decompressRadioButton;

    @FXML
    private TextArea _textArea;

    @FXML
    private CheckMenuItem _enableLoggingCheckMenuItem;

    @FXML
    private CheckMenuItem _enableDarkThemeCheckMenuItem;

    @FXML
    private TextField _outputPathTextField;

    @FXML
    private ComboBox<ArchiveType> _archiveTypeComboBox;

    @FXML
    private Button _startButton;

    @FXML
    private Button _abortButton;

    @FXML
    private Button _selectFilesButton;

    @FXML
    private Button _saveAsButton;

    /**
     * Constructs a new controller with the specified CSS theme.
     *
     * @param theme the {@link CSS} theme to apply.
     */
    public MainViewController(CSS.Theme theme) {
        super(theme);
        _archiveName = DEFAULT_ARCHIVE_NAME;
        Logger.getLogger(GZipper.class.getName()).log(Level.INFO,
                "Default archive name set to: {0}", _archiveName);
    }

    @FXML
    void handleCompressionLevelMenuItemAction(ActionEvent evt) {
        final MenuItem selectedItem = (MenuItem) evt.getSource();
        Object compressionStrength = selectedItem.getProperties().get(COMPRESSION_LEVEL_KEY);

        if (compressionStrength != null) {
            _compressionLevel = (int) compressionStrength;
            Logger.getLogger(GZipper.class.getName()).log(Level.INFO,
                    "Compression level set to: {0}", _compressionLevel);
            LOGGER.log(Level.INFO, "{0}{1}", new Object[]{I18N.getString(
                "compressionLevelChange.text"), selectedItem.getText()});
        }
    }

    @FXML
    void handleCloseMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_closeMenuItem)) {
            close();
            System.exit(0);
        }
    }

    @FXML
    void handleDeleteMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_deleteMenuItem)) {
            Optional<ButtonType> result = AlertDialog.showConfirmationDialog(
                    I18N.getString("clearTextWarning.text"),
                    I18N.getString("confirmation.text"));
            if (result.isPresent() && result.get() == ButtonType.YES) {
                _textArea.clear();
                _textArea.setText("run:\n");
            }
        }
    }

    @FXML
    void handleDropAddressesMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_dropAddressesMenuItem)) {
            List<String> files = ViewControllers
                    .showDropView(_theme).getAddresses();
            if (files != null && !files.isEmpty()) {
                _selectedFiles = new ArrayList<>(files.size());
                files.forEach((filePath) -> {
                    _selectedFiles.add(new File(filePath));
                    LOGGER.log(Level.INFO, "{0}: {1}", new Object[]{I18N.getString(
                        "fileSelected.text"), filePath});
                });
            } else {
                LOGGER.log(Level.INFO, I18N.getString("noFilesSelected.text"));
            }
        }
    }

    @FXML
    void handleStartButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_startButton)) {
            try {
                if (_strategy.validateOutputPath()) {
                    String outputPath = _outputPathTextField.getText();
                    if (!_outputFile.getAbsolutePath().equals(outputPath)) {
                        _outputFile = new File(outputPath);
                        if (!_outputFile.isDirectory()) {
                            _archiveName = _outputFile.getName();
                        }
                    }
                    String archiveType = _archiveTypeComboBox.getValue().getName();
                    ArchiveOperation[] operations = _strategy.initOperation(archiveType);
                    for (ArchiveOperation operation : operations) {
                        Logger.getLogger(GZipper.class.getName()).log(Level.INFO,
                                "Operation started using the following archive info: {0}",
                                operation.getArchiveInfo().toString());
                        //_strategy.performOperation(operation);
                    }
                } else {
                    LOGGER.log(Level.WARNING, I18N.getString("invalidOutputPath.text"));
                }
            } catch (GZipperException ex) {
                Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
                LOGGER.log(Level.SEVERE, ex.getMessage());
            }
        }
    }

    @FXML
    void handleAbortButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_abortButton)) {
            if (_activeTask != null && !_activeTask.isCancelled()) {
                final boolean cancelled = _activeTask.cancel(true);
                if (!cancelled) {
                    Logger.getLogger(GZipper.class.getName()).log(Level.WARNING,
                            "Task cancellation failed for {0}", _activeTask.toString());
                }
            }
        }
    }

    @FXML
    void handleSelectFilesButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_selectFilesButton)) {

            FileChooser fc = new FileChooser();
            if (_compressRadioButton.isSelected()) {
                fc.setTitle(I18N.getString("browseForFiles.text"));
            } else {
                fc.setTitle(I18N.getString("browseForArchive.text"));
                _strategy.applyExtensionFilters(fc);
            }

            _selectedFiles = fc.showOpenMultipleDialog(_primaryStage);

            String message;
            if (_selectedFiles != null) {
                _startButton.setDisable(false);
                final int selectedFiles = _selectedFiles.size();
                message = I18N.getString("filesSelected.text");
                message = message.replace("{0}", Integer.toString(selectedFiles));
                // log the path of each selected file
                _selectedFiles.forEach((file) -> {
                    LOGGER.log(Level.INFO, "{0}: {1}", new Object[]{
                        I18N.getString("fileSelected.text"), file.getAbsolutePath()});
                });
            } else {
                _startButton.setDisable(true);
                message = I18N.getString("noFilesSelected.text");
            }
            LOGGER.log(Level.INFO, message);
        }
    }

    @FXML
    void handleSaveAsButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_saveAsButton)) {

            File file;
            if (_compressRadioButton.isSelected()) {
                FileChooser fc = new FileChooser();
                fc.setTitle(I18N.getString("saveAsArchiveTitle.text"));
                _strategy.applyExtensionFilters(fc);
                file = fc.showSaveDialog(_primaryStage);
            } else {
                DirectoryChooser dc = new DirectoryChooser();
                dc.setTitle(I18N.getString("saveAsPathTitle.text"));
                file = dc.showDialog(_primaryStage);
            }

            if (file != null) {
                updateSelectedFile(file);
                _outputPathTextField.setText(file.getAbsolutePath());
                Logger.getLogger(GZipper.class.getName()).log(Level.INFO,
                        "Output directory set to: {0}", file.getAbsolutePath());
            }
        }
    }

    @FXML
    void handleCompressRadioButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_compressRadioButton)) {
            _strategy = new CompressStrategy();
            _selectFilesButton.setText(I18N.getString("browseForFiles.text"));
            _saveAsButton.setText(I18N.getString("saveAsArchive.text"));
        }
    }

    @FXML
    void handleDecompressRadioButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_decompressRadioButton)) {
            _strategy = new DecompressStrategy();
            _selectFilesButton.setText(I18N.getString("browseForArchive.text"));
            _saveAsButton.setText(I18N.getString("saveAsFiles.text"));
        }
    }

    @FXML
    void handleArchiveTypeComboBoxAction(ActionEvent evt) {
        if (evt.getSource().equals(_archiveTypeComboBox)) {
            int i = _archiveTypeComboBox.getSelectionModel().getSelectedIndex();
            Logger.getLogger(GZipper.class.getName()).log(Level.INFO,
                    "Archive type selection change to: {0}",
                    _archiveTypeComboBox.getItems().get(i)
            );
        }
    }

    @FXML
    void onOutputPathTextFieldKeyTyped(KeyEvent evt) {
        if (evt.getSource().equals(_outputPathTextField)) {
            String fileName = _outputPathTextField.getText();
            if (!FileUtil.containsIllegalChars(fileName)) {
                updateSelectedFile(new File(fileName));
            }
        }
    }

    @FXML
    void handleEnableLoggingCheckMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_enableLoggingCheckMenuItem)) {
            boolean enableLogging = _enableLoggingCheckMenuItem.isSelected();
            Settings.getInstance().setProperty("loggingEnabled", enableLogging);
        }
    }

    @FXML
    void handleEnableDarkThemeCheckMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_enableDarkThemeCheckMenuItem)) {
            boolean enableTheme = _enableDarkThemeCheckMenuItem.isSelected();
            loadAlternativeTheme(enableTheme, CSS.Theme.DARK_THEME);
            Settings.getInstance().setProperty("darkThemeEnabled", enableTheme);
        }
    }

    /**
     * Toggles both, the start and the abort button.
     */
    private void toggleStartAndAbortButton() {
        // toggle start button
        if (_startButton.isDisabled()) {
            _startButton.setDisable(false);
        } else {
            _startButton.setDisable(true);
        }
        // toggle abort button
        if (_abortButton.isDisabled()) {
            _abortButton.setDisable(false);
        } else {
            _abortButton.setDisable(true);
        }
    }

    /**
     * Updates the selected file and the archive name if it is not a directory.
     *
     * @param file the updated file.
     */
    private void updateSelectedFile(File file) {
        if (file != null) {
            if (!file.isDirectory()) {
                _archiveName = file.getName();
            }
            _outputFile = file;
        }
    }

    /**
     * Initializes the archiving job by creating the required {@link Task}. This
     * task will not perform the algorithmic operations for archives but instead
     * constantly check for interruption to properly detect the abort of an
     * operation. For the algorithmic operations a new task will be created and
     * submitted to the task handler. If an operation has been aborted, e.g.
     * through user interaction, the operation will be interrupted.
     *
     * @param operation the {@link ArchiveOperation} that will eventually be
     * performed by the task when executed.
     * @return a {@link Task} that can be executed to perform the specified
     * archiving operation.
     */
    private Task<Boolean> initArchivingJob(ArchiveOperation operation) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                final TaskHandler handler = TaskHandler.getInstance();
                Future<Boolean> futureTask = handler.submit(operation);

                while (!futureTask.isDone()) {
                    try {
                        Thread.sleep(10); // check for interruption
                    } catch (InterruptedException ex) {
                        // if exception is caught, task has been interrupted
                        LOGGER.log(Level.INFO, I18N.getString("interrupt.text"));
                        Logger.getLogger(GZipper.class.getName()).log(
                                Level.WARNING, "Operation has been interrupted.", ex);
                        operation.interrupt();
                        boolean cancelled = futureTask.cancel(true);

                        if (cancelled) {
                            LOGGER.log(Level.INFO, I18N.getString("operationCancel.text"));
                        }
                    }
                }
                return futureTask.get();
            }
        };
        // show success message and finalize archiving job when task has succeeded
        task.setOnSucceeded(e -> {
            boolean success = (boolean) e.getSource().getValue();
            if (success) {
                LOGGER.log(Level.INFO, I18N.getString("operationSuccess.text"));
            } else { // operation failed
                LOGGER.log(Level.SEVERE, I18N.getString("operationFail.text"));
                LOGGER.log(Level.WARNING, I18N.getString("missingAccessRights.text"));
            }
            finalizeArchivingJob(operation);
        });
        // show error message when task has failed and finalize archiving job
        task.setOnFailed(e -> {
            LOGGER.log(Level.INFO, I18N.getString("operationFail.text"));
            final Throwable throwable = e.getSource().getException();
            if (throwable != null) {
                Logger.getLogger(GZipper.class.getName()).log(Level.WARNING, null, throwable);
            }
            // delete corrupt archive on operation fail
            if (operation.isCompress()) {
                ArchiveInfo info = operation.getArchiveInfo();
                final String archive = FileUtil.combinePathAndFileName(
                        info.getOutputPath(), info.getArchiveName());
                try {
                    if (FileUtil.delete(archive)) {
                        Logger.getLogger(GZipper.class.getName()).log(Level.WARNING,
                                "Archive file deleted: {0}", archive);
                    } else {
                        Logger.getLogger(GZipper.class.getName()).log(Level.WARNING,
                                "Archive could not be deleted as it no longer exists.");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE,
                            "I/O error occurred while trying to delete "
                            + "corrupt archive after fail of operation.", ex);
                }
            }
            finalizeArchivingJob(operation);
        });

        return task;
    }

    /**
     * Calculates the total duration in seconds of the specified
     * {@link ArchiveOperation} and appends it via
     * {@link #appendToTextArea(java.lang.String)}. Also toggles
     * {@link #_startButton} and {@link #_abortButton}.
     *
     * @param operation {@link ArchiveOperation} that holds elapsed time.
     */
    private void finalizeArchivingJob(ArchiveOperation operation) {
        LOGGER.log(Level.INFO, "{0}{1} seconds.",
                new Object[]{I18N.getString("elapsedTime.text"),
                    operation.calculateElapsedTime()});
        toggleStartAndAbortButton();
    }

    /**
     * Loads an alternative theme.
     *
     * @param enableTheme true to enable, false to disable alternative theme.
     * @param theme the theme to load.
     */
    private void loadAlternativeTheme(boolean enableTheme, CSS.Theme theme) {
        final String sheetLocation = GZipper.class.getResource(
                theme.getLocation()).toExternalForm();
        _theme = enableTheme ? theme : CSS.Theme.getDefault();
        _stages.forEach((stage) -> {
            if (enableTheme) {
                stage.getScene().getStylesheets().add(sheetLocation);
            } else {
                stage.getScene().getStylesheets().clear();
            }
        });
    }

    /**
     * Initializes the logger that will append text to {@link #_textArea}.
     */
    private void initLogger() {
        Logger logger = Logger.getLogger(MainViewController.class.getName());
        TextAreaHandler handler = new TextAreaHandler(_textArea);
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final Settings settings = Settings.getInstance();
        OperatingSystem os = settings.getOperatingSystem();

        initLogger();

        // set recently used path from settings if valid
        final String recentPath = settings.getProperty("recentPath");
        if (FileUtil.isValidDirectory(recentPath)) {
            _outputPathTextField.setText(recentPath);
        } else {
            _outputPathTextField.setText(os.getDefaultUserDirectory());
        }
        _outputFile = new File(_outputPathTextField.getText());

        // set dark theme as enabled if done so on previous application launch
        if (_theme == CSS.Theme.DARK_THEME) {
            _enableDarkThemeCheckMenuItem.setSelected(true);
        }

        // set up properties for menu items regarding compression level
        _noCompressionMenuItem.getProperties().put(
                COMPRESSION_LEVEL_KEY, Deflater.NO_COMPRESSION);
        _bestSpeedCompressionMenuItem.getProperties().put(
                COMPRESSION_LEVEL_KEY, Deflater.BEST_SPEED);
        _defaultCompressionMenuItem.getProperties().put(
                COMPRESSION_LEVEL_KEY, Deflater.DEFAULT_COMPRESSION);
        _bestCompressionMenuItem.getProperties().put(
                COMPRESSION_LEVEL_KEY, Deflater.BEST_COMPRESSION);

        // set up combo box items
        final ArchiveType[] archiveTypes = ArchiveType.values();
        _archiveTypeComboBox.getItems().addAll(archiveTypes);
        _archiveTypeComboBox.setValue(archiveTypes[0]);

        // set menu item for logging as selected if logging has been enabled before
        final String loggingEnabled = settings.getProperty("loggingEnabled");
        _enableLoggingCheckMenuItem.setSelected(loggingEnabled.equalsIgnoreCase("true"));

        // set up strategy, frame image and the default text for the text area
        _strategy = new CompressStrategy();
        _frameImage = new Image("/images/icon_32.png");
        _textArea.setText("run:\n" + I18N.getString("changeOutputPath.text") + "\n");
    }

    /**
     * Inner class that represents the currently active strategy. This can
     * either be the {@link CompressStrategy} or {@link DecompressStrategy}.
     */
    private abstract class ArchivingStrategy {

        /**
         * Validates the output path for the concrete strategy.
         *
         * @return true if output path is valid, false otherwise.
         */
        public abstract boolean validateOutputPath();

        /**
         * Applies the required extension filters for the concrete strategy to
         * the specified {@link FileChooser}.
         *
         * @param chooser the {@link FileChooser} to which the extension filters
         * will be applied to.
         */
        public abstract void applyExtensionFilters(FileChooser chooser);

        /**
         * Initializes the archiving operation.
         *
         * @param archiveType the type of the archive, see {@link ArchiveType}.
         * @return an array consisting of {@link ArchiveOperation} objects.
         * @throws GZipperException if the archive type could not have been
         * determined.
         */
        public abstract ArchiveOperation[] initOperation(
                String archiveType) throws GZipperException;

        /**
         * Performs the specified {@link ArchiveOperation} using the concrete
         * strategy.
         *
         * @param operation the {@link ArchiveOperation} to be performed.
         */
        public void performOperation(ArchiveOperation operation) {
            if (operation != null) {
                Task<Boolean> task = initArchivingJob(operation);
                ArchiveInfo info = operation.getArchiveInfo();
                LOGGER.log(Level.INFO, "{0}{1}",
                        new Object[]{I18N.getString("outputPath.text"),
                            info.getOutputPath()});
                Logger.getLogger(GZipper.class.getName()).log(Level.INFO,
                        I18N.getString("operationStarted.text"),
                        new Object[]{info.getArchiveType().getDisplayName(), info.getOutputPath()});
                _activeTask = TaskHandler.getInstance().submit(task);
                toggleStartAndAbortButton();
            }
        }
    }

    /**
     * Strategy that will be used if user has activated the compression mode.
     */
    private class CompressStrategy extends ArchivingStrategy {

        @Override
        public boolean validateOutputPath() {
            return FileUtil.isValidFileName(_outputPathTextField.getText());
        }

        @Override
        public void performOperation(ArchiveOperation operation) {
            if (_selectedFiles != null) {
                super.performOperation(operation);
            } else {
                Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE,
                        "Operation cannot be started as no files have been specified.");
                LOGGER.log(Level.INFO, I18N.getString("noFilesSelectedWarning.text"));
            }
        }

        @Override
        public ArchiveOperation[] initOperation(String archiveType)
                throws GZipperException {
            ArchiveInfo info = ArchiveInfoFactory.createArchiveInfo(
                    archiveType, _archiveName, _compressionLevel,
                    _selectedFiles, _outputFile.getParent());
            return new ArchiveOperation[]{new ArchiveOperation(info, true)};
        }

        @Override
        public void applyExtensionFilters(FileChooser chooser) {
            if (chooser != null) {
                final ArchiveType selectedType = _archiveTypeComboBox
                        .getSelectionModel()
                        .getSelectedItem();
                for (ArchiveType type : ArchiveType.values()) {
                    if (type.equals(selectedType)) {
                        ExtensionFilter extFilter = new ExtensionFilter(
                                type.getDisplayName(), type.getExtensionNames());
                        chooser.getExtensionFilters().add(extFilter);
                    }
                }
            }
        }
    }

    /**
     * Strategy that will be used if user has activated the decompression mode.
     */
    private class DecompressStrategy extends ArchivingStrategy {

        @Override
        public boolean validateOutputPath() {
            return FileUtil.isValidDirectory(_outputPathTextField.getText());
        }

        @Override
        public void performOperation(ArchiveOperation operation) {
            if (_outputFile != null) {
                super.performOperation(operation);
            } else {
                Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE,
                        "Operation cannot be started as an invalid path has been specified.");
                LOGGER.log(Level.WARNING, I18N.getString("outputPathWarning.text"));
                _outputPathTextField.requestFocus();
            }
        }

        @Override
        public ArchiveOperation[] initOperation(String archiveType)
                throws GZipperException {
            // array consisting of operations for each selected archive
            ArchiveOperation[] operations
                    = new ArchiveOperation[_selectedFiles.size()];
            // create new operation for each archive to be extracted
            for (int i = 0; i < _selectedFiles.size(); ++i) {
                final File file = _selectedFiles.get(i);
                ArchiveInfo info = ArchiveInfoFactory.createArchiveInfo(
                        archiveType, file.getAbsolutePath(),
                        _outputFile + File.separator);
                operations[i] = new ArchiveOperation(info, false);
            }
            return operations;
        }

        @Override
        public void applyExtensionFilters(FileChooser chooser) {
            if (chooser != null) {
                for (ArchiveType type : ArchiveType.values()) {
                    ExtensionFilter extFilter = new ExtensionFilter(
                            type.getDisplayName(), type.getExtensionNames());
                    chooser.getExtensionFilters().add(extFilter);
                }
            }
        }
    }
}
