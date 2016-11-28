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

import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * EXPERIMENTAL, use with caution as this application may not work at all
 *
 * @author Matthias Fussenegger
 * @version 2016-10-22
 */
public class GZipper extends Application {

    @Override
    public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        loader.setResources(ResourceBundle.getBundle("gzipperMainView", Locale.ENGLISH));

        Parent root = loader.load();

        /*associate primary stage with controller*/
        MainViewController gfc = loader.getController();
        gfc.setPrimaryStage(stage);

        Scene scene = new Scene(root);

        stage.setTitle("GZipper");
        stage.getIcons().add(BaseController._frameImage);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
