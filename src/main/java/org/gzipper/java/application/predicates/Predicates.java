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
package org.gzipper.java.application.predicates;

import java.util.function.Predicate;

/**
 *
 * @author Matthias Fussenegger
 */
public final class Predicates {

    private Predicates() {
        throw new AssertionError("Holds static members only.");
    }

    /**
     * Creates a new {@link Predicate} which always evaluates to <b>true</b>.
     *
     * @param <T> the type of the object that is to be consumed by the predicate
     * and thus the type of the input to the predicate.
     * @return a new instance of {@link Predicate} with the specified type.
     */
    public static final <T> Predicate<T> createAlwaysTrue() {
        return p -> true;
    }

    /**
     * Creates a new {@link Predicate} which always evaluates to <b>false</b>.
     *
     * @param <T> the type of the object that is to be consumed by the predicate
     * and thus the type of the input to the predicate.
     * @return a new instance of {@link Predicate} with the specified type.
     */
    public static final <T> Predicate<T> createAlwaysFalse() {
        return p -> false;
    }
}
