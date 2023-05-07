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

package cuboidx.client.render;

import cuboidx.client.gl.GLDrawMode;
import cuboidx.client.gl.GLProgram;
import cuboidx.client.gl.GLStateMgr;
import cuboidx.client.gl.RenderSystem;
import cuboidx.util.ResourceLocation;

import java.util.Objects;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class GameRenderer implements AutoCloseable {
    private GLProgram positionColorProgram;

    private static GLProgram loadProgram(String path, VertexLayout layout) {
        return Objects.requireNonNull(GLProgram.load(ResourceLocation.cuboidx(path), layout), "Couldn't load program " + path);
    }

    public void init() {
        positionColorProgram = loadProgram("shader/core/position_color", VertexLayout.POSITION_COLOR);
    }

    public void render(double partialTick) {
        final int currentProgram = GLStateMgr.currentProgram();
        RenderSystem.useProgram(positionColorProgram);
        positionColorProgram.specifyUniforms();
        final Tessellator t = Tessellator.getInstance();
        t.begin(GLDrawMode.TRIANGLES);
        t.enableAutoIndices();
        t.position(0, 0.5f, 0).color(1, 0, 0, 1).emit();
        t.position(-0.5f, -0.5f, 0).color(0, 1, 0, 1).emit();
        t.position(0.5f, -0.5f, 0).color(0, 0, 1, 1).emit();
        t.end();
        RenderSystem.useProgram(currentProgram);
    }

    @Override
    public void close() {
        if (positionColorProgram != null) positionColorProgram.close();
        Tessellator.free();
    }
}
