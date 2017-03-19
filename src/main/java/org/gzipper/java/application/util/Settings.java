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
import java.util.ResourceBundle;
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

    private ResourceBundle _resourceBundle;

    private OperatingSystem _operatingSystem;

    private Settings() {
        _defaults = initDefaults();
    }

    public void init(String location, OperatingSystem os, ResourceBundle bundle) {
        File f = new File(location);

        _resourceBundle = bundle;
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

    public String getProperty(String key) {
        return _properties.getProperty(key);
    }

    public OperatingSystem getOperatingSystem() {
        return _operatingSystem;
    }

    public ResourceBundle getResourceBundle() {
        return _resourceBundle;
    }

    public static Settings getInstance() {
        return SettingsHolder.INSTANCE;
    }

    private static class SettingsHolder {

        private static Settings INSTANCE = new Settings();
    }

}
