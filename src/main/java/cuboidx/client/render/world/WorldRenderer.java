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
import cuboidx.client.gl.GLStateMgr;
import cuboidx.client.gl.RenderSystem;
import cuboidx.client.render.BufferedVertexBuilder;
import cuboidx.client.texture.TextureAtlas;
import cuboidx.world.World;
import cuboidx.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import overrungl.opengl.GL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The world renderer renders the world in {@link cuboidx.world.chunk.Chunk chunks}.
 * <p>
 * Each chunk is compiled in certain {@link BlockRenderLayer layers}.<br>
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
    private static final int MAX_COMPILE_COUNT = Runtime.getRuntime().availableProcessors() + 1;
    private final CuboidX client;
    private final World world;
    private final int xChunks, yChunks, zChunks;
    private final ClientChunk[] chunks;
    private final List<ClientChunk> dirtyChunks;
    private final ChunkCompiler compiler = new ChunkCompiler();
    private final ExecutorService threadPool;

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
                    this.chunks[xChunks * (y * zChunks + z) + x] = new ClientChunk(client,
                        world,
                        x, y, z,
                        x * Chunk.SIZE,
                        y * Chunk.SIZE,
                        z * Chunk.SIZE,
                        Math.min((x + 1) * Chunk.SIZE, world.width()) - 1,
                        Math.min((y + 1) * Chunk.SIZE, world.height()) - 1,
                        Math.min((z + 1) * Chunk.SIZE, world.depth()) - 1
                    );
                }
            }
        }
        this.dirtyChunks = Collections.synchronizedList(new ArrayList<>(chunks.length));

        threadPool = new ThreadPoolExecutor(MAX_COMPILE_COUNT - 1,
            MAX_COMPILE_COUNT,
            30L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(@NotNull Runnable r) {
                    return new Thread(r, "Chunk-worker-thread-" + threadNumber.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.DiscardPolicy());
    }

    public void compileChunks() {
        dirtyChunks.clear();
        for (ClientChunk chunk : chunks) {
            // TODO: 2023/7/8 Receiving changes
            if (chunk.dirty() && !chunk.submitted()) {
                dirtyChunks.add(chunk);
            }
        }
        dirtyChunks.sort(new DirtyChunkSorter(client.player(), RenderSystem.frustum()));
        for (int i = 0; i < dirtyChunks.size() && i < MAX_COMPILE_COUNT; i++) {
            final ClientChunk chunk = dirtyChunks.get(i);
            threadPool.submit(() -> {
                chunk.setSubmitted(true);
                final BufferedVertexBuilder builder = compiler.poll(BlockRenderLayer.OPAQUE);
                chunk.compile(builder);
                compiler.free(BlockRenderLayer.OPAQUE, builder);
                chunk.setSubmitted(false);
            });
        }
    }

    public void renderChunks(double partialTick) {
        compileChunks();

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
        final Vector3d rotation = client.camera().rotation();
        RenderSystem.viewMatrix()
            .rotationXYZ(
                (float) -rotation.x(),
                (float) -rotation.y(),
                (float) -rotation.z())
            .translate(
                (float) -pos.x(),
                (float) -pos.y(),
                (float) -pos.z());
        RenderSystem.updateFrustum();
        final int currentProgram = GLStateMgr.currentProgram();
        RenderSystem.useProgram(client.gameRenderer().positionColorTextureProgram(), program -> {
            program.projectionMatrix().set(RenderSystem.projectionMatrix());
            program.modelViewMatrix().set(RenderSystem.modelViewMatrix());
            program.specifyUniforms();
        });
        RenderSystem.bindTexture2D(client.textureManager().get(TextureAtlas.BLOCK_ATLAS));

        // render
        for (ClientChunk chunk : chunks) {
            if (chunk.isVisible(RenderSystem.frustum())) {
                chunk.render();
            }
        }

        // reset states
        RenderSystem.bindTexture2D(0);
        RenderSystem.useProgram(currentProgram);

        RenderSystem.disableCullFace();
        RenderSystem.disableDepthTest();
    }

    @Override
    public void close() {
        for (ClientChunk chunk : chunks) {
            chunk.close();
        }
        compiler.close();
        threadPool.shutdown();
        logger.info("Cleaned up WorldRenderer");
    }
}
