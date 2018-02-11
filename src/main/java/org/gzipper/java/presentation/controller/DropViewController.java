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
package org.gzipper.java.presentation.controller;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.gzipper.java.application.util.FileUtils;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.presentation.style.CSS;

/**
 * Controller for the FXML named "DropView.fxml".
 *
 * @author Matthias Fussenegger
 */
public class DropViewController extends BaseController {

    /**
     * A list consisting of the parsed file addresses.
     */
    private final Set<String> _addresses;

    @FXML
    private TextArea _textArea;
    @FXML
    private Button _cancelButton;
    @FXML
    private Button _submitButton;
    @FXML
    private CheckBox _appendAddressesCheckBox;
    @FXML
    private Text _titleText;

    /**
     * Constructs a controller for Address Dropper with the specified CSS theme.
     *
     * @param theme the {@link CSS} theme to apply.
     */
    public DropViewController(CSS.Theme theme) {
        super(theme);
        _addresses = new LinkedHashSet<>();
    }

    @FXML
    void handleCancelButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_cancelButton)) {
            close();
        }
    }

    @FXML
    void handleSubmitButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_submitButton)) {
            final String text = _textArea.getText();
            if (!text.isEmpty()) {
                // tokenize file paths
                StringTokenizer tokenizer = new StringTokenizer(text, "\"\n");
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    // only add token if it is a valid file or directory
                    if (FileUtils.isValid(token)) {
                        _addresses.add(token);
                    }
                }
            }
            close();
        }
    }

    @FXML
    void handleTextAreaOnDragOver(DragEvent evt) {
        if (evt.getGestureSource() != _textArea
                && evt.getDragboard().hasFiles()) {
            evt.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        evt.consume();
    }

    @FXML
    void handleTextAreaOnDragDropped(DragEvent evt) {
        final Dragboard dragboard = evt.getDragboard();
        boolean success = false;
        if (dragboard.hasFiles()) {
            if (!_appendAddressesCheckBox.isSelected()
                    && !_textArea.getText().isEmpty()) {
                _textArea.clear();
            }
            // add each dropped file's path to text area
            dragboard.getFiles().forEach((file) -> {
                _textArea.appendText(file.getAbsolutePath() + "\n");
            });
            success = true;
        }
        evt.setDropCompleted(success);
        evt.consume();
    }

    /**
     * Returns a list of file paths. This list may be empty if no strings have
     * been parsed or the parsed strings were not valid files.
     *
     * @return a {@link List} consisting of file paths.
     */
    public List<String> getAddresses() {
        return new LinkedList<>(_addresses);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _titleText.setFont(Font.font("System", FontWeight.BOLD, -1));
        _appendAddressesCheckBox.setTooltip(
                new Tooltip(I18N.getString("appendAddressesTooltip.text")));
    }
}
