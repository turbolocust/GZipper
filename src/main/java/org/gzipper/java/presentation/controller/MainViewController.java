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
package org.gzipper.java.presentation.controller;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.converter.PercentageStringConverter;
import org.gzipper.java.application.ArchiveInfo;
import org.gzipper.java.application.ArchiveInfoFactory;
import org.gzipper.java.application.ArchiveOperation;
import org.gzipper.java.application.CompressionMode;
import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.application.model.OperatingSystem;
import org.gzipper.java.application.observer.Listener;
import org.gzipper.java.application.observer.Notifier;
import org.gzipper.java.application.predicates.PatternPredicate;
import org.gzipper.java.application.util.*;
import org.gzipper.java.exceptions.GZipperException;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.presentation.CSS;
import org.gzipper.java.presentation.Dialogs;
import org.gzipper.java.presentation.ProgressManager;
import org.gzipper.java.presentation.handler.TextAreaHandler;
import org.gzipper.java.util.Log;
import org.gzipper.java.util.Settings;

import java.io.File;
import java.net.URL;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;
import java.util.zip.Deflater;

/**
 * Controller for the FXML named "MainView.fxml".
 *
 * @author Matthias Fussenegger
 */
public final class MainViewController extends BaseController {

    //<editor-fold desc="Attributes">

    /**
     * Key constant used to access the properties map for menu items.
     */
    private static final String COMPRESSION_LEVEL_KEY = "compressionLevel";

    /**
     * Logger for UI named {@code MainViewController.class.getName()}.
     */
    public static final Logger LOGGER = Logger.getLogger(MainViewController.class.getName());

    /**
     * Handler used to execute tasks.
     */
    private final TaskHandler _taskHandler;

    /**
     * Map consisting of {@link Future} objects representing the currently
     * active tasks.
     */
    private final ConcurrentMap<Integer, Future<?>> _activeTasks;

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

    /**
     * True if user wishes to put each file into a separate archive.
     */
    private boolean _putIntoSeparateArchives;

    //</editor-fold>

    //<editor-fold desc="FXML attributes">

    @FXML
    private MenuItem _applyFilterMenuItem;
    @FXML
    private MenuItem _resetFilterMenuItem;
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
    private MenuItem _addManyFilesMenuItem;
    @FXML
    private MenuItem _hashingMenuItem;
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

    //</editor-fold>

    /**
     * Constructs a controller for Main View with the specified CSS theme and
     * host services.
     *
     * @param theme        the {@link CSS} theme to be applied.
     * @param hostServices the host services to aggregate.
     */
    public MainViewController(CSS.Theme theme, HostServices hostServices) {
        super(theme, hostServices);
        _archiveName = DEFAULT_ARCHIVE_NAME;
        _compressionLevel = Deflater.DEFAULT_COMPRESSION;
        _activeTasks = new ConcurrentHashMap<>();
        _taskHandler = new TaskHandler(TaskHandler.ExecutorType.CACHED);
        Log.i("Default archive name set to: {0}", _archiveName, false);
    }

    //<editor-fold desc="FXML methods">

