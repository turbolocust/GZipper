/*
 * Copyright (C) 2019 Matthias Fussenegger
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

import org.gzipper.java.util.Log;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * Utility class that provides application-specific methods for e.g. receiving
 * resources from the resource path of a class.
 *
 * @author Matthias Fussenegger
 */
public final class AppUtils {

    private static final String JAVA_VERSION = determineJavaVersion();

    private AppUtils() {
        throw new AssertionError("Holds static members only");
    }

    private static String determineJavaVersion() {

        String version = System.getProperty("java.version");

        int pos = version.indexOf('.');
        if (pos != -1) { // found
            pos = version.indexOf('.', pos + 1);
            if (pos != -1) { // found
                version = version.substring(0, pos);
            }
        }

        return version;
    }

    private static String createTemporaryFile(BufferedInputStream bis, String tempName) throws IOException {
        final File file = File.createTempFile(tempName, ".tmp");
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {

            int readBytes;
            byte[] bytes = new byte[1024];

            while ((readBytes = bis.read(bytes)) != -1) {
                bos.write(bytes, 0, readBytes);
            }
        }

        file.deleteOnExit();
        return file.getPath();
    }

    /**
     * Determines the current Java version and returns the major version of Java
     * as string. For e.g. Java 8, this would return {@code 1.8}.
     *
     * @return the Java major version as string.
     */
    public static String getJavaVersion() {
        return JAVA_VERSION;
    }

    /**
     * Returns the resource path of the specified class as string. The file will
     * be stored in the system's temporary folder with the file extension
     * <b>.tmp</b>. The file will be deleted on JVM termination.
     *
     * @param clazz the class of which to receive the resource path.
     * @param name  the name of the resource to receive.
     * @return the resource path of the specified class.
     * @throws URISyntaxException if URL conversion failed.
     */
    public static String getResource(Class<?> clazz, String name) throws URISyntaxException, FileNotFoundException {

        String resource = null;
        final URL url = clazz.getResource(name);

        if (url == null) {
            throw new FileNotFoundException("Resource not found: " + name);
        }

        if (url.toString().startsWith("jar:")) {

            String tempName = name.substring(name.lastIndexOf('/') + 1);
            var resourceStream = clazz.getResourceAsStream(name);

            if (resourceStream == null) {
                throw new FileNotFoundException("Resource not found: " + name);
            }

            try (BufferedInputStream bis = new BufferedInputStream(resourceStream)) {
                resource = createTemporaryFile(bis, tempName);
            } catch (IOException ex) {
                Log.e(ex.getLocalizedMessage(), ex);
            }
        } else {
            resource = Paths.get(url.toURI()).toString();
        }

        return resource;
    }

    /**
     * Returns the decoded root path of the application's JAR-file.
     *
     * @param clazz the class of which to receive the root path.
     * @return the decoded root path of the JAR-file.
     */
    public static String getDecodedRootPath(Class<?> clazz) {

        String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();

        final File jarFile = new File(path);
        final int cutLength = determineCutLength(jarFile);

        String decPath; // to hold decoded path of JAR-file

        if (System.getProperty("os.name").startsWith("Windows")) {
            decPath = URLDecoder.decode(path.substring(1, path.length() - cutLength), StandardCharsets.UTF_8);
        } else {
            decPath = URLDecoder.decode(path.substring(0, path.length() - cutLength), StandardCharsets.UTF_8);
        }

        return decPath;
    }

    private static int determineCutLength(File f) {
        int cutLength = 0;
        if (!f.isDirectory()) {
            cutLength = f.getName().length();
        }
        return cutLength;
    }
}
