/*
 * Copyright (C) 2017 Matthias Fussenegger
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
package org.gzipper.java.presentation.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.gzipper.java.application.util.AppUtils;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.presentation.GZipper;
import org.gzipper.java.presentation.style.CSS;
import org.gzipper.java.util.Log;

/**
 * Controller for the FXML named "AboutView.fxml".
 *
 * @author Matthias Fussenegger
 */
public class AboutViewController extends BaseController {

    /**
     * The name of the image to be displayed in the about view.
     */
    private final static String IMG_NAME = "images/icon_256.png";

    /**
     * The image file as a static reference in case it has already been loaded.
     */
    private static File _imageFile;

    /**
     * The name of this application.
     */
    private final String _appName = "GZipper";

    /**
     * The version of this application.
     */
    private final String _appVersion = "0.5.13 BETA";

    /**
     * The build date of this application.
     */
    private final String _appBuildDate = "10/04/2017";

    /**
     * The author of this application.
     */
    private final String _appCopyright = "Matthias Fussenegger";

    /**
     * The home page of this project.
     */
    private final String _appHomePage = "https://github.com/turbolocust/GZipper";

    @FXML
    private ImageView _imageView;

    @FXML
    private TextFlow _textFlow;

    @FXML
    private Button _closeButton;

    /**
     * Constructs a controller for About View with the specified CSS theme and
     * host services.
     *
     * @param theme the {@link CSS} theme to apply.
     * @param hostServices the host services to aggregate.
     */
    public AboutViewController(CSS.Theme theme, HostServices hostServices) {
        super(theme, hostServices);
    }

    @FXML
    void handleCloseButtonAction(ActionEvent evt) {
        if (evt.getSource().equals(_closeButton)) {
            close();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (_imageFile == null || !_imageFile.exists()) {
            String imgRes = null;
            try {
                // load image from JAR and display it in image view
                imgRes = AppUtils.getResource(GZipper.class, "/" + IMG_NAME);
            } catch (URISyntaxException ex) {
                Log.e(I18N.getString("error.text"), ex);
                try {
                    imgRes = AppUtils.getDecodedRootPath(getClass()) + IMG_NAME;
                } catch (UnsupportedEncodingException ex1) {
                    Log.e(I18N.getString("error.text"), ex1);
                }
            } finally {
                if (imgRes != null) {
                    _imageFile = new File(imgRes);
                }
            }
        }

        _imageView.setImage(new Image(_imageFile.toURI().toString()));

        final Text appName = new Text(_appName + "\n"),
                appVersion = new Text(
                        "Version"
                        + ": "
                        + _appVersion
                        + "\n"),
                appBuildDate = new Text(
                        resources.getString("buildDate.text")
                        + ": "
                        + _appBuildDate
                        + "\n"),
                appCopyright = new Text(
                        resources.getString("author.text")
                        + ": "
                        + _appCopyright
                        + "\n\r"),
                appLicense = new Text(
                        resources.getString("license.text")
                        + "\n\r");

        final Hyperlink appHomePage = new Hyperlink(_appHomePage);
        appHomePage.setId("aboutViewAppHomePage");
        appHomePage.setOnAction((ActionEvent evt) -> {
            if (evt.getSource().equals(appHomePage)) {
                _hostServices.showDocument(appHomePage.getText());
            }
        });

        // apply different font to app name
        appName.setFont(Font.font("System", FontWeight.BOLD, 16));

        _textFlow.setTextAlignment(TextAlignment.CENTER);
        _textFlow.getChildren().addAll(appName, appVersion,
                appBuildDate, appCopyright, appLicense, appHomePage);
    }
}
