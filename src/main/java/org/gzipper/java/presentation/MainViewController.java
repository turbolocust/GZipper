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
package org.gzipper.java.presentation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gzipper.java.application.model.OperatingSystem;
import org.gzipper.java.application.model.Unix;
import org.gzipper.java.application.model.Windows;
import org.gzipper.java.application.util.AppUtil;
import org.gzipper.java.application.util.Settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;

/**
 *
 * @author Matthias Fussenegger
 */
public class MainViewController extends BaseController implements Initializable {

	@FXML
	private MenuItem _closeMenuItem;

	@FXML
	private MenuItem _deleteMenuItem;

	@FXML
	private TextArea _textArea;

	@FXML
	private TextField _outputPath;

	@FXML
	private ComboBox<String> _archiveTypeComboBox;

	@FXML
	private void handleCloseMenuItemAction(ActionEvent evt) {
		if (evt.getSource().equals(_closeMenuItem)) {
			_primaryStage.close();
			System.exit(0);
		}
	}

	@FXML
	private void handleDeleteMenuItemAction(ActionEvent evt) {
		if (evt.getSource().equals(_deleteMenuItem)) {
			Optional<ButtonType> result = AlertDialog.showConfirmationDialog(
					_resources.getString("clearTextWarning.text"), _resources.getString("confirmation.text"));
			if (result.isPresent() && result.get() == ButtonType.YES) {
				_textArea.clear();
				_textArea.setText("run:\n");
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		try {

			final String decPath = AppUtil.getDecodedRootPath(GZipper.class);
			OperatingSystem os; // to determine the users operating system

			String settingsFile;
			try { // locate settings file
				settingsFile = AppUtil.getResource(GZipper.class, "/settings.properties");
			} catch (URISyntaxException ex) {
				Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
				settingsFile = decPath + "settings.properties";
			}

			if (System.getProperty("os.name").startsWith("Windows")) {
				os = new Windows();
			} else {
				os = new Unix();
			}

			_settings = new Settings(settingsFile, os);

		} catch (IOException ex) {
			Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
		}

		_resources = resources;
		_frameImage = new Image("/images/icon_32.png");

		_textArea.setText("run:\n" + _resources.getString("changeOutputPath.text") + "\n");
	}
}
