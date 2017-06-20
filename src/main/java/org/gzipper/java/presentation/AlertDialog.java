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
package org.gzipper.java.presentation;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import org.gzipper.java.style.CSS;

/**
 * Convenience class that offers the creation of alert dialogs.
 *
 * @author Matthias Fussenegger
 */
public class AlertDialog {

    /**
     * Brings up a dialog using the specified parameters.
     *
     * @param type the type of the alert.
     * @param title the title of the dialog.
     * @param header the header text of the dialog
     * @param content the content text of the dialog
     * @param theme the CSS theme to be applied.
     * @param buttonTypes the buttons to be added.
     * @return an {@link Optional} to indicate which button has been pressed
     */
    public static Optional<ButtonType> showDialog(AlertType type, String title,
            String header, String content, CSS.Theme theme, ButtonType... buttonTypes) {
        final Alert alert = new Alert(type, content, buttonTypes);
        alert.setTitle(title);
        alert.setHeaderText(header);
        CSS.load(theme, alert.getDialogPane().getScene().getStylesheets());
        return alert.showAndWait();
    }

    /**
     * Brings up a confirmation dialog using the specified parameters.
     *
     * @param title the title of the dialog.
     * @param header the header text of the dialog
     * @param content the content text of the dialog
     * @param theme the CSS theme to be applied.
     * @return an {@link Optional} to indicate which button has been pressed
     */
    public static Optional<ButtonType> showConfirmationDialog(String title,
            String header, String content, CSS.Theme theme) {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content,
                ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(header);
        CSS.load(theme, alert.getDialogPane().getScene().getStylesheets());
        // changing default button to {@code ButtonType.NO} to avoid accidential press of return key
        Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
        Button noButton = (Button) alert.getDialogPane().lookupButton(ButtonType.NO);
        yesButton.setDefaultButton(false);
        noButton.setDefaultButton(true);
        return alert.showAndWait();
    }
}
