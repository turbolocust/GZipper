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
package org.gzipper.java.presentation.controller;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.gzipper.java.presentation.CSS;

import java.util.HashSet;
import java.util.Set;

/**
 * The base controller every other controller should derive from.
 *
 * @author Matthias Fussenegger
 */
public abstract class BaseController implements Initializable {

    //<editor-fold desc="Static members">

    /**
     * A set with all the stages currently open.
     */
    private static final Set<Stage> _stages = new HashSet<>();

    /**
     * Returns all currently open stages.
     *
     * @return all currently open stages.
     */
    public static Set<Stage> getStages() {
        return _stages;
    }

    /**
     * The icon image to be used by each stage.
     */
    protected static Image iconImage;

    /**
     * Returns the icon image that is to be used by each stage.
     *
     * @return the icon image that is to be used by each stage.
     */
    public static Image getIconImage() {

        var resource = BaseController.class.getResource("/images/icon_32.png");

        if (resource != null) {
            iconImage = new Image(resource.toExternalForm());
        } else {
            iconImage = new WritableImage(32, 32); // blank image
        }

        return iconImage;
    }

    //</editor-fold>

    /**
     * The aggregated primary stage.
     */
    protected Stage primaryStage;

    /**
     * Sets the primary stage of this controller.
     *
     * @param primaryStage the primary stage to be set.
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * The aggregated host services. May be {@code null} if not set.
     */
    protected HostServices hostServices;

    /**
     * The currently active theme.
     */
    protected CSS.Theme theme;

    /**
     * Constructs a controller with the specified CSS theme.
     *
     * @param theme the {@link CSS} theme to be applied.
     */
    public BaseController(CSS.Theme theme) {
        this.theme = theme;
    }

    /**
     * Constructs a controller with the specified CSS theme and host services.
     *
     * @param theme        the {@link CSS} theme to be applied.
     * @param hostServices the host services to be aggregated.
     */
    public BaseController(CSS.Theme theme, HostServices hostServices) {
        this.theme = theme;
        this.hostServices = hostServices;
    }

    /**
     * Loads the alternative theme (dark theme).
     *
     * @param enableTheme true to enable, false to disable the alternative theme.
     */
    protected void loadAlternativeTheme(boolean enableTheme) {
        this.theme = enableTheme ? CSS.Theme.DARK_THEME : CSS.Theme.getDefault();
        _stages.forEach((stage) -> CSS.load(this.theme, stage.getScene()));
    }

    /**
     * Closes the primary stage of this controller. Must be called by the UI thread.
     */
    protected void close() {
        primaryStage.close();
    }

    /**
     * Terminates the application gracefully.
     */
    protected void exit() {
        close();
        Platform.exit();
        System.exit(0);
    }
}
