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
package org.gzipper.java.presentation.controller.main;

import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.presentation.CSS;
import org.gzipper.java.presentation.Dialogs;
import org.gzipper.java.presentation.controller.AboutViewController;
import org.gzipper.java.presentation.controller.BaseController;
import org.gzipper.java.presentation.controller.DropViewController;
import org.gzipper.java.presentation.controller.HashViewController;
import org.gzipper.java.util.Log;

import java.io.IOException;

/**
 * @author Matthias Fussenegger
 */
public final class ViewControllers {

    /**
     * Defines the resource for this view.
     */
    private static final String ABOUT_VIEW_RES = "/fxml/AboutView.fxml";

    /**
     * Defines the resource for the drop view.
     */
    private static final String DROP_VIEW_RES = "/fxml/DropView.fxml";

    /**
     * Defines the resource for the hash view.
     */
    private static final String HASH_VIEW_RES = "/fxml/HashView.fxml";

    private ViewControllers() {
        throw new AssertionError("Holds static members only");
    }

    /**
     * Shows the about-view in a separate window.
     *
     * @param theme        the theme to be applied.
     * @param icon         the icon to be used in the view
     * @param hostServices the host services to be aggregated.
     */
    static void showAboutView(CSS.Theme theme, Image icon, HostServices hostServices) {

        if (hostServices == null) {
            throw new NullPointerException("Host services must not be null");
        }

        final FXMLLoader fxmlLoader = initFXMLLoader(ABOUT_VIEW_RES);
        AboutViewController controller = new AboutViewController(theme, hostServices);
        fxmlLoader.setController(controller);

        final Stage aboutView = new Stage();
        aboutView.initModality(Modality.APPLICATION_MODAL);
        controller.setPrimaryStage(aboutView);

        try {
            aboutView.getIcons().add(icon);
            aboutView.setTitle(I18N.getString("aboutViewTitle.text"));
            aboutView.setScene(loadScene(fxmlLoader, theme));
            aboutView.showAndWait();
        } catch (IOException ex) {
            handleErrorLoadingView(ex, theme, icon);
        }
    }

    /**
     * Shows the drop view in a separate window.
     *
     * @param theme the theme to be applied.
     * @param icon  the icon to be used in the view
     * @return the controller for the view.
     */
    static DropViewController showDropView(CSS.Theme theme, Image icon) {
        final FXMLLoader fxmlLoader = initFXMLLoader(DROP_VIEW_RES);
        DropViewController controller = new DropViewController(theme);
        fxmlLoader.setController(controller);

        final Stage dropView = new Stage();
        dropView.setAlwaysOnTop(true);
        dropView.initModality(Modality.APPLICATION_MODAL);
        controller.setPrimaryStage(dropView);

        try {
            dropView.getIcons().add(icon);
            dropView.setTitle(I18N.getString("addMany.text"));
            dropView.setScene(loadScene(fxmlLoader, theme));
            dropView.showAndWait();
        } catch (IOException ex) {
            handleErrorLoadingView(ex, theme, icon);
        }

        return controller;
    }

    /**
     * Shows the hash view in a separate window.
     *
     * @param theme the theme to be applied.
     * @param icon  the icon to be used in the view
     */
    static void showHashView(CSS.Theme theme, Image icon) {
        final FXMLLoader fxmlLoader = initFXMLLoader(HASH_VIEW_RES);
        HashViewController controller = new HashViewController(theme);
        fxmlLoader.setController(controller);

        final Stage hashView = new Stage();
        hashView.setAlwaysOnTop(false);
        hashView.initModality(Modality.NONE);
        controller.setPrimaryStage(hashView);

        // add stage to active stages since window is not modal
        BaseController.getStages().add(hashView);

        hashView.setOnCloseRequest(evt -> {
            Log.i("Closing hash view", false);
            controller.interrupt(); // cancels any active task
        });

        hashView.setOnHiding(evt -> {
            Log.i("Hiding hash view", false);
            controller.interrupt(); // cancels any active task
        });

        try {
            hashView.getIcons().add(icon);
            hashView.setTitle(I18N.getString("hashViewTitle.text"));
            hashView.setScene(loadScene(fxmlLoader, theme));
            hashView.show();
        } catch (IOException ex) {
            handleErrorLoadingView(ex, theme, icon);
        }
    }

    /**
     * Initializes a new {@link FXMLLoader} with the specified resource string
     * and sets the resources to the default bundle as of {@link I18N}.
     *
     * @param resource the resources to initialize the {@link FXMLLoader} with.
     * @return the initialized {@link FXMLLoader}.
     */
    private static FXMLLoader initFXMLLoader(String resource) {
        final Class<ViewControllers> clazz = ViewControllers.class;
        FXMLLoader loader = new FXMLLoader(clazz.getResource(resource));
        loader.setResources(I18N.getBundle());
        return loader;
    }

    /**
     * Loads a scene by using the specified {@link FXMLLoader} and then applies
     * the correct theme.
     *
     * @param loader the {@link FXMLLoader} to be used.
     * @param theme  the CSS theme to be applied.
     * @return the loaded {@link Scene}.
     * @throws IOException if an I/O error occurs.
     */
    private static Scene loadScene(FXMLLoader loader, CSS.Theme theme) throws IOException {
        Scene scene = new Scene(loader.load());
        CSS.load(theme, scene);
        return scene;
    }

    /**
     * Handles errors that can occur when trying to load a view. This method
     * will log the localized exception message and bring up an error dialog
     * using the specified theme.
     *
     * @param ex    the {@link Exception} to be logged.
     * @param theme the theme to be applied to the error dialog.
     * @param icon  the icon to be shown in the title.
     */
    private static void handleErrorLoadingView(Exception ex, CSS.Theme theme, Image icon) {
        Log.e(ex.getLocalizedMessage(), ex);
        final String errorText = I18N.getString("error.text");
        Dialogs.showDialog(AlertType.ERROR, errorText, errorText,
                I18N.getString("errorOpeningWindow.text"), theme, icon);
    }
}
