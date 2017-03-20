/*
 * Copyright (C) 2016 Matthias Fussenegger
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
package org.gzipper.java.application.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gzipper.java.application.model.OperatingSystem;
import org.gzipper.java.presentation.GZipper;

/**
 *
 * @author Matthias Fussenegger
 */
public class Settings {

    private Properties _properties;

    private final Properties _defaults;

    private OperatingSystem _operatingSystem;

    private Settings() {
        _defaults = initDefaults();
    }

    /**
     * Initializes this singleton class. This should only be called once after a
     * call of the {@link #getInstance()} method.
     *
     * @param location the location of the properties file.
     * @param os the current operating system.
     */
    public void init(String location, OperatingSystem os) {
        File f = new File(location);

        _operatingSystem = os; // to receive environment variables
        _properties = new Properties(_defaults);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
            _properties.load(bis);
        } catch (IOException ex) {
            Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Properties initDefaults() {

        final Properties defaults = new Properties();

        defaults.setProperty("loggingEnabled", "false");
        defaults.setProperty("recentPath", "");

        return defaults;
    }

    public Object setProperty(String key, String value) {
        return _properties.setProperty(key, value);
    }

    public Object setProperty(String key, boolean value) {
        final String propertyValue = value ? "true" : "false";
        return _properties.setProperty(key, propertyValue);
    }

    public String getProperty(String key) {
        return _properties.getProperty(key);
    }

    public OperatingSystem getOperatingSystem() {
        return _operatingSystem;
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
