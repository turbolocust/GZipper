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
package org.gzipper.java.presentation;

import java.util.List;

import javafx.scene.Scene;

/**
 * Class that allows global access to CSS related information.
 *
 * @author Matthias Fussenegger
 */
public final class CSS {

    private CSS() {
        throw new AssertionError("Holds static members only");
    }

    /**
     * Applies the specified {@link Theme} to the specified {@link Scene}.
     * <p>
     * To be more precise, this method clears the list of style sheets of the
     * specified scene and adds the resource location of the respective CSS file
     * in external form to the list. This way the alternative theme will be
     * loaded.
     *
     * @param theme the theme to be loaded.
     * @param scene the scene to which to apply the theme.
     */
    public static void load(Theme theme, Scene scene) {
        List<String> stylesheets = scene.getStylesheets();
        stylesheets.clear();
        if (theme != Theme.getDefault()) {
            stylesheets.add(CSS.class.getResource(
                    theme.getLocation()).toExternalForm());
        }
    }

    /**
     * Enumeration that consists of all existing visual themes.
     */
    public enum Theme {

        MODENA("MODENA"),
        DARK_THEME("/css/DarkTheme.css");

        /**
         * The physical location of the associated style sheet.
         */
        private final String _location;

        Theme(String location) {
            _location = location;
        }

        /**
         * Returns the physical location of the associated style sheet.
         *
         * @return the physical location of the associated style sheet.
         */
        public String getLocation() {
            return _location;
        }

        /**
         * Returns the default theme of the application.
         *
         * @return the default theme.
         */
        public static Theme getDefault() {
            return MODENA;
        }
    }
}
