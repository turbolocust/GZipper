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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;

/**
 * A class for creating additional frames with pre-defined properties
 *
 * @author Matthias Fussenegger
 */
public class SubFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor of this class that extends {@code JFrame}
     *
     * @param manager The default layout of this sub frame to be set
     */
    public SubFrame(LayoutManager manager) {
        super();
        initFrame(manager);
    }

    /**
     * Constructor of this class that extends {@code JFrame}
     *
     * @param title The title of the frame to be set
     * @param manager The default layout of this sub frame to be set
     */
    public SubFrame(String title, LayoutManager manager) {
        super(title);
        initFrame(manager);
    }

    /**
     * Initializes this frame with pre-defined properties
     *
     * @param manager The default layout of this sub frame to be set
     */
    private void initFrame(LayoutManager manager) {
        if (manager != null) {
            setLayout(manager);
        }
        setResizable(false);
        setAlwaysOnTop(true);
        setMinimumSize(new Dimension(300, 100));
        setIconImage(Settings._frameIcon);
    }

    /**
     * Sets final properties and draws frame
     */
    protected void drawFrame() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Adds a new component to this frame with a {@code KeyListener} for the
     * specified key on keyboard. After pressing this key the frame will dispose
     *
     * @param c The component to be added
     * @param keyCode The {@code KeyEvent} the component will listen to
     */
    protected void addWithKeyListener(Component c, int keyCode) {
        if (c != null) {
            if (c.isFocusable()) {
                c.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent evt) {
                        if (evt.getKeyCode() == keyCode) {
                            setVisible(false);
                            dispose();
                        }
                    }
                });
            }
            super.add(c);
        }
    }
}
