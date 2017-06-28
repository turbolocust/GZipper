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
package org.gzipper.java.application.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Paths;
import org.gzipper.java.util.Log;

/**
 * Utility class that provides methods for e.g. receiving resources from the
 * resource path of a class.
 *
 * @author Matthias Fussenegger
 */
public final class AppUtils {

    /**
     * Returns the resource path of the specified class as string. The file will
     * be stored in the system's temporary folder with the file extension
     * <b>.tmp</b>. The file will be deleted on JVM termination.
     *
     * @param clazz the class of which to receive the resource path.
     * @param name the name of the resource to receive.
     * @return the resource path of the specified class.
     * @throws URISyntaxException if URL conversion failed.
     */
    public static String getResource(Class<?> clazz, String name) throws URISyntaxException {

        String resource = null;

        final URL url = clazz.getResource(name);
        if (url.toString().startsWith("jar:")) {

            String tempName = name.substring(name.lastIndexOf('/') + 1, name.length());

            try (BufferedInputStream bis = new BufferedInputStream(clazz.getResourceAsStream(name))) {

                File file = File.createTempFile(tempName, ".tmp");
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {

                    int readBytes;
                    byte[] bytes = new byte[1024];

                    while ((readBytes = bis.read(bytes)) != -1) {
                        bos.write(bytes, 0, readBytes);
                    }
                }

                resource = file.getPath();
                file.deleteOnExit();

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
     * @throws UnsupportedEncodingException if encoding is not supported.
     */
    public static String getDecodedRootPath(Class<?> clazz) throws UnsupportedEncodingException {

        String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();

        final File jarFile = new File(path);
        final int cutLength = determineCutLength(jarFile);

        String decPath; //to hold decoded path of JAR-file

        if (System.getProperty("os.name").startsWith("Windows")) {
            decPath = URLDecoder.decode(path.substring(
                    1, path.length() - cutLength), "UTF-8");
        } else {
            decPath = URLDecoder.decode(path.substring(
                    0, path.length() - cutLength), "UTF-8");
        }

        return decPath;
    }

    /**
     * Determines the cut length to get the application directory.
     *
     * @param f the file to get cut length for.
     * @return the cut length, that is the name of the executable or the folder.
     */
    private static int determineCutLength(File f) {
        int cutLength = 0;
        if (!f.isDirectory()) {
            cutLength = f.getName().length();
        }
        return cutLength;
    }
}
