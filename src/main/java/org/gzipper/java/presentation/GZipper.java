/*
 * Copyright (C) 2018 Matthias Fussenegger
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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import org.gzipper.java.presentation.controller.BaseController;
import org.gzipper.java.presentation.controller.MainViewController;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.gzipper.java.application.model.OS;
import org.gzipper.java.application.model.OperatingSystem;
import org.gzipper.java.application.util.AppUtils;
import org.gzipper.java.application.util.FileUtils;
import org.gzipper.java.util.Settings;
import org.gzipper.java.util.Log;

/**
 *
 * @author Matthias Fussenegger
 */
public class GZipper extends Application {

    @Override
    public final void start(Stage stage) throws Exception {

        initApplication(); // has to be the first call

        Settings settings = Settings.getInstance();
        final boolean enableLogging = settings.evaluateProperty("loggingEnabled");
        final boolean enableDarkTheme = settings.evaluateProperty("darkThemeEnabled");

        // initialize logger if logging is enabled
        if (enableLogging) {
            initLogger();
        }

        // set correct theme based on settings
        final CSS.Theme theme = enableDarkTheme
                ? CSS.Theme.DARK_THEME : CSS.Theme.MODENA;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        MainViewController controller = new MainViewController(theme, getHostServices());

        fxmlLoader.setResources(ResourceBundle.getBundle("i18n/gzipperMainView"));
        fxmlLoader.setController(controller);

        // load parent to initialize scene
        Scene scene = new Scene(fxmlLoader.load());
        BaseController.getStages().add(stage);
        controller.setPrimaryStage(stage);

        // load CSS theme
        CSS.load(theme, scene);

        // properly shut down application when closing/hiding window
        stage.setOnCloseRequest((WindowEvent evt) -> {
            evt.consume();
            controller.cancelActiveTasks();
            exitApplication();
        });
        stage.setOnHiding((WindowEvent evt) -> {
            evt.consume();
            controller.cancelActiveTasks();
            exitApplication();
        });

        stage.setTitle("GZipper");
        stage.getIcons().add(BaseController.getIconImage());
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Initializes the application.
     */
    private void initApplication() {
        try {
            final String decPath = AppUtils.getDecodedRootPath(getClass());

            File settings = new File(decPath + "settings.properties");
            if (!settings.exists()) {
                try { // copy settings file to application folder if missing
                    String resource = AppUtils.getResource(GZipper.class, "/settings.properties");
                    FileUtils.copy(resource, decPath + "settings.properties");
                }
                catch (URISyntaxException | IOException ex) {
                    Log.e(ex.getLocalizedMessage(), ex);
                }
            }

            // determine operating system and initialize settings class
            OperatingSystem os = System.getProperty("os.name")
                    .toLowerCase().startsWith("windows")
                    ? new OperatingSystem(OS.WINDOWS)
                    : new OperatingSystem(OS.UNIX);

            Settings.getInstance().init(settings, os);
        }
        catch (UnsupportedEncodingException ex) {
            Log.e(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Initializes the logger that will append text to the console.
     */
    private void initLogger() {

        Logger logger = Log.DEFAULT_LOGGER;

        try {
            final String decPath = AppUtils.getDecodedRootPath(getClass());
            FileHandler handler = new FileHandler(decPath + "gzipper.log");
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
            Log.setVerboseUiLogging(true);
        }
        catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        catch (IOException | SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Exits the application properly.
     */
    private void exitApplication() {
        Log.i("Exiting application.", false);
        Platform.exit();
        System.exit(0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // adjust output of the simple formatter
        System.setProperty(
                "java.util.logging.SimpleFormatter.format",
                "[%1$tm-%1$te-%1$ty, %1$tH:%1$tM:%1$tS] %4$s: %5$s %n"
        );
        // store away settings file at application termination
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    Settings.getInstance().storeAway();
                    Logger logger = Log.DEFAULT_LOGGER;
                    for (Handler handler : logger.getHandlers()) {
                        handler.close();
                    }
                }
                catch (IOException ex) {
                    Log.e(ex.getLocalizedMessage(), ex);
                }
            }
        });
        launch(args); // actually launch the UI
    }
}
