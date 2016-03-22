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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
            Optional<ButtonType> result = AlertDialog.showConfirmationDialog("This will clear the text area", "Are you sure?");
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
            aboutWindow.getIcons().add(Settings._frameImage);
            GridPane gridPane = new GridPane();
            Button button = new Button("OK");
            button.setMaxSize(Double.MAX_VALUE, 30);
            WebView webView = new WebView();
            webView.getEngine().loadContent("<html><br /><p align=\"center\">"
                    + "<img src=\"file:/" + Settings._initialPath + "res/icon_256.png\" alt=\"res/icon_256.png\">"
                    + "<br />Author: Matthias Fussenegger<br />E-mail: matfu2@me.com<br /><b>v2016-03-22</b><br />"
                    + "<br />This program uses parts of the commons-compress library by Apache Foundation"
                    + "&nbsp;and is licensed under the GNU General Public License 3 "
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
            webView.setOnKeyPressed((KeyEvent event) -> {
                if (event.getCode().equals(KeyCode.ESCAPE)) {
                    aboutWindow.close();
                }
            });
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

        String decPath = location.getPath();

        if (System.getProperty("os.name").startsWith("Windows")) {
            /*decode path without adding the name of the JAR-file (gzipperFX.fxml = 14 chars)
              - for debugging using an IDE make sure to remove the minus operation (- 14)*/
            decPath = decPath.substring(1, decPath.length() - 14);
        } else {
            Settings._isUnix = true;
            decPath = decPath.substring(0, decPath.length() - 14);
        }

        Settings._frameImage = new Image("file:" + decPath + "res/icon_32.png");
        Settings._initialPath = decPath;

        _textArea.setEditable(false);
        _textArea.setText("run:\nOutput path can be changed in text field above.\n");
    }
}
