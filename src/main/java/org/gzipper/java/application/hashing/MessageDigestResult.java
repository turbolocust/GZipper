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

import java.util.Arrays;
import java.util.Objects;

/**
 * Holds result values of message digest algorithms.
 *
 * @author Matthias Fussenegger
 */
public final class MessageDigestResult {

    private final byte[] _hashedBytes;

    private final String _hashedValue;

    public MessageDigestResult(byte[] hashedBytes, String hashedValue) {
        _hashedBytes = hashedBytes;
        _hashedValue = hashedValue;
    }

    /**
     * Returns the computed hash as an array of bytes.
     *
     * @return the computed hash as an array of bytes.
     */
    public final byte[] getHashedBytes() {
        return _hashedBytes;
    }

    /**
     * Returns the hexadecimal representation of the hash value.
     *
     * @return the hexadecimal representation of the hash value.
     */
    public final String getHashedValue() {
        return _hashedValue;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Arrays.hashCode(_hashedBytes);
        hash = 61 * hash + Objects.hashCode(_hashedValue);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MessageDigestResult)) {
            return false;
        }
        final MessageDigestResult other = (MessageDigestResult) obj;
        if (!Objects.equals(_hashedValue, other._hashedValue)) {
            return false;
        }
        return Arrays.equals(_hashedBytes, other._hashedBytes);
    }

    @Override
    public String toString() {
        return getHashedValue();
    }
}
