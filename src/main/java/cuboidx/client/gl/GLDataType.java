/*
 * cuboidx - A 3D sandbox game
 * Copyright (C) 2023  XenFork Union
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cuboidx.client.gl;

import org.overrun.glib.gl.GL;

import java.lang.foreign.ValueLayout;

/**
 * @author squid233
 * @since 0.1.0
 */
public enum GLDataType {
    BYTE(GL.BYTE, Byte.BYTES, "Byte", ValueLayout.JAVA_BYTE),
    UNSIGNED_BYTE(GL.UNSIGNED_BYTE, Byte.BYTES, "Unsigned Byte", ValueLayout.JAVA_BYTE),
    INT(GL.INT, Integer.BYTES, "Int", ValueLayout.JAVA_INT),
    FLOAT(GL.FLOAT, Float.BYTES, "Float", ValueLayout.JAVA_FLOAT_UNALIGNED);

    private final int enumValue;
    private final int size;
    private final String stringName;
    private final ValueLayout layout;

    GLDataType(int enumValue, int size, String stringName, ValueLayout layout) {
        this.enumValue = enumValue;
        this.size = size;
        this.stringName = stringName;
        this.layout = layout;
    }

    public int enumValue() {
        return enumValue;
    }

    public int size() {
        return size;
    }

    public ValueLayout layout() {
        return layout;
    }

    @Override
    public String toString() {
        return stringName;
    }
}
