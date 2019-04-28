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
package org.gzipper.java.presentation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.gzipper.java.application.util.AppUtils;
import org.gzipper.java.util.Log;

/**
 * GUI utility class.
 *
 * @author Matthias Fussenegger
 */
public final class GUIUtils {

// WORKAROUND based on: 
// https://stackoverflow.com/questions/14650787/javafx-column-in-tableview-auto-fit-size
    private static final Method COLUMN_AUTOFIT_METHOD = initMethod();

    private static Method initMethod() {

        final String methodName = "resizeColumnToFitContent";
        Method method = null;

        try {
            final Class<?> clazz;
            if ((AppUtils.getJavaVersion().charAt(0) - 48) > 8) { // Java 9 and above
                /* clazz = Class.forName("javafx.scene.control.skin.TableSkinUtils"); */
                // does not work with Java 9 since module is not part of public API
            } else {
                clazz = Class.forName("com.sun.javafx.scene.control.skin.TableViewSkin");
                method = clazz.getDeclaredMethod(methodName, TableColumn.class, int.class);
                method.setAccessible(true);
            }
        }
        catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
            Log.e("Method lookup via reflection failed.", ex);
        }
        return method;
    }

    private GUIUtils() {
        throw new AssertionError("Holds static members only.");
    }

    /**
     * Auto fits the columns of the specified table to their content.
     *
     * @param table the table of which the columns are to be auto fitted.
     */
    @SuppressWarnings("Convert2Lambda")
    public static void autoFitTable(TableView<?> table) {
        if (COLUMN_AUTOFIT_METHOD == null) {
            return;
        }
        table.getItems().addListener(new ListChangeListener<Object>() {
            @Override
            public void onChanged(Change<?> c) {
                table.getColumns().forEach((column) -> {
                    try {
                        if (column.isVisible()) {
                            COLUMN_AUTOFIT_METHOD.invoke(table.getSkin(), column, -1);
                        }
                    }
                    catch (IllegalAccessException | InvocationTargetException ex) {
                        Log.e("Error invoking " + COLUMN_AUTOFIT_METHOD.getName(), ex);
                    }
                });
            }
        });
    }
}
