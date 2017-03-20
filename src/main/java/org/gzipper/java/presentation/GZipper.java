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

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import org.gzipper.java.presentation.control.BaseController;
import org.gzipper.java.presentation.control.MainViewController;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.gzipper.java.application.model.OperatingSystem;
import org.gzipper.java.application.model.Unix;
import org.gzipper.java.application.model.Windows;
import org.gzipper.java.application.util.AppUtil;
import org.gzipper.java.application.util.Settings;

/**
 * EXPERIMENTAL - still in active development.
 *
 * @author Matthias Fussenegger
 * @version 2017-03-20
 */
public class GZipper extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        initApplication();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        loader.setResources(ResourceBundle.getBundle("i18n/gzipperMainView", Locale.ENGLISH));

        Parent root = loader.load();

        // associate primary stage with controller
        MainViewController gfc = loader.getController();
        gfc.setPrimaryStage(stage);

        Scene scene = new Scene(root);
        BaseController.getStages().add(stage);

        // properly shut down application when closing window
        stage.setOnCloseRequest((WindowEvent evt) -> {
            evt.consume();
            System.exit(0);
        });

        stage.setTitle("GZipper");
        stage.getIcons().add(BaseController.getFrameImage());
        stage.setScene(scene);
        stage.show();
    }

    private void initApplication() {
        try {
            final String decPath = AppUtil.getDecodedRootPath(getClass());

            String settingsFile;
            try { // locate settings file
                settingsFile = AppUtil.getResource(GZipper.class, "/settings.properties");
            } catch (URISyntaxException ex) {
                Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
                settingsFile = decPath + "settings.properties";
            }

            // set operating system and instantiate settings file
            OperatingSystem os = System.getProperty("os.name").startsWith("Windows")
                    ? new Windows()
                    : new Unix();

            Settings.getInstance().init(settingsFile, os);

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GZipper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
