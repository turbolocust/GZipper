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
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 *
 * @author Matthias Fussenegger
 */
public class gzipperFXController implements Initializable {

    /**
     * The associated primary stage
     */
    private Stage _primaryStage;

    protected void setPrimaryStage(Stage primaryStage) {
        _primaryStage = primaryStage;
    }

    @FXML
    private MenuItem _closeMenuItem;

    @FXML
    private MenuItem _deleteMenuItem;

    @FXML
    private MenuItem _aboutMenuItem;

    @FXML
    private TextArea _textArea;

    @FXML
    private TextField _outputPath;

    @FXML
    private void handleCloseMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_closeMenuItem)) {
            System.exit(0);
        }
    }

    @FXML
    private void handleDeleteMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_deleteMenuItem)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Confirmation");
            alert.setHeaderText("This will clear the text area");
            /*change default button to {@code ButtonType.NO} to avoid accidential press of return key*/
            Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
            Button noButton = (Button) alert.getDialogPane().lookupButton(ButtonType.NO);
            yesButton.setDefaultButton(false);
            noButton.setDefaultButton(true);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                _textArea.clear();
                _textArea.setText("run:\n");
            }
        }
    }

    @FXML
    private void handleAboutMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_aboutMenuItem)) {
            Stage aboutWindow = new Stage();
            GridPane gridPane = new GridPane();
            Button button = new Button("OK");
            button.setMaxSize(Double.MAX_VALUE, 30);
            WebView webView = new WebView();
            webView.getEngine().loadContent("<html><br /><p align=\"center\">"
                    + "<br />Author: Matthias Fussenegger<br />E-mail: matfu2@me.com<br /><b>v2016-03-02</b><br />"
                    + "<br />This program uses parts of the commons-compress library by Apache Foundation"
                    + "&nbsp;and is licensed under the GNU General Public License 3"
                    + "(<a href=\"http://www.gnu.org/licenses/\">http://www.gnu.org/licenses/</a>)"
                    + "&nbsp;<br />&nbsp;</p></html>");
            webView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            webView.setContextMenuEnabled(false);
            gridPane.add(webView, 0, 0);
            gridPane.add(button, 0, 1);
            aboutWindow.setTitle("About");
            aboutWindow.initOwner(_primaryStage);
            aboutWindow.setScene(new Scene(gridPane, 600, 500));
            aboutWindow.setResizable(false);
            button.setOnAction((ActionEvent e) -> {
                if (e.getSource().equals(button)) {
                    aboutWindow.close();
                }
            });
            aboutWindow.show();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _textArea.setEditable(false);
        _textArea.setText("run:\nOutput path can be changed in text field above.\n");
    }
}
