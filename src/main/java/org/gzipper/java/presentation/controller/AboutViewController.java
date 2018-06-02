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
package org.gzipper.java.presentation.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import org.gzipper.java.application.util.AppUtils;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.presentation.CSS;
import org.gzipper.java.presentation.GZipper;
import org.gzipper.java.util.Log;

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

/**
 * Controller for the FXML named "AboutView.fxml".
 *
 * @author Matthias Fussenegger
 */
public final class AboutViewController extends BaseController {

    /**
     * The name of the image to be displayed in the about view.
     */
    private static final String IMG_NAME = "images/icon_256.png";

    /**
     * The name of this application.
     */
    private static final String APP_NAME = "GZipper";

    /**
     * The version of this application.
     */
    private static final String APP_VERSION = "0.5.24 BETA";

    /**
     * The build date of this application.
     */
    private static final String APP_BUILD_DATE = "06/02/2018";

    /**
     * The author of this application.
     */
    private static final String APP_COPYRIGHT = "Matthias Fussenegger";

    /**
     * The home page of this project.
     */
    private static final String APP_HOME_PAGE = "https://github.com/turbolocust/GZipper";

    /**
     * The image file as a static reference in case it has already been loaded.
     */
    private static File _imageFile;

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
     * @param theme the {@link CSS} theme to be applied.
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
            }
            catch (URISyntaxException ex) {
                Log.e(I18N.getString("error.text"), ex);
                try {
                    imgRes = AppUtils.getDecodedRootPath(getClass()) + IMG_NAME;
                }
                catch (UnsupportedEncodingException ex1) {
                    Log.e(I18N.getString("error.text"), ex1);
                }
            }
            finally {
                if (imgRes != null) {
                    _imageFile = new File(imgRes);
                    _imageView.setImage(new Image(_imageFile.toURI().toString()));
                }
            }
        }

        final Text appName = new Text(APP_NAME + "\n"),
                appVersion = new Text(
                        "Version"
                        + ": "
                        + APP_VERSION
                        + "\n"),
                appBuildDate = new Text(
                        resources.getString("buildDate.text")
                        + ": "
                        + APP_BUILD_DATE
                        + "\n"),
                appCopyright = new Text(
                        resources.getString("author.text")
                        + ": "
                        + APP_COPYRIGHT
                        + "\n\r"),
                appLicense = new Text(
                        resources.getString("license.text")
                        + "\n\r");

        final Hyperlink appHomePage = new Hyperlink(APP_HOME_PAGE);
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
