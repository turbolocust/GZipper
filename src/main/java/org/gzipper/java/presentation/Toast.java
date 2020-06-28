/*
 * Copyright (C) 2020 Matthias Fussenegger
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

import org.gzipper.java.util.Log;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Class for creating and displaying toast messages.
 * <p>
 * The implementation of this class is based on <a href="https://stackoverflow.com/a/38373408/8240983">this</a> code.
 *
 * @author Matthias Fussenegger
 */
public final class Toast {

    private Toast() {
        throw new AssertionError("Holds static members only.");
    }

    /**
     * Shows a toast using the specified parameters.
     *
     * @param ownerStage the owner stage.
     * @param toastMsg   the message to be displayed.
     * @param msgColor   the color of the message.
     * @param toastDelay specifies how long the toast will be visible.
     */
    public static void show(Stage ownerStage, String toastMsg, Color msgColor, int toastDelay) {
        show(ownerStage, toastMsg, msgColor, toastDelay, 500, 500);
    }

    /**
     * Shows a toast using the specified parameters.
     *
     * @param ownerStage   the owner stage.
     * @param toastMsg     the message to be displayed.
     * @param msgColor     the color of the message.
     * @param toastDelay   specifies how long the toast will be visible.
     * @param fadeInDelay  delay when fading in.
     * @param fadeOutDelay delay when fading out.
     */
    public static void show(Stage ownerStage, String toastMsg, Color msgColor,
                            int toastDelay, int fadeInDelay, int fadeOutDelay) {

        final Stage toastStage = new Stage();
        toastStage.initOwner(ownerStage);
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        final Text text = new Text(toastMsg);
        text.setFont(Font.font("Verdana", 28));
        text.setFill(msgColor);

        final StackPane root = new StackPane(text);
        root.setStyle("-fx-background-radius: 16; "
                + "-fx-background-color: rgba(0, 0, 0, 0.1); "
                + "-fx-padding: 32px;");
        root.setOpacity(0);

        final Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        double centerXOwnerStage = ownerStage.getX() + ownerStage.getWidth() / 2d;
        double centerYOwnerStage = ownerStage.getY() + ownerStage.getHeight() / 2d;

        // size of stage is unknown until rendered, hence hide and relocate
        toastStage.setOnShowing(windowEvent -> toastStage.hide());
        // relocate toast stage to the center of the owner stage
        toastStage.setOnShown(windowEvent -> {
            toastStage.setX(centerXOwnerStage - toastStage.getWidth() / 2d);
            toastStage.setY(centerYOwnerStage - toastStage.getHeight() / 2d);
            toastStage.show();
        });

        toastStage.setScene(scene);
        toastStage.show();

        final Timeline fadeInTimeline = new Timeline();
        final KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(fadeInDelay),
                new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 1));

        fadeInTimeline.getKeyFrames().add(fadeInKey1);
        fadeInTimeline.setOnFinished((ev1) -> new Thread(() -> {
            try {
                Thread.sleep(toastDelay);
            } catch (InterruptedException ex) {
                Log.e("Thread of toast stage interrupted.", ex);
                Thread.currentThread().interrupt();
            }
            final Timeline fadeOutTimeline = new Timeline();
            final KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(fadeOutDelay),
                    new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0));
            fadeOutTimeline.getKeyFrames().add(fadeOutKey1);
            fadeOutTimeline.setOnFinished((ev2) -> toastStage.close());
            fadeOutTimeline.play();
        }).start());

        fadeInTimeline.play();
    }
}
