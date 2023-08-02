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

import cuboidx.client.CuboidX;
import cuboidx.client.gl.GLDrawMode;
import cuboidx.client.gl.GLProgram;
import cuboidx.client.gl.GLStateMgr;
import cuboidx.client.gl.RenderSystem;
import cuboidx.client.texture.TextureAtlas;
import cuboidx.registry.Registries;
import cuboidx.util.ResourceLocation;
import cuboidx.util.math.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import overrungl.opengl.GL;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class GameRenderer implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger();
    private final CuboidX client;
    private GLProgram positionColorProgram,
        positionColorTextureProgram;

    public GameRenderer(CuboidX client) {
        this.client = client;
        positionColorProgram = loadProgram("core/position_color", VertexLayout.POSITION_COLOR);
        positionColorTextureProgram = loadProgram("core/position_color_texture", VertexLayout.POSITION_COLOR_TEXTURE);

        final Set<ResourceLocation> textures = new HashSet<>();
        for (var e : Registries.BLOCK_TYPE) {
            for (Direction direction : Direction.list()) {
                final ResourceLocation texture = e.getValue().texture(direction);
                if (texture != null)
                    textures.add(texture);
            }
        }
        // TODO: 2023/6/17 load texture atlas asynchronously
        final TextureAtlas atlas = TextureAtlas.load(TextureAtlas.BLOCK_ATLAS, textures);
        client.textureManager().add(atlas);
        logger.info("Created: {}x{}x{} {}", atlas.width(), atlas.height(), atlas.mipmapLevel(), TextureAtlas.BLOCK_ATLAS);
    }

    private static GLProgram loadProgram(String path, VertexLayout layout) {
        return Objects.requireNonNull(GLProgram.load(ResourceLocation.cuboidx(path), layout), "Couldn't load program " + path);
    }

    public void render(double partialTick) {
        client.worldRenderer().renderChunks(partialTick);
        client.worldRenderer().renderHitResult();
        renderGui(partialTick);
    }

    private void renderGui(double partialTick) {
        GL.clear(GL.DEPTH_BUFFER_BIT);
        RenderSystem.projectionMatrix().setOrtho(0, client.width(), 0, client.height(), -100, 100);
        RenderSystem.viewMatrix().identity();
        // draw crossing
        RenderSystem.modelMatrix().pushMatrix().translation(client.width() * 0.5f, client.height() * 0.5f, 0);
        final int currentProgram = GLStateMgr.currentProgram();
        RenderSystem.useProgram(positionColorProgram(), p -> {
            p.projectionMatrix().set(RenderSystem.projectionMatrix());
            p.modelViewMatrix().set(RenderSystem.modelViewMatrix());
            p.specifyUniforms();
        });
        RenderSystem.modelMatrix().popMatrix();
        final Tessellator t = Tessellator.getInstance();
        t.begin(GLDrawMode.QUADS);
        t.enableAutoIndices();
        t.color(1, 1, 1, 1);
        t.vertex(-8, 1, 0).emit();
        t.vertex(-8, -1, 0).emit();
        t.vertex(8, -1, 0).emit();
        t.vertex(8, 1, 0).emit();
        t.vertex(-1, 8, 0).emit();
        t.vertex(-1, -8, 0).emit();
        t.vertex(1, -8, 0).emit();
        t.vertex(1, 8, 0).emit();
        t.end();
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
        Tessellator.getInstance().dispose();
        logger.info("Cleaned up GameRenderer");
    }
}
