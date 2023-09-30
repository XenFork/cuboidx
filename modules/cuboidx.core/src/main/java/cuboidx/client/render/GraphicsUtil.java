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

package cuboidx.client.render;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class GraphicsUtil {
    public static void emitCuboidVertex(VertexBuilder builder,
                                        float minX, float minY, float minZ,
                                        float maxX, float maxY, float maxZ) {
        builder.vertex(minX, minY, minZ).emit(); // 0
        builder.vertex(minX, minY, maxZ).emit(); // 1
        builder.vertex(minX, maxY, minZ).emit(); // 2
        builder.vertex(minX, maxY, maxZ).emit(); // 3
        builder.vertex(maxX, minY, minZ).emit(); // 4
        builder.vertex(maxX, minY, maxZ).emit(); // 5
        builder.vertex(maxX, maxY, minZ).emit(); // 6
        builder.vertex(maxX, maxY, maxZ).emit(); // 7
    }

    public static void drawCuboid(VertexBuilder builder,
                                  float minX, float minY, float minZ,
                                  float maxX, float maxY, float maxZ) {
        builder.indices(
            // -x
            2, 0, 1, 1, 3, 2,
            // +x
            7, 5, 4, 4, 6, 7,
            // -y
            1, 0, 4, 4, 5, 1,
            // +y
            2, 3, 7, 7, 6, 2,
            // -z
            6, 4, 0, 0, 2, 6,
            // +z
            3, 1, 5, 5, 7, 3
        );
        emitCuboidVertex(builder, minX, minY, minZ, maxX, maxY, maxZ);
    }
}
