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
package org.gzipper.java.presentation;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

/**
 * Convenience class that offers the creation of alert dialogs.
 *
 * @author Matthias Fussenegger
 */
public class AlertDialog {

    /**
     * Brings up a confirmation dialog titled "Please confirm".
     *
     * @param header The header text of the dialog
     * @param content The content text of the dialog
     * @return An {@link Optional} to indicate which button has been pressed
     */
    public static Optional<ButtonType> showConfirmationDialog(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO);
        alert.setTitle("Please confirm");
        alert.setHeaderText(header);
        // changing default button to {@code ButtonType.NO} to avoid accidential press of return key
        Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
        Button noButton = (Button) alert.getDialogPane().lookupButton(ButtonType.NO);
        yesButton.setDefaultButton(false);
        noButton.setDefaultButton(true);
        return alert.showAndWait();
    }

    /**
     * Brings up a warning dialog titled "Warning".
     *
     * @param header The header text of the dialog
     * @param content The content text of the dialog
     * @return An {@link Optional} to indicate which button has been pressed
     */
    public static Optional<ButtonType> showWarningDialog(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING, content);
        alert.setTitle("Warning");
        alert.setHeaderText(header);
        return alert.showAndWait();
    }

    /**
     * Brings up an error dialog titled "Error".
     *
     * @param header The header text of the dialog
     * @param content The content text of the dialog
     * @return An {@link Optional} to indicate which button has been pressed
     */
    public static Optional<ButtonType> showErrorDialog(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        return alert.showAndWait();
    }

}
