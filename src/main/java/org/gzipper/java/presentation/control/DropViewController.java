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

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.gzipper.java.application.util.FileUtil;
import org.gzipper.java.style.CSS;

/**
 * Controller for the FXML named "DropView.fxml".
 *
 * @author Matthias Fussenegger
 */
public class DropViewController extends BaseController {

    /**
     * A list consisting of the parsed addresses.
     */
    private final List<String> _addresses;

    @FXML
    private TextArea _textArea;

    @FXML
    private Button _cancelButton;

    @FXML
    private Button _submitButton;

    @FXML
    private Text _titleText;

    /**
     * Constructs a new controller with the specified CSS theme.
     *
     * @param theme the {@link CSS} theme to apply.
     */
    public DropViewController(CSS.Theme theme) {
        super(theme);
        _addresses = new LinkedList<>();
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
                    // only add token if it is a path to a valid file
                    if (FileUtil.isValidFile(token)) {
                        _addresses.add(token);
                    }
                }
            }
            close();
        }
    }

    /**
     * Returns a list of file paths. This list may be empty if no strings have
     * been parsed or the parsed strings were not valid files.
     *
     * @return a {@link List} consisting of file paths.
     */
    public List<String> getAddresses() {
        return _addresses;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _titleText.setFont(Font.font("System", FontWeight.BOLD, -1));
    }
}
