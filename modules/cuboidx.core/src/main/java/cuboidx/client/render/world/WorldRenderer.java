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

package cuboidx.client.render.world;

import cuboidx.client.CuboidX;
import cuboidx.client.gl.GLDrawMode;
import cuboidx.client.gl.GLStateMgr;
import cuboidx.client.gl.RenderSystem;
import cuboidx.client.render.GraphicsUtil;
import cuboidx.client.render.Tessellator;
import cuboidx.client.texture.TextureAtlas;
import cuboidx.util.math.AABBox;
import cuboidx.util.math.Direction;
import cuboidx.world.HitResult;
import cuboidx.world.World;
import cuboidx.world.WorldListener;
import cuboidx.world.block.BlockType;
import cuboidx.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import overrungl.opengl.GL;

import java.lang.Math;
import java.lang.Runtime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
public final class WorldRenderer implements WorldListener, AutoCloseable {
    private static final Logger logger = LogManager.getLogger();
    private static final int MAX_COMPILE_COUNT = Runtime.getRuntime().availableProcessors() + 1;
    private final CuboidX client;
    private final World world;
    private final int xChunks, yChunks, zChunks;
    private final ClientChunk[] chunks;
    private final List<ClientChunk> dirtyChunks;
    private final ChunkCompiler compiler = new ChunkCompiler();
    private final ExecutorService threadPool;
    private final AtomicReference<HitResult> hitResult = new AtomicReference<>();
    private final Vector3f hitOrigin = new Vector3f();
    private final Vector3f hitOrientation = new Vector3f();
    private final Vector2d hitNearFar = new Vector2d();
    private volatile boolean shouldRenderDebugHud = false;

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
            10L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(@NotNull Runnable r) {
                    return new Thread(r, STR. "Chunk-worker-thread-\{ threadNumber.getAndIncrement() }" );
                }
            },
            (r, executor) -> {
                if (!executor.isShutdown() && r instanceof Future<?> future) {
                    future.cancel(true);
                }
            });

        world.addListener(this);
    }

    public void compileChunks() {
        dirtyChunks.clear();
        for (int i = 0, j = 0; i < chunks.length && j < MAX_COMPILE_COUNT; i++) {
            ClientChunk chunk = chunks[i];
            // TODO: 2023/7/8 Receiving changes
            if (chunk.dirty() && !chunk.submitted()) {
                dirtyChunks.add(chunk);
                j++;
            }
        }
        dirtyChunks.sort(new DirtyChunkSorter(client.player(), RenderSystem.frustum()));
        for (int i = 0; i < dirtyChunks.size() && i < MAX_COMPILE_COUNT; i++) {
            final ClientChunk chunk = dirtyChunks.get(i);
            CompletableFuture.supplyAsync(() -> {
                chunk.setSubmitted(true);
                final var builder = compiler.borrow(BlockRenderLayer.OPAQUE);
                chunk.compile(builder.get());
                compiler.returning(builder);
                return chunk;
            }, threadPool).thenAccept(clientChunk -> {
                final ClientChunk.CompileStates states = clientChunk.states();
                states.setUploaded(false);
                clientChunk.setSubmitted(false);
                clientChunk.markNotDirty();
                states.markCompiled();
            });
        }
    }

    public void renderChunks(double partialTick) {
        compileChunks();

        // initialize states
        RenderSystem.enableCullFace();
        RenderSystem.enableDepthTest(GL.LEQUAL);

        RenderSystem.projectionMatrix().setPerspective(
            (float) Math.toRadians(90.0),
            (float) client.width() / (float) client.height(),
            0.05f,
            1000.0f
        );
        client.camera().lerp(partialTick);
        client.camera().viewMatrix(RenderSystem.viewMatrix());
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

        getHitBlock();
    }

    public void renderHitResult() {
        final HitResult result = hitResult();
        if (result != null && !result.missed()) {
            final AABBox box = result.block().outlineShape().move(
                result.x(),
                result.y(),
                result.z());
            final float x0 = (float) box.minX();
            final float y0 = (float) box.minY();
            final float z0 = (float) box.minZ();
            final float x1 = (float) box.maxX();
            final float y1 = (float) box.maxY();
            final float z1 = (float) box.maxZ();

            final float lineWidth = GLStateMgr.lineWidth();
            RenderSystem.lineWidth(2);
            RenderSystem.enableLineSmooth();
            RenderSystem.polygonOffset(-0.1f, 0.2f);
            RenderSystem.enablePolygonOffsetLine();

            final int currentProgram = GLStateMgr.currentProgram();
            RenderSystem.useProgram(client.gameRenderer().positionColorProgram(), program -> {
                program.projectionMatrix().set(RenderSystem.projectionMatrix());
                program.modelViewMatrix().set(RenderSystem.modelViewMatrix());
                program.specifyUniforms();
            });

            final Tessellator t = Tessellator.getInstance();
            t.begin(GLDrawMode.LINES, false);
            t.indices(
                // x
                3, 7, 1, 5, 0, 4, 2, 6,
                // y
                0, 2, 1, 3, 5, 7, 4, 6,
                // z
                2, 3, 0, 1, 4, 5, 6, 7
            );
            t.color(0, 0, 0, 1);
            GraphicsUtil.emitCuboidVertex(t, x0, y0, z0, x1, y1, z1);
            t.end();

            RenderSystem.useProgram(currentProgram);

            RenderSystem.lineWidth(lineWidth);
            RenderSystem.disableLineSmooth();
            RenderSystem.disablePolygonOffsetLine();
        }
    }

    public void renderGui(double partialTick) {
        // draw crossing
        RenderSystem.modelMatrix().pushMatrix().translation(client.width() * 0.5f, client.height() * 0.5f, 0);
        if (shouldRenderDebugHud()) {
            final Vector3d rotation = client.camera().rotation();
            RenderSystem.modelMatrix().rotateXYZ((float) -rotation.x(), (float) -rotation.y(), (float) -rotation.z());
        }
        final int currentProgram = GLStateMgr.currentProgram();
        RenderSystem.useProgram(client.gameRenderer().positionColorProgram(), p -> {
            p.projectionMatrix().set(RenderSystem.projectionMatrix());
            p.modelViewMatrix().set(RenderSystem.modelViewMatrix());
            p.specifyUniforms();
        });
        RenderSystem.modelMatrix().popMatrix();
        final Tessellator t = Tessellator.getInstance();
        if (shouldRenderDebugHud()) {
            RenderSystem.enableDepthTest(GL.LEQUAL);

            t.begin(GLDrawMode.QUADS, false);
            GraphicsUtil.drawCuboid(t.color(1, 0, 0, 1), 0, 0, 0, 16, 1, 1);
            GraphicsUtil.drawCuboid(t.color(0, 1, 0, 1), 0, 0, 0, 1, 16, 1);
            GraphicsUtil.drawCuboid(t.color(0, 0, 1, 1), 0, 0, 0, 1, 1, 16);
            t.end();

            RenderSystem.disableDepthTest();
        } else {
            t.begin(GLDrawMode.QUADS, true);
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
        }
        RenderSystem.useProgram(currentProgram);
    }

    private void getHitBlock() {
        final int pickRange = 5;
        double closestDistance = Double.POSITIVE_INFINITY;
        BlockType target = null;
        int targetX = 0;
        int targetY = 0;
        int targetZ = 0;
        AABBox finalBox = null;
        final FrustumIntersection frustum = RenderSystem.frustum();
        final FrustumRayBuilder ray = RenderSystem.ray();
        ray.origin(hitOrigin);
        ray.dir(0.5f, 0.5f, hitOrientation);

        final Vector3d playerPos = client.player().position();
        final int orgX = (int) Math.floor(playerPos.x());
        final int orgY = (int) Math.floor(playerPos.y());
        final int orgZ = (int) Math.floor(playerPos.z());
        final int x0 = orgX - pickRange;
        final int y0 = orgY - pickRange;
        final int z0 = orgZ - pickRange;
        final int x1 = orgX + pickRange;
        final int y1 = orgY + pickRange;
        final int z1 = orgZ + pickRange;

        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    final BlockType block = world.getBlock(x, y, z);
                    final AABBox box = block.outlineShape().move(x, y, z);
                    if (!box.isEmpty() && box.test(frustum)) {
                        if (Intersectiond.intersectRayAab(
                            hitOrigin.x(), hitOrigin.y(), hitOrigin.z(),
                            hitOrientation.x(), hitOrientation.y(), hitOrientation.z(),
                            box.minX(), box.minY(), box.minZ(),
                            box.maxX(), box.maxY(), box.maxZ(),
                            hitNearFar
                        ) && hitNearFar.x < closestDistance) {
                            closestDistance = hitNearFar.x();
                            target = block;
                            targetX = x;
                            targetY = y;
                            targetZ = z;
                            finalBox = box;
                        }
                    }
                }
            }
        }

        hitResult.set(target == null ? null : new HitResult(
            false,
            finalBox.testSide(
                hitOrigin.x(), hitOrigin.y(), hitOrigin.z(),
                hitOrientation.x(), hitOrientation.y(), hitOrientation.z()
            ),
            targetX,
            targetY,
            targetZ,
            target));
    }

    public HitResult hitResult() {
        return hitResult.get();
    }

    private ClientChunk getChunk(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= xChunks || y >= yChunks || z >= zChunks) return null;
        return chunks[xChunks * (y * zChunks + z) + x];
    }

    private ClientChunk getChunkByBlockPos(int x, int y, int z) {
        return getChunk(
            Math.floorDiv(x, Chunk.SIZE),
            Math.floorDiv(y, Chunk.SIZE),
            Math.floorDiv(z, Chunk.SIZE)
        );
    }

    public boolean shouldRenderDebugHud() {
        return shouldRenderDebugHud;
    }

    public void setShouldRenderDebugHud(boolean shouldRenderDebugHud) {
        this.shouldRenderDebugHud = shouldRenderDebugHud;
    }

    @Override
    public void onBlockChanged(int x, int y, int z, BlockType newBlock) {
        for (Direction direction : Direction.list()) {
            final ClientChunk chunk = getChunkByBlockPos(
                x + direction.axisX(),
                y + direction.axisY(),
                z + direction.axisZ()
            );
            if (chunk != null) chunk.markDirty();
        }
        final ClientChunk chunk = getChunkByBlockPos(x, y, z);
        if (chunk != null) chunk.markDirty();
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
