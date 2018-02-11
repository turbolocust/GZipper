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
package org.gzipper.java.presentation.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;
import java.util.zip.Deflater;

import org.gzipper.java.application.ArchiveOperation;
import org.gzipper.java.application.CompressionMode;
import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.application.model.OperatingSystem;
import org.gzipper.java.application.observer.Listener;
import org.gzipper.java.application.observer.Notifier;
import org.gzipper.java.application.pojo.ArchiveInfo;
import org.gzipper.java.application.pojo.ArchiveInfoFactory;
import org.gzipper.java.application.util.FileUtils;
import org.gzipper.java.application.util.ListUtils;
import org.gzipper.java.application.util.TaskHandler;
import org.gzipper.java.exceptions.GZipperException;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.presentation.Dialogs;
import org.gzipper.java.presentation.ProgressManager;
import org.gzipper.java.presentation.handler.TextAreaHandler;
import org.gzipper.java.presentation.style.CSS;
import org.gzipper.java.util.Log;
import org.gzipper.java.util.Settings;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.converter.PercentageStringConverter;
import org.gzipper.java.application.predicates.PatternPredicate;
import org.gzipper.java.application.util.StringUtils;

/**
 * Controller for the FXML named "MainView.fxml".
 *
 * @author Matthias Fussenegger
 */
public class MainViewController extends BaseController {

    /**
     * Key constant used to access the properties map for menu items.
     */
    private static final String COMPRESSION_LEVEL_KEY = "compressionLevel";

    /**
     * Map consisting of {@link Future} objects representing the currently
     * active tasks.
     */
    private final ConcurrentMap<String, Future<?>> _activeTasks;

    /**
     * The currently active state.
     */
    private ArchivingState _state;

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
     * The file extension of the archive type.
     */
    private String _archiveFileExtension;

    /**
     * The compression level. Initialized with default compression level.
     */
    private int _compressionLevel;

    @FXML
    private MenuItem _applyFilterMenuItem;
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
    private MenuItem _resetAppMenuItem;
    @FXML
    private MenuItem _aboutMenuItem;
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
    @FXML
    private ProgressBar _progressBar;
    @FXML
    private Text _progressText;

    /**
     * Constructs a controller for Main View with the specified CSS theme and
     * host services.
     *
     * @param theme the {@link CSS} theme to apply.
     * @param hostServices the host services to aggregate.
     */
    public MainViewController(CSS.Theme theme, HostServices hostServices) {
        super(theme, hostServices);
        _archiveName = DEFAULT_ARCHIVE_NAME;
        _compressionLevel = Deflater.DEFAULT_COMPRESSION;
        _activeTasks = new ConcurrentHashMap<>();
        Log.i("Default archive name set to: {0}", _archiveName, false);
    }

    /**
     * Cancels all currently active tasks.
     */
    public void cancelActiveTasks() {
        if (_activeTasks != null && !_activeTasks.isEmpty()) {
            _activeTasks.keySet().stream().map((key) -> _activeTasks.get(key))
                    .filter((task) -> (!task.cancel(true))).forEachOrdered((task) -> {
                // log warning message only when cancellation failed
                Log.e("Task cancellation failed for {0}", task.toString());
            });
        }
    }

