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
package gzipper.presentation;

import gzipper.application.util.Settings;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
public abstract class BaseController {

    protected static Settings _settings;

    public static Settings getSettings() {
        return _settings;
    }

    protected static Image _frameImage;

    public static Image getFrameImage() {
        return _frameImage;
    }

    /**
     * Resource bundle that contains locale-sensitive strings.
     */
    protected static ResourceBundle _resources;

    public static ResourceBundle getResourceBundle() {
        return _resources;
    }

    /**
     * The aggregated primary stage.
     */
    protected Stage _primaryStage;

    protected void setPrimaryStage(Stage primaryStage) {
        _primaryStage = primaryStage;
    }

    @FXML
    private void handleAboutMenuItemAction(ActionEvent evt) {
        Stage aboutWindow = new Stage();
        aboutWindow.getIcons().add(_frameImage);
        GridPane gridPane = new GridPane();
        Button button = new Button("Close");
        button.setMaxSize(Double.MAX_VALUE, 30);
        WebView webView = new WebView();
        webView.getEngine().loadContent("<html><br /><p align=\"center\">"
                + "<br />Author: Matthias Fussenegger<br />E-mail: matfu2@me.com<br /><b>v2017-10-02</b><br />"
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
