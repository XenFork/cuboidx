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

package cuboidx.client.render.world;

import cuboidx.client.CuboidX;
import cuboidx.client.gl.GLDrawMode;
import cuboidx.client.gl.GLStateMgr;
import cuboidx.client.gl.RenderSystem;
import cuboidx.client.render.Tessellator;
import cuboidx.util.ResourceLocation;
import cuboidx.world.World;
import cuboidx.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3d;
import org.overrun.gl.opengl.GL;

/**
 * The world renderer renders the world in {@link cuboidx.world.chunk.Chunk chunks}.
 * <p>
 * Each chunk is compiled in certain {@link RenderLayer layers}.<br>
 * The world renderer first renders opaque blocks, then renders translucent blocks from far to near.
 * <p>
 * Chunks outside the view of the player are not rendered.
 * <p>
 * Chunks are compiled in multi-thread: the player modifies the world,
 * and the {@link ChunkCompiler compiler} receives the modification.<br>
 * A compilation is discarded if the player modified the world when the compiler is compiling.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class WorldRenderer implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger();
    public static final ResourceLocation BLOCK_ATLAS = ResourceLocation.cuboidx("block-atlas");
    private final CuboidX client;
    private final World world;
    private final int xChunks, yChunks, zChunks;
    private final ClientChunk[] chunks;

    public WorldRenderer(CuboidX client, World world) {
        this.client = client;
        this.world = world;
        this.xChunks = Math.ceilDiv(world.width(), Chunk.SIZE);
        this.yChunks = Math.ceilDiv(world.height(), Chunk.SIZE);
        this.zChunks = Math.ceilDiv(world.depth(), Chunk.SIZE);
        this.chunks = new ClientChunk[xChunks * yChunks * zChunks];
        for (int x = 0; x < xChunks; x++) {
            for (int y = 0; y < yChunks; y++) {
                for (int z = 0; z < zChunks; z++) {
                    this.chunks[xChunks * (y * zChunks + z) + x] = new ClientChunk(world,
                        x * Chunk.SIZE,
                        y * Chunk.SIZE,
                        z * Chunk.SIZE,
                        Math.min((x + 1) * Chunk.SIZE, world.width()),
                        Math.min((y + 1) * Chunk.SIZE, world.height()),
                        Math.min((z + 1) * Chunk.SIZE, world.depth())
                    );
                }
            }
        }
    }

    public void compileChunks() {
    }

    public void renderChunks(double partialTick) {
        // initialize states
        RenderSystem.enableCullFace();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL.LEQUAL);

        RenderSystem.projectionMatrix().setPerspective(
            (float) Math.toRadians(90.0),
            (float) client.width() / (float) client.height(),
            0.05f,
            1000.0f
        );
        client.camera().lerp(partialTick);
        final Vector3d pos = client.camera().lerpPosition();
        RenderSystem.viewMatrix().translation(
            (float) -pos.x(),
            (float) -pos.y(),
            (float) -pos.z()
        );
        final int currentProgram = GLStateMgr.currentProgram();
        RenderSystem.useProgram(client.gameRenderer().positionColorTextureProgram(), program -> {
            program.projectionMatrix().set(RenderSystem.projectionMatrix());
            program.modelViewMatrix().set(RenderSystem.modelViewMatrix());
            program.specifyUniforms();
        });
        RenderSystem.bindTexture2D(client.textureManager().get(BLOCK_ATLAS));

        // render
        final Tessellator t = Tessellator.getInstance();
        t.begin(GLDrawMode.QUADS);
        t.enableAutoIndices();
        for (int x = 0; x < world.width(); x++) {
            for (int y = 0; y < world.height(); y++) {
                for (int z = 0; z < world.depth(); z++) {
                    client.blockRenderer().renderBlock(t, world.getBlock(x, y, z), x, y, z);
                }
            }
        }
        t.end();

        // reset states
        RenderSystem.bindTexture2D(0);
        RenderSystem.useProgram(currentProgram);

        RenderSystem.disableCullFace();
        RenderSystem.disableDepthTest();
    }

    @Override
    public void close() {
        logger.info("Cleaned up WorldRenderer");
    }
}
