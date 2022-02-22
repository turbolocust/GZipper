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
package org.gzipper.java.presentation.model;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author Matthias Fussenegger
 */
public final class HashViewTableModel {

    private final ReadOnlyStringWrapper _fileName;
    private final ReadOnlyStringWrapper _filePath;
    private final ReadOnlyStringWrapper _hashValue;

    private final SimpleStringProperty _hashValueProperty;

    public HashViewTableModel(String fileName, String filePath, String hashValue) {
        _fileName = new ReadOnlyStringWrapper(fileName);
        _filePath = new ReadOnlyStringWrapper(filePath);
        _hashValue = new ReadOnlyStringWrapper();
        // bind read/write property to allow update of hash value
        // e.g. in case of lower case conversion triggered in UI
        _hashValueProperty = new SimpleStringProperty(hashValue);
        _hashValue.bind(_hashValueProperty);
    }

    public String getFileName() {
        return _fileName.get();
    }

    public String getFilePath() {
        return _filePath.get();
    }

    public String getHashValue() {
        return _hashValue.get();
    }

    public void setHashValue(String value) {
        _hashValueProperty.setValue(value);
    }

    public ReadOnlyStringWrapper fileNameProperty() {
        return _fileName;
    }

    public ReadOnlyStringWrapper filePathProperty() {
        return _filePath;
    }

    public ReadOnlyStringWrapper hashValueProperty() {
        return _hashValue;
    }

    @Override
    public String toString() {
        return "HashViewTableModel{"
                + "_fileName=" + _fileName
                + ", _filePath=" + _filePath
                + ", _hashValue=" + _hashValue + '}';
    }
}
