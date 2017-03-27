/*
 * Copyright (C) 2017 Matthias Fussenegger
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
package org.gzipper.java.presentation.control;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.presentation.AlertDialog;
import org.gzipper.java.presentation.GZipper;
import org.gzipper.java.style.CSS;

/**
 * The base controller each other controller should derive from.
 *
 * @author Matthias Fussenegger
 */
public abstract class BaseController implements Initializable {

    /**
     * A set with all the stages currently open.
     */
    protected static Set<Stage> _stages = new HashSet<>();

    public static Set<Stage> getStages() {
        return _stages;
    }

    /**
     * The frame image used for each scene.
     */
    protected static Image _frameImage;

    public static Image getFrameImage() {
        return _frameImage;
    }

    /**
     * The aggregated primary stage.
     */
    protected Stage _primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        _primaryStage = primaryStage;
    }

    /**
     * The currently active theme.
     */
    protected CSS.Theme _theme = CSS.Theme.MODENA;

    /**
     * Constructs a new controller with the specified CSS theme.
     *
     * @param theme the {@link CSS} theme to apply.
     */
    public BaseController(CSS.Theme theme) {
        _theme = theme;
    }

    @FXML
    protected MenuItem _aboutMenuItem;

    @FXML
    protected void handleAboutMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_aboutMenuItem)) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/AboutView.fxml"));
            AboutViewController controller = new AboutViewController(_theme);

            fxmlLoader.setResources(I18N.getBundle());
            fxmlLoader.setController(controller);

            final Stage aboutView = new Stage();
            aboutView.initModality(Modality.WINDOW_MODAL);
            controller.setPrimaryStage(aboutView);

            try {
                Scene scene = new Scene(fxmlLoader.load());
                if (_theme == CSS.Theme.DARK_THEME) {
                    scene.getStylesheets().add(getClass().getResource(
                            CSS.STYLESHEET_DARK_THEME).toExternalForm());
                }
                aboutView.getIcons().add(_frameImage);
                aboutView.setTitle(I18N.getString("aboutTitle.text"));
                aboutView.setScene(scene);
                aboutView.showAndWait();
            } catch (IOException ex) {
                Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
                AlertDialog.showErrorDialog(I18N.getString("error.text"),
                        I18N.getString("errorOpeningWindow.text"));
            }
        }
    }
}
