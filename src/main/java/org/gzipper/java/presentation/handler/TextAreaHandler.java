/*
 * Copyright (C) 2016 Matthias Fussenegger
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
package org.gzipper.java.presentation.handler;

import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import javafx.scene.control.TextArea;

/**
 *
 * @author Matthias Fussenegger
 */
public class TextAreaHandler extends StreamHandler {

    /**
     * The aggregated {@link TextArea} which will display any record.
     */
    private final TextArea _textArea;

    /**
     * Constructs a new handler for displaying log messages in a text area.
     *
     * @param textArea the text area which will display the log messages.
     */
    public TextAreaHandler(TextArea textArea) {
        _textArea = textArea;
    }

    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        flush();

        if (_textArea != null) {
            _textArea.appendText(getFormatter().format(record));
        }
    }

}
