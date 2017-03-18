/*
 * Copyright (C) 2016 Matthias Fussenegger
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import javafx.concurrent.Task;

import org.gzipper.java.application.model.OperatingSystem;
import org.gzipper.java.application.model.Unix;
import org.gzipper.java.application.model.Windows;
import org.gzipper.java.application.util.AppUtil;
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
import org.gzipper.java.presentation.model.ArchivingOperation;
import org.gzipper.java.presentation.util.ArchiveInfoFactory;

/**
 *
 * @author Matthias Fussenegger
 */
public class MainViewController extends BaseController {

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
     * The currently active task. Multiple tasks may be supported in the future.
     */
    private Task<Boolean> _activeTask;

    /**
     * The file or directory that has been selected by the user.
     */
    private File _selectedFile;

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
    private RadioButton _compressRadioButton;

    @FXML
    private RadioButton _decompressRadioButton;

    @FXML
    private TextArea _textArea;

    @FXML
    private CheckMenuItem _enableLoggingCheckMenuItem;

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

    public MainViewController() {
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
            appendToTextArea(_resources.getString(
                    "compressionLevelChange.text") + selectedItem.getText());
        }
    }

    @FXML
    void handleCloseMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_closeMenuItem)) {
            _primaryStage.close();
            System.exit(0);
        }
    }

    @FXML
    void handleDeleteMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_deleteMenuItem)) {
            Optional<ButtonType> result = AlertDialog.showConfirmationDialog(
                    _resources.getString("clearTextWarning.text"),
                    _resources.getString("confirmation.text"));
            if (result.isPresent() && result.get() == ButtonType.YES) {
                _textArea.clear();
                _textArea.setText("run:\n");
            }
        }
    }

    @FXML
    void handleStartButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_startButton)) {
            try {
                String archiveType = _archiveTypeComboBox.getValue().getName();
                ArchivingOperation operation = _strategy.initOperation(archiveType);
                _strategy.performOperation(operation);
            } catch (GZipperException ex) {
                Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
                appendToTextArea(ex.getMessage());
            }
        }
    }

    @FXML
    void handleAbortButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_abortButton)) {
            _activeTask.cancel(true);
        }
    }

    @FXML
    void handleSelectFilesButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_selectFilesButton)) {

            FileChooser fc = new FileChooser();
            if (_compressRadioButton.isSelected()) {
                fc.setTitle(_resources.getString("browseForFiles.text"));
            } else {
                fc.setTitle(_resources.getString("browseForArchive.text"));
                _strategy.applyExtensionFilters(fc);
            }

            _selectedFiles = fc.showOpenMultipleDialog(_primaryStage);

            String message;
            if (_selectedFiles != null) {
                _startButton.setDisable(false);
                final int selectedFiles = _selectedFiles.size();
                message = _resources.getString("filesSelected.text");
                message = message.replace("{0}", Integer.toString(selectedFiles));
                Logger.getLogger(GZipper.class.getName()).log(Level.INFO,
                        "A total of {0} file(s) have been selected.", selectedFiles);
            } else {
                _startButton.setDisable(true);
                message = _resources.getString("noFilesSelected.text");
                Logger.getLogger(GZipper.class.getName()).log(
                        Level.INFO, "No files have been selected.");
            }

            appendToTextArea(message);
        }
    }

    @FXML
    void handleSaveAsButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_saveAsButton)) {

            File file;
            if (_compressRadioButton.isSelected()) {
                FileChooser fc = new FileChooser();
                fc.setTitle(_resources.getString("saveAsArchiveTitle.text"));
                _strategy.applyExtensionFilters(fc);
                file = fc.showSaveDialog(_primaryStage);
            } else {
                DirectoryChooser dc = new DirectoryChooser();
                dc.setTitle(_resources.getString("saveAsPathTitle.text"));
                file = dc.showDialog(_primaryStage);
            }

            if (file != null) {
                if (!file.isDirectory()) {
                    _archiveName = file.getName();
                }
                _outputPathTextField.setText(file.getAbsolutePath());
                _selectedFile = file;
            }
        }
    }

    @FXML
    void handleCompressRadioButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_compressRadioButton)) {
            _strategy = new CompressStrategy();
            _selectFilesButton.setText(_resources.getString("browseForFiles.text"));
            _saveAsButton.setText(_resources.getString("saveAsArchive.text"));
        }
    }

    @FXML
    void handleDecompressRadioButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_decompressRadioButton)) {
            _strategy = new DecompressStrategy();
            _selectFilesButton.setText(_resources.getString("browseForArchive.text"));
            _saveAsButton.setText(_resources.getString("saveAsFiles.text"));
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
     * Appends text to the text area and inserts a new line after it.
     *
     * @param text The text to be appended.
     */
    private void appendToTextArea(String text) {
        _textArea.appendText(text + "\n");
    }

    private Task<Boolean> initArchivingJob(ArchivingOperation operation) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return operation.performOperation();
            }
        };

        task.setOnSucceeded(e -> {
            boolean success = (boolean) e.getSource().getValue();
            if (success) {
                appendToTextArea(_resources.getString("operationSuccess.text"));
            } else {
                appendToTextArea(_resources.getString("operationFail.text"));
            }
            finalizeArchivingJob(operation);
        });
        task.setOnCancelled(e -> {
            appendToTextArea(_resources.getString("operationCancel.text"));
            finalizeArchivingJob(operation);
            e.consume();
        });
        task.setOnFailed(e -> {
            appendToTextArea(_resources.getString("operationFail.text"));
            appendToTextArea(e.getSource().getException().getMessage());
            finalizeArchivingJob(operation);
        });

        return task;
    }

    /**
     * Calculates the total duration in seconds of the specified
     * {@link ArchivingOperation} and appends it via
     * {@link #appendToTextArea(java.lang.String)}. Also toggles
     * {@link #_startButton} and {@link #_abortButton}.
     *
     * @param operation {@link ArchivingOperation} that holds elapsed time.
     */
    private void finalizeArchivingJob(ArchivingOperation operation) {
        appendToTextArea(_resources.getString("elapsedTime.text")
                + operation.calculateElapsedTime() + " seconds.");
        toggleStartAndAbortButton();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {

            final String decPath = AppUtil.getDecodedRootPath(GZipper.class);
            OperatingSystem os; // to determine the users operating system

            String settingsFile;
            try { // locate settings file
                settingsFile = AppUtil.getResource(GZipper.class, "/settings.properties");
            } catch (URISyntaxException ex) {
                Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
                settingsFile = decPath + "settings.properties";
            }

            // set operating system and instantiate settings file
            os = System.getProperty("os.name").startsWith("Windows")
                    ? new Windows()
                    : new Unix();

            _settings = new Settings(settingsFile, os);

            // set recently used path from settings if valid
            String recentPath = _settings.getProperty("recentPath");
            if (FileUtil.isValidDirectory(recentPath)) {
                _outputPathTextField.setText(recentPath);
            } else {
                _outputPathTextField.setText(os.getDefaultUserDirectory());
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

        } catch (IOException ex) {
            Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
        }

        _resources = resources;
        _strategy = new CompressStrategy();
        _frameImage = new Image("/images/icon_32.png");

        _textArea.setText("run:\n" + _resources.getString("changeOutputPath.text") + "\n");
    }

    private abstract class ArchivingStrategy {

        public abstract boolean validateOutputPath();

        public abstract void applyExtensionFilters(FileChooser chooser);

        public abstract ArchivingOperation initOperation(
                String archiveType) throws GZipperException;

        public void performOperation(ArchivingOperation operation) {
            if (operation != null) {
                _activeTask = initArchivingJob(operation);
                toggleStartAndAbortButton();
                TaskHandler.getInstance().execute(_activeTask);
            }
        }

    }

    private class CompressStrategy extends ArchivingStrategy {

        @Override
        public boolean validateOutputPath() {
            return FileUtil.isValidFileName(_outputPathTextField.getText());
        }

        @Override
        public void performOperation(ArchivingOperation operation) {
            if (_selectedFiles != null) {
                super.performOperation(operation);
            } else {
                Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE,
                        "Operation cannot be started as no files have been specified.");
                appendToTextArea(_resources.getString("noFilesSelectedWarning.text"));
            }
        }

        @Override
        public ArchivingOperation initOperation(String archiveType) throws GZipperException {
            ArchiveInfo info = ArchiveInfoFactory.createArchiveInfo(
                    archiveType, _archiveName, _compressionLevel,
                    _selectedFiles, _selectedFile.getParent());
            return new ArchivingOperation(info, true);
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

    private class DecompressStrategy extends ArchivingStrategy {

        @Override
        public boolean validateOutputPath() {
            return FileUtil.isValidDirectory(_outputPathTextField.getText());
        }

        @Override
        public void performOperation(ArchivingOperation operation) {
            if (_selectedFile != null && _selectedFile.isDirectory()) {
                super.performOperation(operation);
            } else {
                Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE,
                        "Operation cannot be started as an invalid path has been specified.");
                appendToTextArea(_resources.getString("outputPathWarning.text"));
                _outputPathTextField.requestFocus();
            }
        }

        @Override
        public ArchivingOperation initOperation(String archiveType) throws GZipperException {
            ArchiveInfo info = ArchiveInfoFactory.createArchiveInfo(
                    archiveType, _archiveName, _selectedFile.getParent());
            return new ArchivingOperation(info, false);
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
