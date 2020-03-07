/*
 * Copyright (C) 2020 Matthias Fussenegger
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

import java.util.Objects;

/**
 * Aggregates a {@link MessageDigestResult} and holds a name attribute.
 *
 * @author Matthias Fussenegger
 */
public final class NamedMessageDigestResult {

    private final MessageDigestResult _messageDigestResult;

    private final String _name;

    public NamedMessageDigestResult(MessageDigestResult result, String name) {
        _messageDigestResult = result;
        _name = name;
    }

    /**
     * Returns the aggregated {@link MessageDigestResult}.
     *
     * @return the aggregated {@link MessageDigestResult}.
     */
    public MessageDigestResult getMessageDigestResult() {
        return _messageDigestResult;
    }

    /**
     * Returns the name that was set to this instance.
     *
     * @return the name that was set to this instance.
     */
    public String getName() {
        return _name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(_messageDigestResult);
        hash = 79 * hash + Objects.hashCode(_name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final NamedMessageDigestResult other = (NamedMessageDigestResult) obj;
        if (!Objects.equals(_name, other._name)) {
            return false;
        }

        return Objects.equals(_messageDigestResult, other._messageDigestResult);
    }

    @Override
    public String toString() {
        return _messageDigestResult.toString() + ":" + _name;
    }
}
