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
package gzipper.operations;

import gzipper.exceptions.ConfigErrorException;
import gzipper.graphics.GUI;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for parsing configuration file. Also stores all the entries of the file
 *
 * @author Matthias Fussenegger
 */
public class ConfigFileParser {

    /**
     * Stores the configuration entries
     */
    private final List<String> _configEntries;

    /**
     * Stores the decoded standard path of JAR-file
     */
    private final String _settingsPath;

    /**
     * Instantiates a new {@code ConfigFileParser} to parse configuration file
     *
     * @param rootPath The decoded standard path of JAR-file
     * @throws java.io.IOException If an error parsing file occurred
     * @throws gzipper.exceptions.ConfigErrorException If configuration file is
     * corrupt
     */
    public ConfigFileParser(String rootPath) throws IOException, ConfigErrorException {
        _configEntries = new ArrayList<>();
        _settingsPath = rootPath + "gzipper.ini";
        parseLoggerConfig();
    }

    /**
     * Parses the configuration file and stores all entries in a list
     *
     * @throws IOException If an error while reading configuration file occurs
     * @throws ConfigErrorException If configuration file has errors
     */
    private void parseLoggerConfig() throws IOException, ConfigErrorException {
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(_settingsPath), Charset.forName("UTF-8")))) {
            while ((line = br.readLine()) != null) {
                _configEntries.add(line);
            }
        } finally {
            if (_configEntries.isEmpty()
                    || !_configEntries.get(0).equals("[OPTIONS]")) {
                throw new ConfigErrorException();
            }
        }
    }

    /**
     * Check an entry for true or false, if it stores {@code boolean} values
     *
     * @param prefix The prefix of which to check value
     * @return True or false, depending on value
     * @throws ConfigErrorException If prefix does not exist or if value of
     * prefix is not a {@code boolean}
     */
    public boolean checkValue(String prefix) throws ConfigErrorException {
        for (String entry : _configEntries) {
            if (entry.startsWith(prefix)) {
                if (entry.endsWith("=true")) {
                    return true;
                } else if (entry.endsWith("=false")) {
                    return false;
                }
            }
        }
        throw new ConfigErrorException();
    }

    /**
     * Returns the {@code String} entry of the given prefix
     *
     * @param prefix The prefix of which to get full entry
     * @return The entry of the given prefix
     * @throws gzipper.exceptions.ConfigErrorException If prefix does not exist
     */
    public String getValue(String prefix) throws ConfigErrorException {
        for (String entry : _configEntries) {
            if (entry.startsWith(prefix)) {
                return makeToken(entry.substring(entry.indexOf('=') + 1));
            }
        }
        throw new ConfigErrorException();
    }

    /**
     * Updates configuration file on change of settings
     *
     * @param prefix The prefix of which value will be updated
     * @param value The new value to be set, respectively updated
     */
    public void updateValue(String prefix, String value) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(_settingsPath), Charset.forName("UTF-8")))) {

            for (String entry : _configEntries) {
                if (entry.startsWith(prefix)) {
                    bw.write(prefix + "=\"" + value + "\"");
                } else {
                    bw.write(entry);
                }
                bw.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE,
                    "Error writing configuration file", ex);
        }
    }

    /**
     * Updates configuration file on change of settings
     *
     * @param prefix The prefix of which value will be updated
     * @param value The new value to be set, respectively updated
     */
    public void updateValue(String prefix, boolean value) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(_settingsPath), Charset.forName("UTF-8")))) {

            for (String entry : _configEntries) {
                if (entry.startsWith(prefix)) {
                    if (value) {
                        bw.write(prefix + "=true");
                    } else {
                        bw.write(prefix + "=false");
                    }
                } else {
                    bw.write(entry);
                }
                bw.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE,
                    "Error writing configuration file", ex);
        }
    }

    /**
     * Removes any quotes and whitespace from a {@code String}
     *
     * @param value The {@code String} value to be converted into a token
     * @return The new {@code String} value that is a token
     */
    private String makeToken(String value) {
        String tokenizedString = "";
        for (int i = 0; i < value.length(); ++i) {
            if (value.charAt(i) != '"') {
                tokenizedString += value.charAt(i);
            }
        }
        return tokenizedString.trim();
    }
}
