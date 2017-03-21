/*
 * Copyright (C) 2016 Matthias Fussenegger
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
package org.gzipper.java.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Matthias Fussenegger
 */
public class I18N {

    private static ResourceBundle _bundle;

    public static String getString(String key) {
        return getBundle().getString(key);
    }

    public static synchronized ResourceBundle getBundle() {
        if (_bundle == null) {
            final String base = "i18n/gzipperMainView";
            _bundle = ResourceBundle.getBundle(base, Locale.ENGLISH);
        }
        return _bundle;
    }

    /**
     * Holds static members only.
     */
    private I18N() {
    }
}
