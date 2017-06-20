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

import java.util.HashSet;
import java.util.Set;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.stage.Stage;
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
    private static Set<Stage> _stages = new HashSet<>();

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
     * The aggregated host services. May be {@code null} if not set.
     */
    protected HostServices _hostServices;

    /**
     * The currently active theme.
     */
    protected CSS.Theme _theme = CSS.Theme.MODENA;

    /**
     * Constructs a controller with the specified CSS theme.
     *
     * @param theme the {@link CSS} theme to apply.
     */
    public BaseController(CSS.Theme theme) {
        _theme = theme;
    }

    /**
     * Constructs a controller with the specified CSS theme and host services.
     *
     * @param theme the {@link CSS} theme to apply.
     * @param hostServices the host services to aggregate.
     */
    public BaseController(CSS.Theme theme, HostServices hostServices) {
        _theme = theme;
        _hostServices = hostServices;
    }

    /**
     * Loads an alternative theme.
     *
     * @param enableTheme true to enable, false to disable alternative theme.
     * @param theme the theme to be loaded.
     */
    protected void loadAlternativeTheme(boolean enableTheme, CSS.Theme theme) {
        _theme = enableTheme ? theme : CSS.Theme.getDefault();
        _stages.forEach((stage) -> {
            CSS.load(_theme, stage.getScene().getStylesheets());
        });
    }

    /**
     * Closes the primary stage of this controller.
     */
    protected void close() {
        _primaryStage.close();
    }

    /**
     * Exits the application.
     */
    protected void exit() {
        close();
        Platform.exit();
        System.exit(0);
    }
}
