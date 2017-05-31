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
package org.gzipper.java.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Class that allows global access to the internationalization files, which will
 * be accessed through a {@link ResourceBundle}.
 *
 * @author Matthias Fussenegger
 */
public class I18N {

    /**
     * The resource bundle used to access internationalized strings.
     */
    private static ResourceBundle _bundle;

    /**
     * Returns the value that belongs to the specified key.
     *
     * @param key the key as string.
     * @return the value that belongs to the key.
     */
    public static String getString(String key) {
        return getBundle().getString(key);
    }

    /**
     * Returns the aggregated {@link ResourceBundle} used by this class.
     *
     * @return the aggregated {@link ResourceBundle} used by this class.
     */
    public static synchronized ResourceBundle getBundle() {
        if (_bundle == null) {
            final String base = "i18n/gzipperMainView";
            final Locale locale = determineLocale();
            _bundle = locale != null
                    ? ResourceBundle.getBundle(base, locale)
                    : ResourceBundle.getBundle(base);
        }
        return _bundle;
    }

    /**
     * Determines the {@link Locale} of the system.
     *
     * @return the {@link Locale} of the system. May be {@code null}.
     */
    private static Locale determineLocale() {
        String userLang = System.getProperty("user.language");
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.getLanguage().equals(userLang)) {
                return locale;
            }
        }
        return null;
    }

    /**
     * Holds static members only.
     */
    private I18N() {
    }
}
