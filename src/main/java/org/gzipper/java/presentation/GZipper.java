/*
 * Copyright (C) 2020 Matthias Fussenegger
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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.gzipper.java.application.model.OS;
import org.gzipper.java.application.model.OperatingSystem;
import org.gzipper.java.application.util.AppUtils;
import org.gzipper.java.application.util.FileUtils;
import org.gzipper.java.presentation.controller.BaseController;
import org.gzipper.java.presentation.controller.HashViewController;
import org.gzipper.java.presentation.controller.MainViewController;
import org.gzipper.java.util.Log;
import org.gzipper.java.util.Settings;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.logging.*;

/**
 * @author Matthias Fussenegger
 */
public final class GZipper extends Application {

    //<editor-fold desc="Constants">

    private static final String LAUNCH_MODE_PARAM_NAME = "launch_mode";
    private static final String LAUNCH_MODE_APPLICATION = "application";
    private static final String LAUNCH_MODE_HASH_VIEW = "hashview";

    //</editor-fold>

    //<editor-fold desc="Initialization">

    private void initApplication() {
        final String decPath = AppUtils.getDecodedRootPath(getClass());

        File settings = new File(decPath + "settings.properties");
        if (!settings.exists()) {
            try { // copy settings file to application folder if missing
                String resource = AppUtils.getResource(GZipper.class, "/settings.properties");
                FileUtils.copy(resource, decPath + "settings.properties");
            } catch (URISyntaxException | IOException ex) {
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

    private void initLogger() {

        Logger logger = Log.DEFAULT_LOGGER;

        try {
            final String decPath = AppUtils.getDecodedRootPath(getClass());
            FileHandler handler = new FileHandler(decPath + "gzipper.log");
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
            Log.setVerboseUiLogging(true);
        } catch (IOException | SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    //</editor-fold>

    //<editor-fold desc="Loading of views">

    private Parent loadMainView(Stage stage, CSS.Theme theme) throws IOException {
        var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        var controller = new MainViewController(theme, getHostServices());
        controller.setPrimaryStage(stage);

        fxmlLoader.setResources(getDefaultResourceBundle());
        fxmlLoader.setController(controller);

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

        return fxmlLoader.load();
    }

    private Parent loadHashView(Stage stage, CSS.Theme theme) throws IOException {
        var fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/HashView.fxml"));
        var controller = new HashViewController(theme);
        controller.setPrimaryStage(stage);

        fxmlLoader.setResources(getDefaultResourceBundle());
        fxmlLoader.setController(controller);

        // properly shut down application when closing/hiding window
        stage.setOnCloseRequest((WindowEvent evt) -> {
            evt.consume();
            controller.interrupt();
            exitApplication();
        });

        stage.setOnHiding((WindowEvent evt) -> {
            evt.consume();
            controller.interrupt();
            exitApplication();
        });

        return fxmlLoader.load();
    }

    //</editor-fold>

    /**
     * Returns the default resource bundle to be used by this application.
     *
     * @return the default resource bundle to be used by this application.
     */
    private ResourceBundle getDefaultResourceBundle() {
        return ResourceBundle.getBundle("i18n/gzipper");
    }

    /**
     * Exits the application gracefully.
     */
    private void exitApplication() {
        Log.i("Exiting application", false);
        Platform.exit();
        System.exit(0);
    }

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
        final CSS.Theme theme = enableDarkTheme ? CSS.Theme.DARK_THEME : CSS.Theme.MODENA;

        var params = getParameters().getNamed(); // is never null
        String launchMode = params.get(LAUNCH_MODE_PARAM_NAME);
        if (launchMode == null) launchMode = LAUNCH_MODE_APPLICATION;

        Parent parent;
        switch (launchMode) {
            case LAUNCH_MODE_APPLICATION:
                parent = loadMainView(stage, theme);
                break;
            case LAUNCH_MODE_HASH_VIEW:
                parent = loadHashView(stage, theme);
                break;
            default:
                throw new IllegalArgumentException(String.format(
                        "Value of parameter '%s' is unknown", LAUNCH_MODE_PARAM_NAME));
        }

        // load parent to initialize scene
        Scene scene = new Scene(parent);
        BaseController.getStages().add(stage);

        // load CSS theme
        CSS.load(theme, scene);

        stage.setTitle("GZipper");
        stage.getIcons().add(BaseController.getIconImage());
        stage.setScene(scene);
        stage.show();
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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Settings.getInstance().storeAway();
                final Logger logger = Log.DEFAULT_LOGGER;
                for (Handler handler : logger.getHandlers()) {
                    handler.close();
                }
            } catch (IOException ex) {
                Log.e(ex.getLocalizedMessage(), ex);
            }
        }));

        launch(args);
    }
}
