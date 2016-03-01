/*
 * Copyright (C) 2016 Matthias Fussenegger
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gzipper.graphics;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 *
 * @author Matthias Fussenegger
 */
public class gzipperFXController implements Initializable {

    @FXML
    private MenuItem _closeMenuItem;

    @FXML
    private MenuItem _deleteMenuItem;

    @FXML
    private TextArea _textArea;

    @FXML
    private TextField _outputPath;
    
    @FXML
    private void handleCloseMenuItemAction(ActionEvent evt) {
        if (evt.getSource() == _closeMenuItem) {
            System.exit(0);
        }
    }

    @FXML
    private void handleDeleteMenuItemAction(ActionEvent evt) {
        if (evt.getSource() == _deleteMenuItem) {
            _textArea.clear();
            _textArea.setText("run:\n");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _textArea.setEditable(false);
        _textArea.setText("run:\nOutput path can be changed in text field above.\n");
    }
}
