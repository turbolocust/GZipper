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
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import org.gzipper.java.application.hashing.MessageDigestAlgorithm;
import org.gzipper.java.application.hashing.MessageDigestProvider;
import org.gzipper.java.application.hashing.MessageDigestProviderImpl;
import org.gzipper.java.application.hashing.MessageDigestResult;
import org.gzipper.java.application.util.ListUtils;
import org.gzipper.java.application.util.TaskHandler;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.presentation.model.HashViewTableModel;
import org.gzipper.java.presentation.style.CSS;
import org.gzipper.java.util.Log;
import org.gzipper.java.application.concurrency.Interruptible;

/**
 * Controller for the FXML named "HashView.fxml".
 *
 * @author Matthias Fussenegger
 */
public final class HashViewController extends BaseController implements Interruptible {

    /**
     * The aggregated {@link MessageDigestProvider}.
     */
    private final MessageDigestProvider _provider;

    /**
     * The currently selected {@link MessageDigestAlgorithm}.
     */
    private final ObjectProperty<MessageDigestAlgorithm> _algorithm;

    /**
     * Set to remember results for {@link #_resultTable} to avoid duplicates.
     */
    private final Set<MessageDigestResult> _models = new HashSet<>();

    /**
     * Handler used to execute tasks.
     */
    private final TaskHandler _taskHandler;

    /**
     * If set to false the currently running task will be interrupted.
     */
    private volatile boolean _isAlive = false;

    @FXML
    private TableView<HashViewTableModel> _resultTable;
    @FXML
    private TableColumn<HashViewTableModel, String> _fileNameColumn;
    @FXML
    private TableColumn<HashViewTableModel, String> _filePathColumn;
    @FXML
    private TableColumn<HashViewTableModel, String> _hashValueColumn;
    @FXML
    private ComboBox<MessageDigestAlgorithm> _algorithmComboBox;
    @FXML
    private Button _addFilesButton;
    @FXML
    private Button _closeButton;
    @FXML
    private CheckBox _appendFilesCheckBox;
    @FXML
    private CheckBox _lowerCaseCheckBox;

    /**
     * Constructs a controller for the hash view with the specified CSS theme.
     *
     * @param theme the {@link CSS} theme to apply.
     */
    public HashViewController(CSS.Theme theme) {
        super(theme);
        _provider = new MessageDigestProviderImpl();
        _algorithm = new SimpleObjectProperty<>();
        _taskHandler = new TaskHandler(TaskHandler.ExecutorType.QUEUED);
    }

