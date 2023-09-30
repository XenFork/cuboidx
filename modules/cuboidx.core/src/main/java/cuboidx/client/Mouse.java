/*
 * cuboidx - A 3D sandbox game
 * Copyright (C) 2023  XenFork Union
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cuboidx.client;

import cuboidx.client.event.MouseEvent;
import cuboidx.util.AtomicDouble;
import org.overrun.pooling.PoolObjectState;
import overrungl.glfw.GLFW;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Mouse {
    private final MemorySegment window;
    private final AtomicLong cursorX = new AtomicLong();
    private final AtomicLong cursorY = new AtomicLong();
    private final AtomicLong cursorDeltaX = new AtomicLong();
    private final AtomicLong cursorDeltaY = new AtomicLong();
    private boolean mouseGrabbed = false;

    public Mouse(MemorySegment window) {
        this.window = window;
        GLFW.setCursorPosCallback(window, (h, x, y) -> {
            AtomicDouble.set(cursorDeltaX, x - cursorX());
            AtomicDouble.set(cursorDeltaY, y - cursorY());
            AtomicDouble.set(cursorX, x);
            AtomicDouble.set(cursorY, y);
            final PoolObjectState<MouseEvent.CursorPos> event = MouseEvent.CursorPos.of(x, y, cursorDeltaX(), cursorDeltaY());
            CuboidX.EVENT_BUS.post(event.get());
            MouseEvent.CursorPos.free(event);
        });
        GLFW.setMouseButtonCallback(window, (h, button, action, mods) -> {
            final MouseEvent.Button event = new MouseEvent.Button(button, action, mods);
            CuboidX.EVENT_BUS.post(event);
        });
    }

    public boolean isButtonUp(int button) {
        return GLFW.getMouseButton(window, button) == GLFW.RELEASE;
    }

    public boolean isButtonDown(int button) {
        return GLFW.getMouseButton(window, button) == GLFW.PRESS;
    }

    public double cursorX() {
        return AtomicDouble.get(cursorX);
    }

    public double cursorY() {
        return AtomicDouble.get(cursorY);
    }

    public double cursorDeltaX() {
        return AtomicDouble.get(cursorDeltaX);
    }

    public double cursorDeltaY() {
        return AtomicDouble.get(cursorDeltaY);
    }

    public void setMouseGrabbed(boolean mouseGrabbed) {
        this.mouseGrabbed = mouseGrabbed;
        GLFW.setInputMode(window, GLFW.CURSOR, mouseGrabbed ? GLFW.CURSOR_DISABLED : GLFW.CURSOR_NORMAL);
    }

    public boolean mouseGrabbed() {
        return mouseGrabbed;
    }
}
