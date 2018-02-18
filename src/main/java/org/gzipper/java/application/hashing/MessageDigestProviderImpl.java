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
package org.gzipper.java.application.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.gzipper.java.application.util.StringUtils;
import org.gzipper.java.util.Log;

/**
 *
 * @author Matthias Fussenegger
 */
public class MessageDigestProviderImpl implements MessageDigestProvider {

    @Override
    public MessageDigestResult computeHash(byte[] bytes, MessageDigestAlgorithm algo) {
        try {
            final String name = algo.getAlgorithmName();
            final MessageDigest msgDigest = MessageDigest.getInstance(name);
            byte[] result = msgDigest.digest(bytes);
            return new MessageDigestResult(result, convertToHex(result));
        }
        catch (NoSuchAlgorithmException ex) {
            Log.e("Specified message digest algorithm does not exist.", ex);
        }

        // return an empty result if something went wrong
        return new MessageDigestResult(new byte[0], StringUtils.EMPTY);
    }

    private String convertToHex(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String formatted = String.format("%02X", b);
            sb.append(formatted);
        }
        return sb.toString();
    }
}
