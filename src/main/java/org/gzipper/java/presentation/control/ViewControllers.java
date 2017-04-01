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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.presentation.AlertDialog;
import org.gzipper.java.presentation.GZipper;
import static org.gzipper.java.presentation.control.BaseController._frameImage;
import org.gzipper.java.style.CSS;

/**
 *
 * @author Matthias Fussenegger
 */
public class ViewControllers {

    /**
     * Static reference to the class of {@link ViewControllers}.
     */
    private static final Class<ViewControllers> CLAZZ = ViewControllers.class;

    /**
     * Defines the resource for the about view.
     */
    private static final String ABOUT_VIEW_RES = "/fxml/AboutView.fxml";

    /**
     * Defines the resource for the drop view.
     */
    private static final String DROP_VIEW_RES = "/fxml/DropView.fxml";

    /**
     * Shows the about view in a separate window.
     *
     * @param theme the theme to apply.
     * @return the controller for the view.
     */
    static AboutViewController showAboutView(CSS.Theme theme) {
        FXMLLoader fxmlLoader = initFXMLLoader(ABOUT_VIEW_RES);
        AboutViewController controller = new AboutViewController(theme);
        fxmlLoader.setController(controller);

        final Stage aboutView = new Stage();
        aboutView.initModality(Modality.APPLICATION_MODAL);
        controller.setPrimaryStage(aboutView);

        try {
            aboutView.getIcons().add(_frameImage);
            aboutView.setTitle(I18N.getString("aboutViewTitle.text"));
            aboutView.setScene(loadScene(fxmlLoader, theme));
            aboutView.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
            AlertDialog.showErrorDialog(I18N.getString("error.text"),
                    I18N.getString("errorOpeningWindow.text"));
        }
        return controller;
    }

    /**
     * Shows the drop view in a separate window.
     *
     * @param theme the theme to apply.
     * @return the controller for the view.
     */
    static DropViewController showDropView(CSS.Theme theme) {
        FXMLLoader fxmlLoader = initFXMLLoader(DROP_VIEW_RES);
        DropViewController controller = new DropViewController(theme);
        fxmlLoader.setController(controller);

        final Stage dropView = new Stage();
        dropView.initModality(Modality.APPLICATION_MODAL);
        controller.setPrimaryStage(dropView);

        try {
            dropView.getIcons().add(_frameImage);
            dropView.setTitle("AddressDropper");
            dropView.setScene(loadScene(fxmlLoader, theme));
            dropView.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
            AlertDialog.showErrorDialog(I18N.getString("error.text"),
                    I18N.getString("errorOpeningWindow.text"));
        }
        return controller;
    }

    /**
     * Initializes a new {@link FXMLLoader} with the specified resource string
     * and sets the resources to the default bundle as of {@link I18N}.
     *
     * @param resource the resources to initialize the {@link FXMLLoader} with.
     * @return the initialized {@link FXMLLoader}.
     */
    private static FXMLLoader initFXMLLoader(String resource) {
        FXMLLoader loader = new FXMLLoader(CLAZZ.getResource(resource));
        loader.setResources(I18N.getBundle());
        return loader;
    }

    /**
     * Loads a scene by using the specified {@link FXMLLoader} and then applies
     * the correct theme.
     *
     * @param loader the {@link FXMLLoader} to use.
     * @param theme the CSS theme to apply.
     * @return the loaded {@link Scene}.
     * @throws IOException if an I/O error occurs.
     */
    private static Scene loadScene(FXMLLoader loader, CSS.Theme theme) throws IOException {
        Scene scene = new Scene(loader.load());
        if (theme == CSS.Theme.DARK_THEME) {
            scene.getStylesheets().add(CLAZZ.getResource(
                    CSS.STYLESHEET_DARK_THEME).toExternalForm());
        }
        return scene;
    }

    /**
     * Holds static members only.
     */
    private ViewControllers() {
    }
}