    @FXML
    void handleAddFilesButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_addFilesButton)) {
            final FileChooser fc = new FileChooser();
            fc.setTitle(I18N.getString("browseForFiles.text"));

            final List<File> selectedFiles
                    = fc.showOpenMultipleDialog(_primaryStage);
            computeAndAppend(selectedFiles); // performs null check
        }
    }

    @FXML
    void handleLowerCaseCheckBoxAction(ActionEvent evt) {
        if (evt.getSource().equals(_lowerCaseCheckBox)) {
            _resultTable.getItems().forEach(item -> {
                item.setHashValue(setCase(item.getHashValue()));
            });
            _resultTable.refresh();
        }
    }

    @FXML
    void handleAlgorithmComboBoxAction(ActionEvent evt) {
        if (evt.getSource().equals(_algorithmComboBox)) {
            final int size = _resultTable.getItems().size();
            final List<File> files = new ArrayList<>(size);
            _resultTable.getItems().stream()
                    .map((model) -> new File(model.getFilePath()))
                    .forEachOrdered((file) -> {
                        files.add(file);
                    });
            clearRows();
            computeAndAppend(files);
        }
    }

    @FXML
    void handleCloseButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_closeButton)) {
            close();
        }
    }

    @FXML
    void handleResultTableOnDragOver(DragEvent evt) {
        if (evt.getGestureSource() != _resultTable
                && evt.getDragboard().hasFiles()) {
            evt.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        evt.consume();
    }

    @FXML
    void handleResultTableOnDragDropped(DragEvent evt) {
        final Dragboard dragboard = evt.getDragboard();
        boolean success = false;
        if (dragboard.hasFiles()) {
            computeAndAppend(dragboard.getFiles());
            success = true;
        }
        evt.setDropCompleted(success);
        evt.consume();
    }

    private void initTableCells() {
        _fileNameColumn.setCellValueFactory(data
                -> new ReadOnlyStringWrapper(data.getValue().getFileName()));
        _filePathColumn.setCellValueFactory(data
                -> new ReadOnlyStringWrapper(data.getValue().getFilePath()));
        _hashValueColumn.setCellValueFactory(data
                -> new ReadOnlyStringWrapper(data.getValue().getHashValue()));
        setCellFactory(_fileNameColumn);
        setCellFactory(_filePathColumn);
        setCellFactory(_hashValueColumn);
    }

    private void setCellFactory(TableColumn<HashViewTableModel, String> column) {
        column.setCellFactory((TableColumn<HashViewTableModel, String> col) -> {
            final TableCell<HashViewTableModel, String> cell
                    = new TableCell<HashViewTableModel, String>() {
                @Override
                protected void updateItem(String value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(value);
                    }
                }
            };

            // programmatically set up context menu
            final ContextMenu ctxMenu = new ContextMenu();
            final MenuItem copyMenuItem = new MenuItem(I18N.getString("copy.text"));
            final MenuItem copyRowMenuItem = new MenuItem(I18N.getString("copyRow.text"));
            final MenuItem copyAllMenuItem = new MenuItem(I18N.getString("copyAll.text"));

            copyMenuItem.setOnAction(evt -> { // copy
                if (evt.getSource().equals(copyMenuItem)) {
                    final Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    content.putString(cell.getItem());
                    clipboard.setContent(content);
                }
            });

            copyRowMenuItem.setOnAction(evt -> { // copy row
                if (evt.getSource().equals(copyRowMenuItem)) {
                    final Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    final StringBuilder sb = new StringBuilder();
                    final HashViewTableModel model
                            = (HashViewTableModel) cell.getTableRow().getItem();
                    sb.append(model.getFileName()).append("\t")
                            .append(model.getFilePath()).append("\t")
                            .append(model.getHashValue());
                    content.putString(sb.toString());
                    clipboard.setContent(content);
                }
            });

            copyAllMenuItem.setOnAction(evt -> { // copy all
                if (evt.getSource().equals(copyAllMenuItem)) {
                    final Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    final StringBuilder sb = new StringBuilder();
                    _resultTable.getItems().forEach((model) -> {
                        sb.append(model.getFileName()).append("\t")
                                .append(model.getFilePath()).append("\t")
                                .append(model.getHashValue()).append("\n");
                    });
                    content.putString(sb.toString());
                    clipboard.setContent(content);
                }
            });

            ctxMenu.getItems().addAll(copyMenuItem, copyRowMenuItem, copyAllMenuItem);

            cell.contextMenuProperty().bind(Bindings
                    .when(cell.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(ctxMenu));

            return cell;
        });
    }

    private String setCase(String value) {
        return _lowerCaseCheckBox.isSelected()
                ? value.toLowerCase()
                : value.toUpperCase();
    }

    @SuppressWarnings("SleepWhileInLoop")
    private void computeAndAppend(final List<File> files) {
        if (_isAlive || ListUtils.isNullOrEmpty(files)) {
            return;
        }
        // clear table if append is deactivated
        if (!_appendFilesCheckBox.isSelected()) {
            clearRows();
        }

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                for (File file : files) {
                    if (!_isAlive) {
                        return false;
                    }
                    try {
                        // folders are not supported
                        if (file.isFile() && file.exists()) {
                            byte[] bytes = Files.readAllBytes(file.toPath());
                            appendColumn(_provider.computeHash(
                                    bytes, _algorithm.get()), file);
                        }
                    }
                    catch (IOException ex) {
                        Log.e("Error reading file.", ex);
                        appendColumn(new MessageDigestResult(), file);
                    }
                }
                return true;
            }
        };
        // set up event handlers
        task.setOnSucceeded(e -> {
            _isAlive = false;
            e.consume();
        });
        task.setOnFailed(e -> {
            _isAlive = false;
            e.consume();
        });

        bindUIcontrols(task);
        _taskHandler.submit(task);
        _isAlive = true;

        while (task.isRunning()) {
            try {
                Thread.sleep(10);
            }
            catch (InterruptedException ex) {
                Log.e("Task interrupted.", ex);
            }
        }
    }

    private void bindUIcontrols(Task<?> task) {
        _addFilesButton.disableProperty().bind(task.runningProperty());
        _algorithmComboBox.disableProperty().bind(task.runningProperty());
        _appendFilesCheckBox.disableProperty().bind(task.runningProperty());
        _lowerCaseCheckBox.disableProperty().bind(task.runningProperty());
    }

    private void clearRows() {
        _resultTable.getItems().clear();
        _models.clear();
    }

    private void appendColumn(MessageDigestResult result, File file) {
        if (!_models.contains(result)) {
            final HashViewTableModel model;
            if (!result.isEmpty()) {
                model = new HashViewTableModel(
                        file.getName(),
                        file.getAbsolutePath(),
                        setCase(result.toString()));
            } else {
                model = new HashViewTableModel(
                        file.getName(),
                        file.getAbsolutePath(),
                        I18N.getString("errorReadingFile.text"));
            }
            Platform.runLater(() -> {
                _resultTable.getItems().add(model);
                _models.add(result);
            });
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set up combo box
        final MessageDigestAlgorithm selectedAlgorithm
                = MessageDigestAlgorithm.SHA_256;
        _algorithmComboBox.getItems().addAll(MessageDigestAlgorithm.values());
        _algorithmComboBox.valueProperty().bindBidirectional(_algorithm);
        _algorithm.setValue(selectedAlgorithm);

        // set up table
        initTableCells();
    }

    @Override
    public void interrupt() {
        _isAlive = false;
    }
}
