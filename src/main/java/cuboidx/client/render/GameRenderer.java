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
import cuboidx.client.texture.Texture2D;
import cuboidx.util.ResourceLocation;

import java.util.Objects;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class GameRenderer implements AutoCloseable {
    private GLProgram positionColorProgram,
        positionColorTextureProgram;
    private Texture2D texture2D;

    private static GLProgram loadProgram(String path, VertexLayout layout) {
        return Objects.requireNonNull(GLProgram.load(ResourceLocation.cuboidx(path), layout), "Couldn't load program " + path);
    }

    public void init() {
        positionColorProgram = loadProgram("core/position_color", VertexLayout.POSITION_COLOR);
        positionColorTextureProgram = loadProgram("core/position_color_texture", VertexLayout.POSITION_COLOR_TEXTURE);
        texture2D = Texture2D.load(ResourceLocation.cuboidx("block/grass_block_top"));
    }

    public void render(double partialTick) {
        final int currentProgram = GLStateMgr.currentProgram();
        RenderSystem.useProgram(positionColorTextureProgram).specifyUniforms();
        RenderSystem.bindTexture2D(texture2D);
        final Tessellator t = Tessellator.getInstance();
        t.begin(GLDrawMode.QUADS);
        t.enableAutoIndices();
        t.position(-0.5f, 0.5f, 0.0f).color(1, 1, 1, 1).texture(0.0f, 0.0f).emit();
        t.position(-0.5f, -0.5f, 0.0f).color(1, 1, 1, 1).texture(0.0f, 1.0f).emit();
        t.position(0.5f, -0.5f, 0.0f).color(1, 1, 1, 1).texture(1.0f, 1.0f).emit();
        t.position(0.5f, 0.5f, 0.0f).color(1, 1, 1, 1).texture(1.0f, 0.0f).emit();
        t.end();
        RenderSystem.bindTexture2D(0);
        RenderSystem.useProgram(currentProgram);
    }

    public GLProgram positionColorProgram() {
        return positionColorProgram;
    }

    public GLProgram positionColorTextureProgram() {
        return positionColorTextureProgram;
    }

    @Override
    public void close() {
        if (positionColorProgram != null) positionColorProgram.close();
        if (positionColorTextureProgram != null) positionColorTextureProgram.close();
        if (texture2D != null) texture2D.close();
        Tessellator.free();
    }
}
