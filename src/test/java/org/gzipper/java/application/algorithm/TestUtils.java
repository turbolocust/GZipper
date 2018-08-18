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
package org.gzipper.java.application.algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.gzipper.java.application.model.OS;
import org.gzipper.java.application.model.OperatingSystem;
import org.gzipper.java.application.util.FileUtils;

/**
 *
 * @author Matthias Fussenegger
 */
public final class TestUtils {

    private static final int NUMBER_OF_LINES = 32;

    private static final int LINE_LENGTH = 256;

    static String generateRandomString(int length) {
        final StringBuilder sb = new StringBuilder(length);
        final Random rand = new Random();
        for (int i = 0; i < length; ++i) {
            char c = (char) (rand.nextInt(126 - 32) + 32);
            sb.append(Character.toString(c));
        }
        return sb.toString();
    }

    static TestObject generateTestObject(String dir, String fname) throws IOException {

        String filename = FileUtils.combine(dir, fname);
        File file = File.createTempFile(filename, null);

        Document document = new Document();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < NUMBER_OF_LINES; ++i) {
                String line = TestUtils.generateRandomString(LINE_LENGTH);
                writer.append(line);
                document.appendLine(line);
                writer.newLine();
                writer.flush();
            }
        }

        return new TestObject(file, document);
    }

    static OperatingSystem getOperatingSystem() {
        return System.getProperty("os.name")
                .toLowerCase().startsWith("windows")
                ? new OperatingSystem(OS.WINDOWS)
                : new OperatingSystem(OS.UNIX);
    }

    static class Document {

        final List<String> _lines;

        private Document() {
            _lines = new ArrayList<>();
        }

        void appendLine(String line) {
            _lines.add(line);
        }

        Iterator<String> linesIterator() {
            return _lines.iterator();
        }
    }

    static class TestObject {

        final File _testFile;

        final Document _fileContent;

        private TestObject(File testFile, Document fileContent) {
            _testFile = testFile;
            _fileContent = fileContent;
        }
    }
}
