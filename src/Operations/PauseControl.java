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
package Operations;

/**
 *
 * @author Matthias Fussenegger
 */
public class PauseControl {

    /**
     * True if thread needs to pause
     */
    private boolean _needToPause;

    /**
     * This method lets a thread of another class to be paused. Behavior of this
     * method depends on whether pause() or unpause() has been called before
     *
     * @throws InterruptedException If an error occurred
     */
    public synchronized void pausePoint() throws InterruptedException {
        while (_needToPause) {
            wait();
        }
    }

    /**
     * To pause execution of thread when pause point has been called
     */
    public synchronized void pause() {
        _needToPause = true;
    }

    /**
     * To continue execution of thread when pause point has been called
     */
    public synchronized void unpause() {
        _needToPause = false;
        this.notifyAll();
    }
}
