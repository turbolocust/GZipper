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
module org.gzipper {
    opens org.gzipper.java.presentation to javafx.graphics;
    opens org.gzipper.java.presentation.controller to javafx.fxml;
    opens org.gzipper.java.presentation.controller.main to javafx.fxml;
    // standard-lib modules
    requires java.logging;
    // third-party modules
    requires org.apache.commons.compress;
    requires org.tukaani.xz;
    requires javafx.baseEmpty;
    requires javafx.base;
    requires javafx.controlsEmpty;
    requires javafx.controls;
    requires javafx.fxmlEmpty;
    requires javafx.fxml;
    requires javafx.graphicsEmpty;
    requires javafx.graphics;
    requires kotlin.stdlib;
}
