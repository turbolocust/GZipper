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
package org.gzipper.java.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.gzipper.java.application.model.OperatingSystem;

/**
 * Singleton that provides convenience when working with {@link Properties} and
 * represents the settings file that can be used globally.
 *
 * @author Matthias Fussenegger
 */
public final class Settings {

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
     * Initializes this singleton class. This should only be called once after a
     * call of the {@link #getInstance()} method.
     *
     * @param props the properties file to initialize this class with.
     * @param os the current operating system.
     */
    public void init(File props, OperatingSystem os) {

        _propsFile = props;

        _os = os; // to receive environment variables
        _props = new Properties(_defaults);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(props))) {
            _props.load(bis);
        } catch (IOException ex) {
            Log.e(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Initializes the default properties values.
     *
     * @return the default properties.
     */
    private Properties initDefaults() {

        final Properties defaults = new Properties();

        defaults.setProperty("loggingEnabled", "false");
        defaults.setProperty("recentPath", "");
        defaults.setProperty("darkThemeEnabled", "false");
        defaults.setProperty("showGzipInfoDialog", "true");

        return defaults;
    }

    /**
     * Sets a new property to {@link #_props}.
     *
     * @param key the key of the property.
     * @param value the value of the property as string.
     * @return the previous value of the specified key.
     */
    public synchronized Object setProperty(String key, String value) {
        return _props.setProperty(key, value);
    }

    /**
     * Sets a new property to {@link #_props}.
     *
     * @param key the key of the property.
     * @param value the value of the property as boolean.
     * @return the previous value of the specified key.
     */
    public synchronized Object setProperty(String key, boolean value) {
        final String propertyValue = value ? "true" : "false";
        return _props.setProperty(key, propertyValue);
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
        String property = _props.getProperty(key);
        return property != null && property.equals("true");
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
     * Saves all the properties to {@link #_propsFile};
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

        private static Settings INSTANCE = new Settings();
    }
}
