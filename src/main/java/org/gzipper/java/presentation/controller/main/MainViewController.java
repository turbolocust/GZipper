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
package org.gzipper.java.presentation.controller.main;

import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.gzipper.java.application.ArchiveOperation;
import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.application.model.OperatingSystem;
import org.gzipper.java.application.predicates.PatternPredicate;
import org.gzipper.java.application.util.FileUtils;
import org.gzipper.java.application.util.ListUtils;
import org.gzipper.java.application.util.TaskHandler;
import org.gzipper.java.exceptions.GZipperException;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.presentation.CSS;
import org.gzipper.java.presentation.Dialogs;
import org.gzipper.java.presentation.TaskGroup;
import org.gzipper.java.presentation.controller.BaseController;
import org.gzipper.java.presentation.controller.DropViewController;
import org.gzipper.java.presentation.handler.TextAreaHandler;
import org.gzipper.java.util.Log;
import org.gzipper.java.util.Settings;

import java.io.File;
import java.net.URL;
import java.util.*;
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
    final TaskHandler taskHandler;

    /**
     * Holds the currently active tasks (or operations).
     */
    final TaskGroup activeTasks;

    public TaskGroup getActiveTasks() {
        return activeTasks;
    }

    /**
     * The currently active state.
     */
    private ArchivingState _state;

    /**
     * The output file or directory that has been selected by the user.
     */
    private File _outputFile;

    /**
     * Returns the output file, i.e. the archive or file to be created.
     *
     * @return a reference to the output file.
     */
    File getOutputFile() {
        return _outputFile;
    }

    /**
     * A list consisting of the files that have been selected by the user.
     * These can either be files to be packed or archives to be extracted.
     */
    private List<File> _selectedFiles;

    List<File> getSelectedFiles() {
        return List.copyOf(_selectedFiles);
    }

    /**
     * The archive name specified by the user.
     */
    private String _archiveName;

    String getArchiveName() {
        return _archiveName;
    }

    void setArchiveName(String archiveName) {
        _archiveName = archiveName;
    }

    /**
     * The file extension of the archive type.
     */
    private String _archiveFileExtension;

    void setArchiveFileExtension(String extension) {
        _archiveFileExtension = extension;
    }

    /**
     * The compression level. Initialized with default compression level.
     */
    private int _compressionLevel;

    int getCompressionLevel() {
        return _compressionLevel;
    }

    /**
     * True if user wishes to put each file into a separate archive.
     */
    private boolean _putFilesIntoSeparateArchives;

    boolean isPutFilesIntoSeparateArchives() {
        return _putFilesIntoSeparateArchives;
    }

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
    private MenuItem _startOperationMenuItem;
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
        _archiveName = CompressState.DEFAULT_ARCHIVE_NAME;
        _compressionLevel = Deflater.DEFAULT_COMPRESSION;
        activeTasks = new TaskGroup();
        taskHandler = new TaskHandler(TaskHandler.ExecutorType.CACHED);
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
            Log.i("{0}{1} {2}", true, msg, selectedItem.getText(), "(Deflate)");
        }
    }

    @FXML
    void handleCloseMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_closeMenuItem)) {
            activeTasks.cancelTasks();
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
    void handleStartOperationMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_startOperationMenuItem)) {
            if (!_startButton.isDisable()) {
                createAndPerformOperations();
            }
        }
    }

    @FXML
    void handleAddManyFilesMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_addManyFilesMenuItem)) {
            final DropViewController dropViewController = ViewControllers.showDropView(theme, iconImage);
            final List<String> filePaths = dropViewController.getAddresses();
            if (!ListUtils.isNullOrEmpty(filePaths)) {
                _putFilesIntoSeparateArchives = dropViewController.isPutInSeparateArchives();
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
            ViewControllers.showHashView(theme, iconImage);
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
            ViewControllers.showAboutView(theme, iconImage, hostServices);
        }
    }

    @FXML
    void handleStartButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_startButton)) {
            createAndPerformOperations();
        }
    }

    @FXML
    void handleAbortButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_abortButton)) {
            activeTasks.cancelTasks();
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
                _putFilesIntoSeparateArchives = false;
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
    void handleOutputPathTextFieldKeyTyped(KeyEvent evt) {
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

        if (_compressRadioButton.isSelected() && _putFilesIntoSeparateArchives) {
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
        _state = compress ? new CompressState(this) : new DecompressState(this);
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
        _putFilesIntoSeparateArchives = false;
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

    /**
     * Disables all relevant UI controls as long as any task is active (or running).
     * Once disabled, a UI control is unusable (or greyed-out).
     */
    void disableUIControlsAsLongAsAnyTaskIsActive() {

        if (_startButton.disableProperty().isBound()) return;
        final var runningProperty = activeTasks.anyTasksPresentProperty();

        _startButton.disableProperty().bind(runningProperty);
        _abortButton.disableProperty().bind(Bindings.not(runningProperty));
        _compressRadioButton.disableProperty().bind(runningProperty);
        _decompressRadioButton.disableProperty().bind(runningProperty);
        _archiveTypeComboBox.disableProperty().bind(runningProperty);
        _saveAsButton.disableProperty().bind(runningProperty);
        _selectFilesButton.disableProperty().bind(runningProperty);
        _addManyFilesMenuItem.disableProperty().bind(runningProperty);
        _progressBar.visibleProperty().bind(runningProperty);
        _progressText.visibleProperty().bind(runningProperty);
    }

    /**
     * Enables all relevant UI controls, i.e. makes them usable again.
     */
    void enableUIControls() {
        _startButton.disableProperty().unbind();
        _abortButton.disableProperty().unbind();
        _compressRadioButton.disableProperty().unbind();
        _decompressRadioButton.disableProperty().unbind();
        _archiveTypeComboBox.disableProperty().unbind();
        _selectFilesButton.disableProperty().unbind();
        _saveAsButton.disableProperty().unbind();
        _addManyFilesMenuItem.disableProperty().unbind();
        _progressBar.visibleProperty().unbind();
        _progressText.visibleProperty().unbind();
    }

    /**
     * Returns archive type, which is currently selected by the user.
     *
     * @return the currently selected archive type.
     */
    ArchiveType getSelectedArchiveType() {
        return _archiveTypeComboBox.getSelectionModel().getSelectedItem();
    }

    /**
     * Returns the text currently set in the text field which specifies the output path.
     *
     * @return the text currently set in the text field which specifies the output path.
     */
    String getTextOfOutputPathTextField() {
        return _outputPathTextField.getText();
    }

    /**
     * Sets the specified {@code text} in the text field which specifies the output path.
     *
     * @param text the text to be set in the text field which specifies the output path.
     */
    void setTextOfOutputPathTextField(String text) {
        _outputPathTextField.setText(text);
    }

    /**
     * Returns the currently set progress of the progress bar.
     *
     * @return the currently set progress of the progress bar.
     */
    double getProgressOfProgressBar() {
        return _progressBar.getProgress();
    }

    /**
     * Sets the current progress of the progress bar (from 0 to 1).
     *
     * @param value the progress of the progress bar to be set.
     */
    void setProgressInProgressBar(double value) {
        _progressBar.setProgress(value);
    }

    /**
     * Requests the focus on the text field which specifies the output path.
     */
    void requestFocusOnOutputPathTextField() {
        _outputPathTextField.requestFocus();
    }

    /**
     * Resets the progress of the progress bar.
     */
    void resetProgressBar() {
        setProgressInProgressBar(0d);
    }

    /**
     * Sets the text visible in the progress bar.
     *
     * @param text the text visible in the progress bar.
     */
    void setTextInProgressBar(String text) {
        _progressText.setText(text);
    }

    //</editor-fold>

    private void createAndPerformOperations() {
        try {
            if (_state.checkUpdateOutputPath()) {
                final String outputPathText = _outputPathTextField.getText();
                final File outputFile = new File(outputPathText);
                final String outputPath = FileUtils.getPath(outputFile);

                checkUpdateOutputFileAndArchiveName(outputPath);

                final String recentPath = FileUtils.getParent(outputPath);
                Settings.getInstance().setProperty("recentPath", recentPath);
                final var archiveType = _archiveTypeComboBox.getValue();
                final var operations = _state.initOperation(archiveType);

                for (var operation : operations) {
                    Log.i("Starting operation using the following archive info: {0}",
                            operation.getArchiveInfo().toString(), false);
                }

                _state.performOperations(operations.toArray(new ArchiveOperation[0]));
            } else {
                Log.w(I18N.getString("invalidOutputPath.text"), true);
            }
        } catch (GZipperException ex) {
            Log.e(ex.getLocalizedMessage(), ex);
        }
    }

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

        _state = new CompressState(this); // the default one
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
}
