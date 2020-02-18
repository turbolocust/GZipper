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
import java.util.regex.Pattern;

/**
 * Simple filter predicate which uses a regular expression to test a string
 * against a {@link Pattern}.
 *
 * @author Matthias Fussenegger
 */
public final class PatternPredicate implements Predicate<String> {

    private final Pattern _pattern;

    public PatternPredicate(Pattern pattern) {
        _pattern = pattern;
    }

    @Override
    public boolean test(String t) {
        return _pattern.matcher(t).find();
    }
}
