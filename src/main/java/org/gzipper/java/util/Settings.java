/*
 * Copyright (C) 2018 Matthias Fussenegger
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
package org.gzipper.java.util;

import org.gzipper.java.application.model.OperatingSystem;
import org.gzipper.java.application.util.StringUtils;

import java.io.*;
import java.util.Properties;

/**
 * Singleton that provides convenience when working with {@link Properties} and
 * represents the settings file that can be used globally.
 *
 * @author Matthias Fussenegger
 */
public final class Settings {

    public static final String FALSE_STRING = "false";
    public static final String TRUE_STRING = "true";

    /**
     * The actual properties file. Required to store away changed properties.
     */
    private File _propsFile;

    /**
     * The properties read from the {@link #_propsFile};
     */
    private Properties _props;

    /**
     * The default properties values for restoration.
     */
    private final Properties _defaults;

    /**
     * The operating system the JVM runs on.
     */
    private OperatingSystem _os;

    private Settings() {
        _defaults = initDefaults();
    }

    /**
     * Initializes this singleton class. This may only be called once.
     *
     * @param props the properties file to initialize this class with.
     * @param os    the current operating system.
     */
    public void init(File props, OperatingSystem os) {
        if (_props == null) {
            _os = os; // to receive environment variables
            if (props != null) {
                _propsFile = props;
                _props = new Properties(_defaults);

                try (final FileInputStream fis = new FileInputStream(props);
                     final BufferedInputStream bis = new BufferedInputStream(fis)) {
                    _props.load(bis);
                } catch (IOException ex) {
                    Log.e(ex.getLocalizedMessage(), ex);
                }
            }
        }
    }

    /**
     * Initializes the default properties values.
     *
     * @return the default properties.
     */
    private Properties initDefaults() {

        final Properties defaults = new Properties();

        defaults.setProperty("loggingEnabled", FALSE_STRING);
        defaults.setProperty("recentPath", StringUtils.EMPTY);
        defaults.setProperty("darkThemeEnabled", FALSE_STRING);
        defaults.setProperty("showGzipInfoDialog", TRUE_STRING);

        return defaults;
    }

    /**
     * Sets (or overrides) the value of the property with the specified {@code key}.
     *
     * @param key   the key of the property.
     * @param value the value of the property as {@code String}.
     */
    public synchronized void setProperty(String key, String value) {
        _props.setProperty(key, value);
    }

    /**
     * Sets (or overrides) the value of the property with the specified {@code key}.
     *
     * @param key   the key of the property.
     * @param value the value of the property as {@code boolean}.
     */
    public synchronized void setProperty(String key, boolean value) {
        final String propertyValue = value ? TRUE_STRING : FALSE_STRING;
        _props.setProperty(key, propertyValue);
    }

    /**
     * Returns the property with the specified key if it exists.
     *
     * @param key the key of the property.
     * @return the value of the property as string.
     */
    public String getProperty(String key) {
        return _props.getProperty(key);
    }

    /**
     * Evaluates and returns the property with the specified key if it exists.
     *
     * @param key the key of the property.
     * @return true if property equals "true", false otherwise.
     */
    public boolean evaluateProperty(String key) {
        final String property = _props.getProperty(key);
        return property != null && property.equals(TRUE_STRING);
    }

    /**
     * Returns the operating system on which the JVM is running on.
     *
     * @return the operating system on which the JVM is running on.
     */
    public OperatingSystem getOs() {
        return _os;
    }

    /**
     * Saves all stored properties to the Properties file.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void storeAway() throws IOException {
        _props.store(new BufferedOutputStream(new FileOutputStream(_propsFile)), "");
    }

    /**
     * Restores the default properties.
     */
    public void restoreDefaults() {
        _props.clear();
        _props.putAll(_defaults);
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return the singleton instance of this class.
     */
    public static Settings getInstance() {
        return SettingsHolder.INSTANCE;
    }

    /**
     * Holder class for singleton instance.
     */
    private static class SettingsHolder {

        private static final Settings INSTANCE = new Settings();
    }
}