    @FXML
    void handleApplyFilterMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_applyFilterMenuItem)) {
            final Optional<String> result = Dialogs
                    .showPatternInputDialog(_theme, getIconImage());
            if (result.isPresent()) {
                if (!result.get().isEmpty()) {
                    final Pattern pattern = Pattern.compile(result.get());
                    _state.setFilterPredicate(new PatternPredicate(pattern));
                    Log.i(I18N.getString("filterApplied.text", result.get()), true);
                } else { // reset filter
                    _state.setFilterPredicate(null);
                    Log.i(I18N.getString("filterReset.text", result.get()), true);
                }
            }
        }
    }

    @FXML
    void handleCompressionLevelMenuItemAction(ActionEvent evt) {
        final MenuItem selectedItem = (MenuItem) evt.getSource();
        final Object compressionStrength = selectedItem
                .getProperties().get(COMPRESSION_LEVEL_KEY);
        if (compressionStrength != null) {
            _compressionLevel = (int) compressionStrength;
            Log.i("{0}{1}", true, I18N.getString("compressionLevelChange.text"),
                    selectedItem.getText());
        }
    }

    @FXML
    void handleCloseMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_closeMenuItem)) {
            cancelActiveTasks();
            exit();
        }
    }

    @FXML
    void handleDeleteMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_deleteMenuItem)) {
            final Optional<ButtonType> result = Dialogs
                    .showConfirmationDialog(I18N.getString("clearText.text"),
                            I18N.getString("clearTextConfirmation.text"),
                            I18N.getString("confirmation.text"), _theme, getIconImage());
            if (result.isPresent() && result.get() == ButtonType.YES) {
                _textArea.clear();
                _textArea.setText("run:\n");
            }
        }
    }

    @FXML
    void handleDropAddressesMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_dropAddressesMenuItem)) {
            final List<String> filePaths = ViewControllers
                    .showDropView(_theme).getAddresses();
            if (!ListUtils.isNullOrEmpty(filePaths)) {
                final int size = filePaths.size();
                _selectedFiles = new ArrayList<>(size);
                _startButton.setDisable(false);
                if (size > 10) { // threshold, to avoid flooding text area
                    filePaths.forEach((filePath) -> {
                        _selectedFiles.add(new File(filePath));
                    });
                    Log.i(I18N.getString("manyFilesSelected.text"), true, size);
                } else { // log files in detail
                    filePaths.stream().map((filePath) -> {
                        _selectedFiles.add(new File(filePath));
                        return filePath;
                    }).forEachOrdered((filePath) -> {
                        Log.i("{0}: {1}", true, I18N.getString("fileSelected.text"), filePath);
                    });
                }
            } else {
                Log.i(I18N.getString("noFilesSelected.text"), true);
                _startButton.setDisable(ListUtils.isNullOrEmpty(_selectedFiles));
            }
        }
    }

    @FXML
    void handleResetAppMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_resetAppMenuItem)) {
            final Optional<ButtonType> result = Dialogs
                    .showConfirmationDialog(I18N.getString("resetApp.text"),
                            I18N.getString("resetAppConfirmation.text"),
                            I18N.getString("confirmation.text"), _theme, getIconImage());
            if (result.isPresent() && result.get() == ButtonType.YES) {
                Settings.getInstance().restoreDefaults();
            }
        }
    }

    @FXML
    void handleAboutMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_aboutMenuItem)) {
            ViewControllers.showAboutView(_theme, _hostServices);
        }
    }

    @FXML
    void handleStartButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_startButton)) {
            try {
                if (_state.validateOutputPath()) {
                    final String outputPath = _outputPathTextField.getText();
                    if (!_outputFile.getAbsolutePath().equals(outputPath)) {
                        _outputFile = new File(outputPath);
                        if (!_outputFile.isDirectory()) {
                            _archiveName = _outputFile.getName();
                        }
                    }
                    final String recentPath = FileUtils.getParent(outputPath);
                    Settings.getInstance().setProperty("recentPath", recentPath);
                    final ArchiveType archiveType = _archiveTypeComboBox.getValue();
                    for (ArchiveOperation operation : _state.initOperation(archiveType)) {
                        Log.i("Operation started using the following archive info: {0}",
                                operation.getArchiveInfo().toString(), false);
                        _state.performOperation(operation);
                    }
                } else {
                    Log.w(I18N.getString("invalidOutputPath.text"), true);
                }
            } catch (GZipperException ex) {
                Log.e(ex.getLocalizedMessage(), ex);
            }
        }
    }

    @FXML
    void handleAbortButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_abortButton)) {
            cancelActiveTasks();
        }
    }

    @FXML
    void handleSelectFilesButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_selectFilesButton)) {

            final FileChooser fc = new FileChooser();
            if (_compressRadioButton.isSelected()) {
                fc.setTitle(I18N.getString("browseForFiles.text"));
            } else {
                fc.setTitle(I18N.getString("browseForArchive.text"));
                _state.applyExtensionFilters(fc);
            }

            final List<File> selectedFiles
                    = fc.showOpenMultipleDialog(_primaryStage);

            String message;
            if (selectedFiles != null) {
                _startButton.setDisable(false);
                int size = selectedFiles.size();
                message = I18N.getString("filesSelected.text", size);
                if (size > 10) {
                    Log.i(I18N.getString("manyFilesSelected.text"), true, size);
                } else {
                    selectedFiles.forEach((file) -> { // log the path of each selected file
                        Log.i("{0}: \"{1}\"", true,
                                I18N.getString("fileSelected.text"),
                                file.getAbsolutePath());
                    });
                }
                _selectedFiles = selectedFiles;
            } else {
                message = I18N.getString("noFilesSelected.text");
                _startButton.setDisable(ListUtils.isNullOrEmpty(_selectedFiles));
            }
            Log.i(message, true);
        }
    }

    @FXML
    void handleSaveAsButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_saveAsButton)) {

            final File file;
            if (_compressRadioButton.isSelected()) {
                final FileChooser fc = new FileChooser();
                fc.setTitle(I18N.getString("saveAsArchiveTitle.text"));
                _state.applyExtensionFilters(fc);
                file = fc.showSaveDialog(_primaryStage);
            } else {
                final DirectoryChooser dc = new DirectoryChooser();
                dc.setTitle(I18N.getString("saveAsPathTitle.text"));
                file = dc.showDialog(_primaryStage);
            }

            if (file != null) {
                updateSelectedFile(file);
                String absolutePath = file.getAbsolutePath();
                if (!file.isDirectory() && FileUtils.getExtension(absolutePath).isEmpty()) {
                    absolutePath += _archiveFileExtension;
                }
                _outputPathTextField.setText(absolutePath);
                Log.i("Output file set to: {0}", file.getAbsolutePath(), false);
            }
        }
    }

    @FXML
    void handleModeRadioButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_compressRadioButton)) {
            performModeRadioButtonAction(true, "browseForFiles.text", "saveAsArchive.text");
        } else if (evt.getSource().equals(_decompressRadioButton)) {
            performModeRadioButtonAction(false, "browseForArchive.text", "saveAsFiles.text");
        }
        resetSelections();
    }

    @FXML
    void handleArchiveTypeComboBoxAction(ActionEvent evt) {
        if (evt.getSource().equals(_archiveTypeComboBox)) {
            ArchiveType type = _archiveTypeComboBox.getValue();
            Log.i("Archive type selection change to: {0}", type, false);
            if (type == ArchiveType.GZIP) {
                performGzipSelectionAction();
            }
            if (_decompressRadioButton.isSelected()) {
                resetSelections();
            } else { // update file extension
                String outputPathText = _outputPathTextField.getText(),
                        fileExtension = type.getDefaultExtensionName(false);
                String outputPath;
                if (outputPathText.endsWith(_archiveFileExtension)) {
                    outputPath = outputPathText.replace(_archiveFileExtension, fileExtension);
                } else if (!FileUtils.isValidDirectory(outputPathText)) {
                    outputPath = outputPathText + fileExtension;
                } else {
                    outputPath = outputPathText;
                }
                _outputPathTextField.setText(outputPath);
                _archiveFileExtension = fileExtension;
            }
        }
    }

    @FXML
    void onOutputPathTextFieldKeyTyped(KeyEvent evt) {
        if (evt.getSource().equals(_outputPathTextField)) {
            String filename = _outputPathTextField.getText();
            if (!FileUtils.containsIllegalChars(filename)) {
                updateSelectedFile(new File(filename + evt.getCharacter()));
            }
        }
    }

    @FXML
    void handleEnableLoggingCheckMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_enableLoggingCheckMenuItem)) {
            boolean enableLogging = _enableLoggingCheckMenuItem.isSelected();
            Settings.getInstance().setProperty("loggingEnabled", enableLogging);
            Log.setVerboseUiLogging(enableLogging);
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
     * Called when the archiving mode has been changed via a radio button.
     *
     * @param compress true for compress, false for decompress.
     * @param selectFilesButtonText text to display on the button which brings
     * up a dialog to select files or an archive.
     * @param saveAsButtonText text to display on the button which brings up the
     * "Save as..." dialog.
     */
    private void performModeRadioButtonAction(boolean compress,
            String selectFilesButtonText, String saveAsButtonText) {
        _state = compress ? new CompressState() : new DecompressState();
        _selectFilesButton.setText(I18N.getString(selectFilesButtonText));
        _saveAsButton.setText(I18N.getString(saveAsButtonText));
    }

    /**
     * Called when the GZIP type has been selected in the combo box of types.
     */
    private void performGzipSelectionAction() {
        final Settings settings = Settings.getInstance();
        final String propertyKey = "showGzipInfoDialog";
        boolean showDialog = settings.evaluateProperty(propertyKey);
        if (showDialog) {
            final String infoTitle = I18N.getString("info.text");
            final String infoText = I18N.getString("gzipCompressionInfo.text",
                    ArchiveType.TAR_GZ.getDisplayName());
            final StringBuilder infoContent = new StringBuilder(infoText);
            infoContent.append("\n\n").append(I18N.getString("dialogWontShowAgain.text"));
            Dialogs.showDialog(Alert.AlertType.INFORMATION, infoTitle, infoTitle,
                    infoContent.toString(), _theme, getIconImage());
            settings.setProperty(propertyKey, false);
        }
    }

    /**
     * Toggles UI controls which react to user input.
     *
     * @param start true to disable controls, false to enable them.
     */
    private void toggleUIcontrols(boolean start) {
        _startButton.setDisable(start);
        _abortButton.setDisable(!start);
        _compressRadioButton.setDisable(start);
        _decompressRadioButton.setDisable(start);
        _archiveTypeComboBox.setDisable(start);
        _saveAsButton.setDisable(start);
        _selectFilesButton.setDisable(start);
        _dropAddressesMenuItem.setDisable(start);
    }

    /**
     * Resets the selected files.
     */
    private void resetSelections() {
        if (!ListUtils.isNullOrEmpty(_selectedFiles)) {
            Log.i(I18N.getString("selectionReset.text"), true);
        }
        _selectedFiles = Collections.<File>emptyList();
        _startButton.setDisable(true);
    }

    /**
     * Updates the selected file, the archive name and the file name extension
     * of the archive if it is not a directory.
     *
     * @param file the updated file.
     */
    private void updateSelectedFile(File file) {
        if (file != null) {
            if (!file.isDirectory()) {
                String archiveName = _archiveName = file.getName(),
                        fileExtension = FileUtils.getExtension(archiveName);
                if (fileExtension.isEmpty()) { // update file extension
                    fileExtension = _archiveTypeComboBox.getValue().getDefaultExtensionName(false);
                }
                _archiveFileExtension = fileExtension;
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
    @SuppressWarnings("SleepWhileInLoop")
    private Task<Boolean> initArchivingJob(final ArchiveOperation operation) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                Future<Boolean> futureTask = TaskHandler.submit(operation);

                while (!futureTask.isDone()) {
                    try {
                        Thread.sleep(10); // check for interruption
                    } catch (InterruptedException ex) {
                        // if exception is caught, task has been interrupted
                        Log.i(I18N.getString("interrupt.text"), true);
                        Log.w(ex.getLocalizedMessage(), false);
                        operation.interrupt();
                        if (futureTask.cancel(true)) {
                            Log.i(I18N.getString("operationCancel.text"), true, operation);
                        }
                    }
                }
                try { // check for cancellation
                    return futureTask.get();
                } catch (CancellationException ex) {
                    return false;
                }
            }
        };
        // show success message and finalize archiving job when task has
        // succeeded
        task.setOnSucceeded(e -> {
            boolean success = (boolean) e.getSource().getValue();
            if (success) {
                Log.i(I18N.getString("operationSuccess.text"), true, operation);
            } else {
                Log.w(I18N.getString("operationNoSuccess.text"), true, operation);
            }
            finishArchivingJob(operation, task);
        });
        // show error message when task has failed and finalize archiving job
        task.setOnFailed(e -> {
            Log.i(I18N.getString("operationFail.text"), true, operation);
            final Throwable thrown = e.getSource().getException();
            if (thrown != null) {
                Log.e(thrown.getLocalizedMessage(), thrown);
            }
            finishArchivingJob(operation, task);
        });
        return task;
    }

    /**
     * Calculates the total duration in seconds of the specified
     * {@link ArchiveOperation} and logs it to {@link #_textArea}. Also toggles
     * {@link #_startButton} and {@link #_abortButton}.
     *
     * @param operation {@link ArchiveOperation} that holds elapsed time.
     * @param task the task that will be removed from {@link #_activeTasks}.
     */
    private void finishArchivingJob(ArchiveOperation operation, Task<?> task) {
        Log.i(I18N.getString("elapsedTime.text"), true,
                operation.calculateElapsedTime());
        _activeTasks.remove(task.toString());
        if (_activeTasks.isEmpty()) {
            _progressBar.setProgress(0d); // reset
            _progressText.setText(StringUtils.EMPTY);
            toggleUIcontrols(false);
        }
    }

    /**
     * Initializes the logger that will append text to {@link #_textArea}.
     */
    private void initLogger() {
        Logger logger = Log.UI_LOGGER;
        TextAreaHandler handler = new TextAreaHandler(_textArea);
        handler.setFormatter(new SimpleFormatter());
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final Settings settings = Settings.getInstance();
        final OperatingSystem os = settings.getOs();

        initLogger();

        // set recently used path from settings if valid
        final String recentPath = settings.getProperty("recentPath");
        if (FileUtils.isValidDirectory(recentPath)) {
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
        _noCompressionMenuItem.getProperties().put(COMPRESSION_LEVEL_KEY, Deflater.NO_COMPRESSION);
        _bestSpeedCompressionMenuItem.getProperties().put(COMPRESSION_LEVEL_KEY, Deflater.BEST_SPEED);
        _defaultCompressionMenuItem.getProperties().put(COMPRESSION_LEVEL_KEY, Deflater.DEFAULT_COMPRESSION);
        _bestCompressionMenuItem.getProperties().put(COMPRESSION_LEVEL_KEY, Deflater.BEST_COMPRESSION);

        // set up combo box items and set default selected type
        final ArchiveType selectedType = ArchiveType.TAR_GZ;
        _archiveTypeComboBox.getItems().addAll(ArchiveType.values());
        _archiveTypeComboBox.setValue(selectedType);
        _archiveFileExtension = selectedType.getDefaultExtensionName(false);

        // set menu item for logging as selected if logging has been enabled
        final boolean enableLogging = settings.evaluateProperty("loggingEnabled");
        _enableLoggingCheckMenuItem.setSelected(enableLogging);

        // set up initial state, window icon and the default text for the text area
        _state = new CompressState();
        _iconImage = new Image("/images/icon_32.png");
        final String formattedText = String.format("run:\n%s\n",
                I18N.getString("changeOutputPath.text"));
        _textArea.setText(formattedText);
    }

    /**
     * Inner class that represents the currently active state. This can either
     * be the {@link CompressState} or {@link DecompressState}.
     */
    private abstract class ArchivingState implements Listener<Integer> {

        /**
         * Holds the current progress or {@code -1d}. The current progress is
         * retrieved by the JavaFX thread to update the progress in the UI. A
         * new task is only submitted to the JavaFX thread if the value is
         * {@code -1d}. This avoids an unresponsive UI since the JavaFX thread
         * will not be flooded with new tasks.
         */
        private final ProgressManager _progressManager = new ProgressManager();

        /**
         * Converts percentage values to string objects. See method
         * {@link #update(org.gzipper.java.application.observer.Notifier, java.lang.Double)}.
         */
        private final PercentageStringConverter _converter = new PercentageStringConverter();

        /**
         * Used to filter files or archive entries when processing archives.
         */
        protected Predicate<String> _filterPredicate = null;

        /**
         * Set the filter to be used when processing archives.
         *
         * @param filterPredicate the filter or {@code null} to reset it.
         */
        void setFilterPredicate(Predicate<String> filterPredicate) {
            _filterPredicate = filterPredicate;
        }

        /**
         * Applies the required extension filters to the specified file chooser.
         *
         * @param chooser the {@link FileChooser} to which the extension filters
         * will be applied to.
         */
        void applyExtensionFilters(FileChooser chooser) {
            if (chooser != null) {
                final ArchiveType selectedType = _archiveTypeComboBox.getSelectionModel().getSelectedItem();
                for (ArchiveType type : ArchiveType.values()) {
                    if (type.equals(selectedType)) {
                        ExtensionFilter extFilter = new ExtensionFilter(type.getDisplayName(),
                                type.getExtensionNames(true));
                        chooser.getExtensionFilters().add(extFilter);
                    }
                }
            }
        }

        /**
         * Validates the output path specified in user control.
         *
         * @return true if output path is valid, false otherwise.
         */
        abstract boolean validateOutputPath();

        /**
         * Initializes the archiving operation.
         *
         * @param archiveType the type of the archive, see {@link ArchiveType}.
         * @return list consisting of {@link ArchiveOperation}.
         * @throws GZipperException if the archive type could not have been
         * determined.
         */
        abstract List<ArchiveOperation> initOperation(ArchiveType archiveType) throws GZipperException;

        /**
         * Performs the specified {@link ArchiveOperation}.
         *
         * @param operation the {@link ArchiveOperation} to be performed.
         */
        void performOperation(ArchiveOperation operation) {
            if (operation != null) {
                Task<Boolean> task = initArchivingJob(operation);
                ArchiveInfo info = operation.getArchiveInfo();
                Log.i(I18N.getString("operationStarted.text"), true, operation,
                        info.getArchiveType().getDisplayName());
                Log.i(I18N.getString("outputPath.text", info.getOutputPath()), true);
                _progressBar.visibleProperty().bind(task.runningProperty());
                _progressText.visibleProperty().bind(task.runningProperty());
                _activeTasks.put(task.toString(), TaskHandler.submit(task));
                toggleUIcontrols(true);
            }
        }

        @Override
        public void update(Notifier<Integer> notifier, Integer value) {
            if (value >= 100) {
                notifier.detach(this);
            } else {
                double progress = _progressManager.updateProgress(notifier.getId(), value);
                // update progress and execute on JavaFX thread if not busy
                if (_progressManager.getAndSetProgress(progress) == ProgressManager.SENTINEL) {
                    Platform.runLater(() -> {
                        double totalProgress = _progressManager
                                .getAndSetProgress(ProgressManager.SENTINEL);
                        if (totalProgress > _progressBar.getProgress()) {
                            _progressBar.setProgress(totalProgress);
                            _progressText.setText(_converter.toString(totalProgress));
                        }
                    });
                }
            }
        }
    }

    private class CompressState extends ArchivingState {

        @Override
        public boolean validateOutputPath() {
            String outputPath = _outputPathTextField.getText();
            final String extName = _archiveTypeComboBox.getValue()
                    .getDefaultExtensionName(false);

            if (FileUtils.isValidDirectory(outputPath)) {
                // user has not specified output filename
                outputPath = FileUtils.generateUniqueFilename(outputPath,
                        DEFAULT_ARCHIVE_NAME, extName);
            }
            if (FileUtils.isValidOutputFile(outputPath)) {
                _outputPathTextField.setText(outputPath);
                _archiveFileExtension = extName;
                return true;
            }
            return false;
        }

        @Override
        public void performOperation(ArchiveOperation operation) {
            if (!ListUtils.isNullOrEmpty(_selectedFiles)) {
                super.performOperation(operation);
            } else {
                Log.e("Operation cannot be started as no files have been specified.");
                Log.i(I18N.getString("noFilesSelectedWarning.text"), true);
            }
        }

        @Override
        public List<ArchiveOperation> initOperation(ArchiveType archiveType)
                throws GZipperException {
            List<ArchiveOperation> operations;
            if (_archiveTypeComboBox.getValue() == ArchiveType.GZIP) {
                // put each file into a separate archive
                operations = new ArrayList<>(_selectedFiles.size());
                List<ArchiveInfo> infos = ArchiveInfoFactory.createArchiveInfos(
                        archiveType, _archiveName, _compressionLevel,
                        _selectedFiles, _outputFile.getParent());
                for (int i = 0; i < infos.size(); ++i) {
                    final ArchiveOperation.Builder builder
                            = new ArchiveOperation.Builder(
                                    infos.get(i), CompressionMode.COMPRESS);
                    builder.addListener(this).filterPredicate(_filterPredicate);
                    operations.add(builder.build());
                }
            } else {
                operations = new ArrayList<>(1);
                ArchiveInfo info = ArchiveInfoFactory.createArchiveInfo(archiveType,
                        _archiveName, _compressionLevel, _selectedFiles, _outputFile.getParent());
                final ArchiveOperation.Builder builder
                        = new ArchiveOperation.Builder(
                                info, CompressionMode.COMPRESS);
                builder.addListener(this).filterPredicate(_filterPredicate);
                operations.add(builder.build());
            }
            return operations;
        }
    }

    private class DecompressState extends ArchivingState {

        @Override
        public boolean validateOutputPath() {
            return FileUtils.isValidDirectory(_outputPathTextField.getText());
        }

        @Override
        public void performOperation(ArchiveOperation operation) {
            if (_outputFile != null && !ListUtils.isNullOrEmpty(_selectedFiles)) {
                super.performOperation(operation);
            } else {
                Log.e("Operation cannot be started as an invalid path has been specified.");
                Log.w(I18N.getString("outputPathWarning.text"), true);
                _outputPathTextField.requestFocus();
            }
        }

        @Override
        public List<ArchiveOperation> initOperation(ArchiveType archiveType)
                throws GZipperException {
            List<ArchiveOperation> operations = new ArrayList<>(_selectedFiles.size());
            // create new operation for each archive to be extracted
            for (int i = 0; i < _selectedFiles.size(); ++i) {
                final File file = _selectedFiles.get(i);
                ArchiveInfo info = ArchiveInfoFactory.createArchiveInfo(archiveType,
                        file.getAbsolutePath(), _outputFile + File.separator);
                final ArchiveOperation.Builder builder
                        = new ArchiveOperation.Builder(
                                info, CompressionMode.DECOMPRESS);
                builder.addListener(this).filterPredicate(_filterPredicate);
                operations.add(builder.build());
            }
            return operations;
        }
    }
}
