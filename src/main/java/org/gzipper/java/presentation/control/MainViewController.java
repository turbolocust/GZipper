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
import java.util.ArrayList;
import java.util.LinkedList;
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
import javafx.stage.FileChooser;
import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.presentation.AlertDialog;
import org.gzipper.java.presentation.GZipper;
import org.gzipper.java.application.pojo.ArchiveInfo;
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
     * The currently active task. Multiple tasks may be supported in the future.
     */
    private Task<Boolean> _activeTask;

    /**
     * A list consisting of the selected files or the selected archive.
     */
    private List<File> _selectedFiles;

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
    private TextArea _textArea;

    @FXML
    private CheckMenuItem _enableLoggingCheckMenuItem;

    @FXML
    private TextField _outputPath;

    @FXML
    private ComboBox<String> _archiveTypeComboBox;

    @FXML
    private Button _startButton;

    @FXML
    private Button _abortButton;

    @FXML
    private Button _selectFilesButton;

    @FXML
    void handleCompressionLevelMenuItemAction(ActionEvent evt) {
        final MenuItem selectedItem = (MenuItem) evt.getSource();
        Object compressionStrength = selectedItem.getProperties().get(COMPRESSION_LEVEL_KEY);

        if (compressionStrength != null) {
            _compressionLevel = (int) compressionStrength;
            Logger.getLogger(GZipper.class.getName()).log(Level.INFO,
                    "Compression level set to: {0}", _compressionLevel);
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

            boolean compress = _compressRadioButton.isSelected();
            String archiveType = _archiveTypeComboBox.getValue();

            try {
                ArchiveInfo info = compress
                        ? ArchiveInfoFactory.createArchiveInfo(archiveType, 0)
                        : ArchiveInfoFactory.createArchiveInfo(archiveType);

                // TODO: set archive name, file(s) and output path (needs validation)
                ArchivingOperation operation = new ArchivingOperation(info, compress);

                Task<Boolean> task = initArchivingJob(operation);

                _activeTask = task;
                toggleStartAndAbortButton();
                TaskHandler.getInstance().execute(_activeTask);

            } catch (GZipperException ex) {
                Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
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
            fc.setTitle(_resources.getString("select.text"));

            // TODO: extension filter
            if (_compressRadioButton.isSelected()) {
                _selectedFiles = new LinkedList<>();
                _selectedFiles.add(fc.showOpenDialog(_primaryStage));
            } else {
                _selectedFiles = fc.showOpenMultipleDialog(_primaryStage);
            }
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
            appendToTextArea(_resources.getString("operationSuccess.text"));
            finalizeArchivingJob(operation);
            e.consume();
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
                + operation.calculateElapsedTime());
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

            if (System.getProperty("os.name").startsWith("Windows")) {
                os = new Windows();
            } else {
                os = new Unix();
            }

            _settings = new Settings(settingsFile, os);

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
            List<String> typeNames = new ArrayList<>(ArchiveType.values().length);
            for (ArchiveType type : ArchiveType.values()) {
                typeNames.add(type.getFriendlyName());
            }

            _archiveTypeComboBox.getItems().addAll(typeNames);
            _archiveTypeComboBox.setValue(typeNames.get(0));

        } catch (IOException ex) {
            Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
        }

        _resources = resources;
        _frameImage = new Image("/images/icon_32.png");

        _textArea.setText("run:\n" + _resources.getString("changeOutputPath.text") + "\n");
    }

}
