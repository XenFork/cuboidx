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

/**
 * @author squid233
 * @since 0.1.0
 */
public enum GLDrawMode {
    TRIANGLES(GL.TRIANGLES, 3, 0, 1, 2),
    ;

    private final int enumValue;
    private final int vertexCount;
    private final int indexCount;
    private final int[] indices;

    GLDrawMode(int enumValue, int vertexCount, int... indices) {
        this.enumValue = enumValue;
        this.vertexCount = vertexCount;
        this.indexCount = indices.length;
        this.indices = indices;
    }

    public int enumValue() {
        return enumValue;
    }

    public int vertexCount() {
        return vertexCount;
    }

    public int indexCount() {
        return indexCount;
    }

    public int[] indices() {
        return indices;
    }
}
