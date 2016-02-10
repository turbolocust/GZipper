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
package Operations;

import Exceptions.ConfigErrorException;
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
     * @throws Exceptions.ConfigErrorException If configuration file is corrupt
     */
    public ConfigFileParser(String rootPath) throws IOException, ConfigErrorException {
        _configEntries = new ArrayList<>();
        _settingsPath = rootPath + "gzipper.ini";
        parseLoggerConfig();
    }

    /**
     * Parses the configuration file and stores all entries in list
     *
     * @throws IOException If an error while reading configuration file occurs
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
     * Check an entry for true or false, if it stores Boolean values
     *
     * @param prefix The prefix of which to check value
     * @return True or False, depending on value
     * @throws ConfigErrorException If prefix does not exist or is no Boolean
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
     * Returns the full entry of the given prefix
     *
     * @param prefix The prefix of which to get full entry
     * @return The full entry of the given prefix
     * @throws Exceptions.ConfigErrorException If prefix does not exist
     */
    public String getValue(String prefix) throws ConfigErrorException {
        for (String entry : _configEntries) {
            if (entry.startsWith(prefix)) {
                return entry;
            }
        }
        throw new ConfigErrorException();
    }

    /**
     * Updates configuration file on change of settings
     *
     * @param prefix The prefix of which value will be updated
     * @param value The new value to be set, respectively updated
     * @throws IOException If an error writing configuration file occurred
     */
    public void updateValue(String prefix, String value) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(_settingsPath), Charset.forName("UTF-8")))) {

            for (String entry : _configEntries) {
                if (entry.startsWith(prefix)) {
                    bw.write(prefix + value);
                } else {
                    bw.write(entry);
                }
                bw.newLine();
            }
        }
    }

    /**
     * Updates configuration file on change of settings
     *
     * @param prefix The prefix of which value will be updated
     * @param value The new value to be set, respectively updated
     * @throws IOException If an error writing configuration file occurred
     */
    public void updateValue(String prefix, boolean value) throws IOException {
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
        }
    }
}
