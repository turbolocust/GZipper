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

import org.gzipper.java.application.util.StringUtils;
import org.gzipper.java.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Matthias Fussenegger
 */
public class MessageDigestProvider {

    private final MessageDigest _messageDigest;

    public MessageDigestProvider(MessageDigest messageDigest) {
        _messageDigest = messageDigest;
    }

    /**
     * Factory method to create a new instance of {@link MessageDigestProvider}.
     *
     * @param algorithm the algorithm for the {@link MessageDigest}.
     * @return a new instance of {@link MessageDigestProvider}.
     * @throws NoSuchAlgorithmException if the specified algorithm (its name)
     *                                  does not exist.
     */
    public static MessageDigestProvider createProvider(
            MessageDigestAlgorithm algorithm) throws NoSuchAlgorithmException {
        String name = algorithm.getAlgorithmName();
        MessageDigest msgDigest = MessageDigest.getInstance(name);
        return new MessageDigestProvider(msgDigest);
    }

    /**
     * Returns the algorithm name of the aggregated {@link MessageDigest}.
     *
     * @return the algorithm name of the aggregated {@link MessageDigest}.
     */
    public String getAlgorithmName() {
        return _messageDigest.getAlgorithm();
    }

    /**
     * Computes a hash from the current state of the digest of the aggregated
     * {@link MessageDigest} and returns the result wrapped in
     * {@link MessageDigestResult} together with its hexadecimal representation.
     *
     * @return result object which holds the computed values.
     */
    public MessageDigestResult computeHash() {
        final byte[] result = _messageDigest.digest();
        return new MessageDigestResult(result, convertToHex(result));
    }

    /**
     * Computes and then returns the hash value of the specified bytes using the
     * specified message digest algorithm. If the specified algorithm, for
     * whatever reason, does not exist, an empty result is returned.
     *
     * @param bytes the bytes to be processed.
     * @param algo  the algorithm to be used.
     * @return result object which holds the computed values.
     */
    public static MessageDigestResult computeHash(byte[] bytes, MessageDigestAlgorithm algo) {
        try {
            final String name = algo.getAlgorithmName();
            final MessageDigest msgDigest = MessageDigest.getInstance(name);
            byte[] result = msgDigest.digest(bytes);
            return new MessageDigestResult(result, convertToHex(result));
        } catch (NoSuchAlgorithmException ex) {
            Log.e("Specified message digest algorithm does not exist", ex);
        }
        // return an empty result if something went wrong
        return new MessageDigestResult(new byte[0], StringUtils.EMPTY);
    }

    /**
     * Updates the digest of the aggregated {@link MessageDigest}.
     *
     * @param bytes  the bytes to be processed.
     * @param offset starting index in array.
     * @param length the length to be processed, starting at {@code offset}.
     */
    public void updateHash(byte[] bytes, int offset, int length) {
        _messageDigest.update(bytes, offset, length);
    }

    /**
     * Resets the aggregated {@link MessageDigest} for further use.
     */
    public void reset() {
        _messageDigest.reset();
    }

    private static String convertToHex(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String formatted = String.format("%02X", b);
            sb.append(formatted);
        }
        return sb.toString();
    }
}
