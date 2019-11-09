/*
 * Copyright (C) 2019 Matthias Fussenegger
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;
import org.gzipper.java.application.algorithm.TestUtils.TestObject;
import org.gzipper.java.application.algorithm.type.*;
import org.gzipper.java.application.model.OperatingSystem;
import org.gzipper.java.application.util.FileUtils;
import org.gzipper.java.util.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test of {@link CompressionAlgorithm} interface and its realizations.
 *
 * @author Matthias Fussenegger
 */
public class CompressionAlgorithmTest {

    private String _archiveFileNamePrefix;

    private String _testFileNamePrefix;

    private final String _tempDirectory;

    public CompressionAlgorithmTest() throws IOException {
        File temp = File.createTempFile("gzipper_temp_file", null);
        _tempDirectory = temp.getAbsoluteFile().getParent();
    }

    @BeforeClass
    public static void setUpClass() {
        OperatingSystem os = TestUtils.getOperatingSystem();
        Settings.getInstance().init(null, os);
    }

    @Before
    public void setUp() {
        final LocalTime time = LocalTime.now();
        // define name of test archive
        _archiveFileNamePrefix = "gzipper_aa_test_archive-"
                + LocalDate.now() + "-" + time.getNano();
        _archiveFileNamePrefix = _archiveFileNamePrefix.replaceAll(":", "_");
        assertFalse(FileUtils.containsIllegalChars(_archiveFileNamePrefix));
        // define name of test file (to be archived)
        _testFileNamePrefix = "gzipper_aa_test_file-"
                + LocalDate.now() + "-" + time.getNano();
        _testFileNamePrefix = _testFileNamePrefix.replaceAll(":", "_");
        assertFalse(FileUtils.containsIllegalChars(_testFileNamePrefix));
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of compress and extract method, of class ArchivingAlgorithm.
     */
    @Test
    public void testZipCompressExtract() {
        try {
            System.out.println("ZIP test");
            testCompressionExtraction(new Zip(), ".zip");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void testJarCompressExtract() {
        try {
            System.out.println("JAR test");
            testCompressionExtraction(new Jar(), ".jar");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void testGzipCompressExtract() {
        try {
            System.out.println("GZIP test");
            testCompressionExtraction(new Gzip(), ".gzip");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void testTarCompressExtract() {
        try {
            System.out.println("TAR test");
            testCompressionExtraction(new Tar(), ".tar");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void testTarGzCompressExtract() {
        try {
            System.out.println("TAR+GZ test");
            testCompressionExtraction(new TarGzip(), ".tgz");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void testTarBzip2CompressExtract() {
        try {
            System.out.println("TAR+BZIP2 test");
            testCompressionExtraction(new TarBzip2(), ".tbz2");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void testTarLzmaCompressExtract() {
        try {
            System.out.println("TAR+LZMA test");
            testCompressionExtraction(new TarLzma(), ".tlz");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void testTarXzCompressExtract() {
        try {
            System.out.println("TAR+XZ test");
            testCompressionExtraction(new TarXz(), ".txz");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    private void testCompressionExtraction(
            CompressionAlgorithm instance, String suffix) throws Exception {

        final String location = _tempDirectory;
        final String name = _archiveFileNamePrefix + suffix;
        final String filename = FileUtils.generateUniqueFilename(
                location, _archiveFileNamePrefix, suffix, 0);

        final TestObject testObj = TestUtils
                .generateTestObject(_tempDirectory, _testFileNamePrefix);

        final File testFile = testObj._testFile;
        final File archiveFile = new File(filename);

        try {
            System.out.println("compress");

            final File[] files = {testFile};
            instance.compress(files, location, name);

            // build output file location
            final StringBuilder sb = new StringBuilder(location);
            if (!location.endsWith(File.separator)) {
                sb.append(File.separator);
            }

            System.out.println("extract");
            instance.extract(sb.toString(), filename);

            // build output file name
            File outputFolder = null;
            if (!(instance instanceof Gzip)) { // gzip does not create output folder
                sb.append(_archiveFileNamePrefix);
                outputFolder = new File(sb.toString());
                assertTrue(outputFolder.exists());
                sb.append(File.separator);
            }

            sb.append(testFile.getName());
            final File extractedFile = new File(sb.toString());
            assertTrue(extractedFile.exists());

            try {
                // validate extracted file
                final Iterator<String> iter = testObj._fileContent.linesIterator();
                try (FileReader fr = new FileReader(extractedFile);
                        BufferedReader reader = new BufferedReader(fr)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        assertTrue(iter.hasNext());
                        assertEquals(line, iter.next());
                    }
                }
                System.out.println("Test successful.");
            }
            finally {
                extractedFile.delete();
                if (outputFolder != null) {
                    outputFolder.delete();
                }
            }
        }
        finally {
            testFile.delete();
            archiveFile.delete();
        }
    }
}
