/*
 * Copyright (C) 2016 Matthias Fussenegger
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
package gzipper.graphics;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * A class that offers the functionality of creating message dialogs
 *
 * @author Matthias Fussenegger
 */
public class MessageBox extends JFrame {
    
    private static final long serialVersionUID = 1L;

    /**
     * Constructor of this class that extends {@link JFrame}
     */
    private MessageBox() {
        super();
        initFrame();
    }

    /**
     * Initializes this frame with pre-defined properties
     */
    private void initFrame() {
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Brings up an information-message dialog titled "Message"
     *
     * @param message The {@link Object} to display
     */
    public static void showDefaultMessage(Object message) {
        JOptionPane.showMessageDialog(new MessageBox(), message);
    }

    /**
     * Brings up a warning-message dialog titled "Warning"
     *
     * @param message The {@link Object} to display
     */
    public static void showWarningMessage(Object message) {
        JOptionPane.showMessageDialog(new MessageBox(), message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Brings up an error-message dialog titled "Error"
     *
     * @param message The {@link Object} to display
     */
    public static void showErrorMessage(Object message) {
        JOptionPane.showMessageDialog(new MessageBox(), message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
