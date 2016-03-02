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

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Matthias Fussenegger
 * @version 2016-03-02
 */
public class NewFXMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gzipperFX.fxml"));
        Parent root = loader.load();

        /*associate primary stage with controller*/
        gzipperFXController gfc = loader.getController();
        gfc.setPrimaryStage(stage);

        Scene scene = new Scene(root);

        stage.setTitle("GZipper");
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
