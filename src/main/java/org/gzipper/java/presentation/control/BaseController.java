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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
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
     * Closes the primary stage of this controller.
     */
    protected void close() {
        _primaryStage.close();
    }

    @FXML
    protected MenuItem _aboutMenuItem;

    @FXML
    protected void handleAboutMenuItemAction(ActionEvent evt) {
        if (evt.getSource().equals(_aboutMenuItem)) {
            ViewControllers.showAboutView(_theme, _hostServices);
        }
    }
}