    @FXML
    void handleApplyFilterMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_applyFilterMenuItem)) {
            final Optional<String> result = Dialogs.showPatternInputDialog(theme, getIconImage());
            if (result.isPresent()) {
                if (!result.get().isEmpty()) {
                    final Pattern pattern = Pattern.compile(result.get());
                    _state.setFilterPredicate(new PatternPredicate(pattern));
                    Log.i(I18N.getString("filterApplied.text", result.get()), true);
                } else {
                    resetFilter();
                }
            }
        }
    }

    @FXML
    void handleResetFilterMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_resetFilterMenuItem)) {
            resetFilter();
        }
    }

    @FXML
    void handleCompressionLevelMenuItemAction(ActionEvent evt) {
        final MenuItem selectedItem = (MenuItem) evt.getSource();
        final Object compressionStrength = selectedItem.getProperties().get(COMPRESSION_LEVEL_KEY);
        if (compressionStrength != null) {
            _compressionLevel = (int) compressionStrength;
            final String msg = I18N.getString("compressionLevelChange.text") + " ";
            Log.i("{0}{1}", true, msg, selectedItem.getText());
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
                            I18N.getString("confirmation.text"), theme, getIconImage());

            if (result.isPresent() && result.get() == ButtonType.YES) {
                _textArea.clear();
                _textArea.setText("run:\n");
            }
        }
    }

    @FXML
    void handleAddManyFilesMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_addManyFilesMenuItem)) {
            final DropViewController dropViewController = ViewControllers.showDropView(theme);
            final List<String> filePaths = dropViewController.getAddresses();
            if (!ListUtils.isNullOrEmpty(filePaths)) {
                _putIntoSeparateArchives = dropViewController.isPutInSeparateArchives();
                final int size = filePaths.size();
                _selectedFiles = new ArrayList<>(size);
                _startButton.setDisable(false);
                if (size > 10) { // threshold, to avoid flooding text area
                    filePaths.forEach((filePath) -> _selectedFiles.add(new File(filePath)));
                    Log.i(I18N.getString("manyFilesSelected.text"), true, size);
                } else { // log files in detail
                    filePaths.stream()
                            .peek((filePath) -> _selectedFiles.add(new File(filePath)))
                            .forEachOrdered((filePath) -> Log.i("{0}: {1}",
                                    true, I18N.getString("fileSelected.text"), filePath));
                }
            } else {
                Log.i(I18N.getString("noFilesSelected.text"), true);
                _startButton.setDisable(ListUtils.isNullOrEmpty(_selectedFiles));
            }
        }
    }

    @FXML
    void handleHashingMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_hashingMenuItem)) {
            ViewControllers.showHashView(theme);
        }
    }

    @FXML
    void handleResetAppMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_resetAppMenuItem)) {
            final Optional<ButtonType> result = Dialogs
                    .showConfirmationDialog(I18N.getString("resetApp.text"),
                            I18N.getString("resetAppConfirmation.text"),
                            I18N.getString("confirmation.text"), theme, getIconImage());
            if (result.isPresent() && result.get() == ButtonType.YES) {
                Settings.getInstance().restoreDefaults();
            }
        }
    }

    @FXML
    void handleAboutMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_aboutMenuItem)) {
            ViewControllers.showAboutView(theme, hostServices);
        }
    }

    @FXML
    void handleStartButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_startButton)) {
            try {
                if (_state.checkUpdateOutputPath()) {
                    final String outputPathText = _outputPathTextField.getText();
                    final File outputFile = new File(outputPathText);
                    final String outputPath = FileUtils.getPath(outputFile);

                    checkUpdateOutputFileAndArchiveName(outputPath);

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

            final List<File> selectedFiles = fc.showOpenMultipleDialog(primaryStage);

            String message;
            if (selectedFiles != null) {
                _putIntoSeparateArchives = false;
                _startButton.setDisable(false);
                int size = selectedFiles.size();
                message = I18N.getString("filesSelected.text", size);
                if (size <= 10) {
                    selectedFiles.forEach((file) -> { // log the path of each selected file
                        Log.i("{0}: \"{1}\"", true,
                                I18N.getString("fileSelected.text"),
                                FileUtils.getPath(file));
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
            final File file = pickFileToBeSaved();
            if (file != null) {
                updateSelectedFile(file);
                String path = FileUtils.getPath(file);
                if (!file.isDirectory() && FileUtils.getExtension(path).isEmpty()) {
                    path += _archiveFileExtension;
                }
                setOutputPath(path);
                Log.i("Output file set to: {0}", FileUtils.getPath(file), false);
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
        resetSelectedFiles();
    }

    @FXML
    void handleArchiveTypeComboBoxAction(ActionEvent evt) {
        if (evt.getSource().equals(_archiveTypeComboBox)) {
            var archiveType = _archiveTypeComboBox.getValue();
            Log.i("Archive type selection change to: {0}", archiveType, false);
            if (archiveType == ArchiveType.GZIP) {
                performGzipSelectionAction();
            }
            if (_decompressRadioButton.isSelected()) {
                resetSelectedFiles();
            } else { // update file extension
                final String outputPathText = _outputPathTextField.getText();
                final String fileExtension = archiveType.getDefaultExtensionName();
                String outputPath;
                if (outputPathText.endsWith(_archiveFileExtension)) {
                    int lastIndexOfExtension = outputPathText.lastIndexOf(_archiveFileExtension);
                    outputPath = outputPathText.substring(0, lastIndexOfExtension) + fileExtension;
                } else if (!FileUtils.isValidDirectory(outputPathText)) {
                    outputPath = outputPathText + fileExtension;
                } else {
                    outputPath = outputPathText;
                }
                setOutputPath(outputPath);
                _archiveFileExtension = fileExtension;
            }
        }
    }

    @FXML
    void onOutputPathTextFieldKeyTyped(KeyEvent evt) {
        if (evt.getSource().equals(_outputPathTextField)) {
            String filename = _outputPathTextField.getText();
            if (!FileUtils.containsIllegalChars(filename)) {
                updateSelectedFile(new File(filename));
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
            loadAlternativeTheme(enableTheme);
            Settings.getInstance().setProperty("darkThemeEnabled", enableTheme);
        }
    }

    //</editor-fold>

    //<editor-fold desc="Methods related to UI">

    private void checkUpdateOutputFileAndArchiveName(String outputPath) {
        if (!FileUtils.getPath(_outputFile).equals(outputPath)) {
            _outputFile = new File(outputPath);
            if (!_outputFile.isDirectory()) {
                _archiveName = _outputFile.getName();
            }
        }
    }

    private File pickFileToBeSaved() {
        final File file;

        if (_compressRadioButton.isSelected() && _putIntoSeparateArchives) {
            var directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(I18N.getString("saveAsArchiveTitle.text"));
            file = directoryChooser.showDialog(primaryStage);
        } else if (_compressRadioButton.isSelected()) {
            var fileChooser = new FileChooser();
            fileChooser.setTitle(I18N.getString("saveAsArchiveTitle.text"));
            _state.applyExtensionFilters(fileChooser);
            file = fileChooser.showSaveDialog(primaryStage);
        } else {
            var directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(I18N.getString("saveAsPathTitle.text"));
            file = directoryChooser.showDialog(primaryStage);
        }

        return file;
    }

    private void performModeRadioButtonAction(boolean compress, String selectFilesButtonText, String saveAsButtonText) {
        _state = compress ? new CompressState() : new DecompressState();
        _selectFilesButton.setText(I18N.getString(selectFilesButtonText));
        _saveAsButton.setText(I18N.getString(saveAsButtonText));
    }

    private void performGzipSelectionAction() {
        final Settings settings = Settings.getInstance();
        final String propertyKey = "showGzipInfoDialog";
        final boolean showDialog = settings.evaluateProperty(propertyKey);
        if (showDialog) {
            final String infoTitle = I18N.getString("info.text");
            final String infoText = I18N.getString("gzipCompressionInfo.text", ArchiveType.TAR_GZ.getDisplayName());
            Dialogs.showDialog(Alert.AlertType.INFORMATION, infoTitle, infoTitle, infoText, theme, getIconImage());
            settings.setProperty(propertyKey, false);
        }
    }

    private void bindUIControls(Task<?> task) {

        final ReadOnlyBooleanProperty running = task.runningProperty();

        // controls
        _startButton.disableProperty().bind(running);
        _abortButton.disableProperty().bind(Bindings.not(running));
        _compressRadioButton.disableProperty().bind(running);
        _decompressRadioButton.disableProperty().bind(running);
        _archiveTypeComboBox.disableProperty().bind(running);
        _saveAsButton.disableProperty().bind(running);
        _selectFilesButton.disableProperty().bind(running);
        _addManyFilesMenuItem.disableProperty().bind(running);
        // progress bar
        _progressBar.visibleProperty().bind(running);
        _progressText.visibleProperty().bind(running);
    }

    private void unbindUIControls() {
        // controls
        _startButton.disableProperty().unbind();
        _abortButton.disableProperty().unbind();
        _compressRadioButton.disableProperty().unbind();
        _decompressRadioButton.disableProperty().unbind();
        _archiveTypeComboBox.disableProperty().unbind();
        _selectFilesButton.disableProperty().unbind();
        _saveAsButton.disableProperty().unbind();
        _addManyFilesMenuItem.disableProperty().unbind();
        // progress bar
        _progressBar.visibleProperty().unbind();
        _progressText.visibleProperty().unbind();
    }

    private void resetFilter() {
        final boolean wasApplied = _state.getFilterPredicate() != null;
        _state.setFilterPredicate(null);
        if (wasApplied) {
            Log.i(I18N.getString("filterReset.text"), true);
        }
    }

    private void resetSelectedFiles() {
        if (!ListUtils.isNullOrEmpty(_selectedFiles)) {
            Log.i(I18N.getString("selectionReset.text"), true);
        }
        _putIntoSeparateArchives = false;
        _selectedFiles = Collections.emptyList();
        _startButton.setDisable(true);
    }

    private void setOutputPath(String outputPath) {
        String osStyleFilePath = outputPath.replace('/', File.separatorChar);
        _outputPathTextField.setText(osStyleFilePath);
    }

    private void updateSelectedFile(File file) {
        if (file != null) {
            if (!file.isDirectory()) {
                final String archiveName = _archiveName = file.getName();
                String fileExtension = FileUtils.getExtension(archiveName, true);
                if (fileExtension.isEmpty()) {
                    fileExtension = _archiveTypeComboBox.getValue().getDefaultExtensionName();
                }
                _archiveFileExtension = fileExtension;
            }
            _outputFile = file;
        }
    }

    //</editor-fold>

    //<editor-fold desc="Methods related to archiving job">

    /**
     * Cancels all currently active tasks.
     */
    public void cancelActiveTasks() {
        if (!MapUtils.isNullOrEmpty(_activeTasks)) {
            _activeTasks.keySet().stream().map(_activeTasks::get)
                    .filter((task) -> (!task.cancel(true))).forEachOrdered((task) -> {
                        // log error message only when cancellation failed
                        Log.e("Task cancellation failed for {0}", task.hashCode());
                    });
        }
    }

    /**
     * Initializes the archiving job by creating the required {@link Task}. This
     * task will not perform the algorithmic operations for archiving but instead
     * constantly checks for interruption to properly detect the abortion of an
     * operation. For the algorithmic operations a new task will be created and
     * submitted to the task handler. If an operation has been aborted, e.g.
     * through user interaction, the operation will be interrupted.
     *
     * @param operation the {@link ArchiveOperation} that will eventually be
     *                  performed by the task when executed.
     * @return a {@link Task} that can be executed to perform the specified archiving operation.
     */
    @SuppressWarnings("SleepWhileInLoop")
    private Task<Boolean> initArchivingJob(final ArchiveOperation operation) {
        Task<Boolean> task = new Task<>() {
            @SuppressWarnings("BusyWait")
            @Override
            protected Boolean call() throws Exception {
                final Future<Boolean> futureTask = _taskHandler.submit(operation);
                while (!futureTask.isDone()) {
                    try {
                        Thread.sleep(10); // continuous check for interruption
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
                    return false; // ignore exception
                }
            }
        };

        showSuccessMessageAndFinalizeArchivingJob(operation, task);
        showErrorMessageAndFinalizeArchivingJob(operation, task);

        return task;
    }

    private void showErrorMessageAndFinalizeArchivingJob(ArchiveOperation operation, Task<Boolean> task) {
        task.setOnFailed(e -> {
            Log.i(I18N.getString("operationFail.text"), true, operation);
            final Throwable thrown = e.getSource().getException();
            if (thrown != null) Log.e(thrown.getLocalizedMessage(), thrown);
            finishArchivingJob(operation, task);
        });
    }

    private void showSuccessMessageAndFinalizeArchivingJob(ArchiveOperation operation, Task<Boolean> task) {
        task.setOnSucceeded(e -> {
            final boolean success = (boolean) e.getSource().getValue();
            if (success) {
                Log.i(I18N.getString("operationSuccess.text"), true, operation);
            } else {
                Log.w(I18N.getString("operationNoSuccess.text"), true, operation);
            }
            finishArchivingJob(operation, task);
        });
    }

    /**
     * Calculates the total duration in seconds of the specified
     * {@link ArchiveOperation} and logs it to {@link #_textArea}. Also toggles
     * {@link #_startButton} and {@link #_abortButton}.
     *
     * @param operation {@link ArchiveOperation} that holds elapsed time.
     * @param task      the task that will be removed from {@link #_activeTasks}.
     */
    private void finishArchivingJob(ArchiveOperation operation, Task<?> task) {
        Log.i(I18N.getString("elapsedTime.text"), true, operation.calculateElapsedTime());
        _activeTasks.remove(task.hashCode());
        if (_activeTasks.isEmpty()) {
            unbindUIControls();
            _state.refresh();
            _progressBar.setProgress(0d); // reset
            _progressText.setText(StringUtils.EMPTY);
        }
    }

    //</editor-fold>

    //<editor-fold desc="Initialization">

    private void initLogger() {
        TextAreaHandler handler = new TextAreaHandler(_textArea);
        handler.setFormatter(new SimpleFormatter());
        Log.setLoggerForUI(LOGGER.getName());
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initLogger();

        final Settings settings = Settings.getInstance();
        setRecentlyUsedPathInOutputPathTextField(settings);

        if (theme == CSS.Theme.DARK_THEME) {
            _enableDarkThemeCheckMenuItem.setSelected(true);
        }

        setUpPropertiesForCompressionLevelMenuItem();
        setUpArchiveTypesComboBox();

        final boolean enableLogging = settings.evaluateProperty("loggingEnabled");
        _enableLoggingCheckMenuItem.setSelected(enableLogging);

        _state = new CompressState(); // the default one
        final String formattedText = String.format("run:\n%s\n", I18N.getString("changeOutputPath.text"));
        _textArea.setText(formattedText);
    }

    private void setRecentlyUsedPathInOutputPathTextField(Settings settings) {
        final OperatingSystem os = settings.getOs();
        final String recentPath = settings.getProperty("recentPath");

        if (FileUtils.isValidDirectory(recentPath)) {
            setOutputPath(recentPath);
        } else {
            setOutputPath(os.getDefaultUserDirectory());
        }

        _outputFile = new File(_outputPathTextField.getText());
    }

    private void setUpArchiveTypesComboBox() {
        final ArchiveType selectedType = ArchiveType.TAR_GZ;
        _archiveTypeComboBox.getItems().addAll(ArchiveType.values());
        _archiveTypeComboBox.setValue(selectedType);
        _archiveFileExtension = selectedType.getDefaultExtensionName();
    }

    private void setUpPropertiesForCompressionLevelMenuItem() {
        _noCompressionMenuItem.getProperties().put(COMPRESSION_LEVEL_KEY, Deflater.NO_COMPRESSION);
        _bestSpeedCompressionMenuItem.getProperties().put(COMPRESSION_LEVEL_KEY, Deflater.BEST_SPEED);
        _defaultCompressionMenuItem.getProperties().put(COMPRESSION_LEVEL_KEY, Deflater.DEFAULT_COMPRESSION);
        _bestCompressionMenuItem.getProperties().put(COMPRESSION_LEVEL_KEY, Deflater.BEST_COMPRESSION);
    }

    //</editor-fold>

    //<editor-fold desc="Controller states">

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
         * {@link #update(org.gzipper.java.application.observer.Notifier, java.lang.Integer)}.
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
        final void setFilterPredicate(Predicate<String> filterPredicate) {
            _filterPredicate = filterPredicate;
        }

        /**
         * Returns the filter to be used when processing archives.
         *
         * @return the filter to be used when processing archives. If no filter
         * is set, this method will return {@code null}.
         */
        final Predicate<String> getFilterPredicate() {
            return _filterPredicate;
        }

        /**
         * Validates the output path specified in user control.
         *
         * @return true if output path is valid, false otherwise.
         */
        abstract boolean checkUpdateOutputPath();

        /**
         * Initializes the archiving operation.
         *
         * @param archiveType the type of the archive, see {@link ArchiveType}.
         * @return list consisting of {@link ArchiveOperation}.
         * @throws GZipperException if the archive type could not have been
         *                          determined.
         */
        abstract List<ArchiveOperation> initOperation(ArchiveType archiveType) throws GZipperException;

        /**
         * Applies the required extension filters to the specified file chooser.
         *
         * @param chooser the {@link FileChooser} to which the extension filters
         *                will be applied to.
         */
        void applyExtensionFilters(FileChooser chooser) {
            if (chooser != null) {
                final ArchiveType selectedType = _archiveTypeComboBox.getSelectionModel().getSelectedItem();
                for (ArchiveType type : ArchiveType.values()) {
                    if (type.equals(selectedType)) {
                        ExtensionFilter extFilter = new ExtensionFilter(
                                type.getDisplayName(), type.getExtensionNames(true));
                        chooser.getExtensionFilters().add(extFilter);
                    }
                }
            }
        }

        /**
         * Refreshes this state, e.g. clears the state of the progress manager.
         */
        void refresh() {
            _progressManager.reset();
        }

        /**
         * Performs the specified {@link ArchiveOperation}.
         *
         * @param operation the {@link ArchiveOperation} to be performed.
         */
        void performOperation(ArchiveOperation operation) {
            if (operation != null) {
                Task<Boolean> task = initArchivingJob(operation);
                final ArchiveInfo info = operation.getArchiveInfo();
                Log.i(I18N.getString("operationStarted.text"), true, operation,
                        info.getArchiveType().getDisplayName());
                Log.i(I18N.getString("outputPath.text", info.getOutputPath()), true);
                bindUIControls(task); // do this before submitting task
                _activeTasks.put(task.hashCode(), _taskHandler.submit(task));
            }
        }

        @Override
        public final void update(Notifier<Integer> notifier, Integer value) {
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

    private final class CompressState extends ArchivingState {

        private String determineOutputPath(File outputFile) {
            if (!outputFile.exists() || outputFile.isFile()) {
                return outputFile.getParent();
            }

            return FileUtils.getPath(outputFile);
        }

        @Override
        public boolean checkUpdateOutputPath() {
            String outputPath = _outputPathTextField.getText();
            String extName = _archiveTypeComboBox.getValue().getDefaultExtensionName();

            if (FileUtils.isValidDirectory(outputPath) && !_putIntoSeparateArchives) {

                String archiveName = DEFAULT_ARCHIVE_NAME;

                if (_selectedFiles.size() == 1) {
                    File firstFile = _selectedFiles.get(0);
                    archiveName = firstFile.getName();
                }
                // user has not specified output filename
                outputPath = FileUtils.generateUniqueFilename(outputPath, archiveName, extName);
            }

            _archiveFileExtension = extName;

            if (FileUtils.isValidOutputFile(outputPath)) {
                setOutputPath(outputPath);
                return true;
            }

            return false;
        }

        @Override
        public void performOperation(ArchiveOperation operation) {
            if (!ListUtils.isNullOrEmpty(_selectedFiles)) {
                super.performOperation(operation);
            } else {
                Log.e("Operation cannot be started as no files have been specified");
                Log.i(I18N.getString("noFilesSelectedWarning.text"), true);
            }
        }

        @Override
        public List<ArchiveOperation> initOperation(ArchiveType archiveType) throws GZipperException {

            List<ArchiveOperation> operations;

            if (_archiveTypeComboBox.getValue() == ArchiveType.GZIP || _putIntoSeparateArchives) {

                final List<ArchiveInfo> infos;
                final String outputPath = determineOutputPath(_outputFile);
                operations = new ArrayList<>(_selectedFiles.size());

                if (_putIntoSeparateArchives) {
                    infos = ArchiveInfoFactory.createArchiveInfos(
                            archiveType, _compressionLevel, _selectedFiles, outputPath);
                } else if (_selectedFiles.size() == 1) {
                    var info = ArchiveInfoFactory.createArchiveInfo(archiveType,
                            _archiveName, _compressionLevel, _selectedFiles, _outputFile.getParent());
                    infos = new ArrayList<>(1);
                    infos.add(info);
                } else {
                    infos = ArchiveInfoFactory.createArchiveInfos(archiveType,
                            _archiveName, _compressionLevel, _selectedFiles, outputPath);
                }

                for (ArchiveInfo info : infos) {
                    var builder = new ArchiveOperation.Builder(info, CompressionMode.COMPRESS);
                    builder.addListener(this).filterPredicate(_filterPredicate);
                    operations.add(builder.build());
                }
            } else {
                operations = new ArrayList<>(1);
                var info = ArchiveInfoFactory.createArchiveInfo(archiveType, _archiveName,
                        _compressionLevel, _selectedFiles, _outputFile.getParent());
                _archiveName = info.getArchiveName();
                setOutputPath(FileUtils.combine(info.getOutputPath(), _archiveName));
                var builder = new ArchiveOperation.Builder(info, CompressionMode.COMPRESS);
                builder.addListener(this).filterPredicate(_filterPredicate);
                operations.add(builder.build());
            }

            return operations;
        }
    }

    private final class DecompressState extends ArchivingState {

        @Override
        public boolean checkUpdateOutputPath() {
            return FileUtils.isValidDirectory(_outputPathTextField.getText());
        }

        @Override
        public void performOperation(ArchiveOperation operation) {
            if (_outputFile != null && !ListUtils.isNullOrEmpty(_selectedFiles)) {
                super.performOperation(operation);
            } else {
                Log.e("Operation cannot be started because an invalid path has been specified");
                Log.w(I18N.getString("outputPathWarning.text"), true);
                _outputPathTextField.requestFocus();
            }
        }

        @Override
        public List<ArchiveOperation> initOperation(ArchiveType archiveType) throws GZipperException {

            List<ArchiveOperation> operations = new ArrayList<>(_selectedFiles.size());

            for (File file : _selectedFiles) {
                var info = ArchiveInfoFactory.createArchiveInfo(archiveType, FileUtils.getPath(file),
                        FileUtils.getPath(_outputFile) + File.separator);
                var builder = new ArchiveOperation.Builder(info, CompressionMode.DECOMPRESS);
                builder.addListener(this).filterPredicate(_filterPredicate);
                operations.add(builder.build());
            }

            return operations;
        }
    }

    //</editor-fold>
}
