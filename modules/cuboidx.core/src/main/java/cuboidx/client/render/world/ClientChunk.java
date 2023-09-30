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
import cuboidx.client.render.BufferedVertexBuilder;
import cuboidx.util.math.Direction;
import cuboidx.world.World;
import cuboidx.world.block.BlockType;
import cuboidx.world.chunk.Chunk;
import cuboidx.world.entity.Entity;
import org.joml.FrustumIntersection;
import overrungl.opengl.GL;
import overrungl.util.MemoryStack;
import overrungl.util.MemoryUtil;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A client chunk that stored the vertices and indices data.
 * <p>
 * There are 4 phases of compilation: dirtying, submitting, compiling and uploading.
 * <h2>Dirtying</h2>
 * If the content of this chunk has been changed, this chunk will {@link #markDirty()}.
 * {@link WorldRenderer} will collect dirty chunks and submit then to the compiling task queue.
 * <h2>Submitting</h2>
 * Dirty chunks are submitted.
 * If the chunk submitted successfully, {@link #submitted() submitted} will be set to {@code true} and it will compile;
 * otherwise it is discarded and deferred to next submitting.
 * <h2>Compiling</h2>
 * Compiling chunk means building mesh data of the chunk.
 * This is done from another thread.
 * Once the chunk is compiled, {@link CompileStates#hadCompiled() hadCompiled} will be set to {@code true},
 * {@link CompileStates#uploaded() uploaded}, {@link #dirty() dirty} and {@link #submitted() submitted} will be set to false.
 * <h2>Uploading</h2>
 * If the chunk have had compiled, then the render thread will upload the mesh data to OpenGL.
 * Once the chunk is uploaded, {@link CompileStates#uploaded() uploaded} will be set to {@code true}.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class ClientChunk extends Chunk implements AutoCloseable {
    private final CompileStates states = new CompileStates();
    private final CuboidX client;
    private final AtomicBoolean dirty = new AtomicBoolean(true);
    private final AtomicBoolean submitted = new AtomicBoolean();

    public ClientChunk(CuboidX client,
                       World world,
                       double x, double y, double z,
                       int x0, int y0, int z0,
                       int x1, int y1, int z1) {
        super(world, x, y, z, x0, y0, z0, x1, y1, z1);
        this.client = client;
    }

    /**
     * The compiling states of the client chunk.
     *
     * @author squid233
     * @since 0.1.0
     */
    public static final class CompileStates implements AutoCloseable {
        private final BlockRenderLayer layer = BlockRenderLayer.OPAQUE;
        private final int vao, vbo, ebo;
        private final AtomicInteger indexCount = new AtomicInteger();
        private final AtomicBoolean hadCompiled = new AtomicBoolean();
        private final AtomicBoolean uploaded = new AtomicBoolean();
        private final AtomicBoolean expanded = new AtomicBoolean(true);
        private final AtomicReference<MemorySegment> data = new AtomicReference<>();
        private final AtomicReference<MemorySegment> indexData = new AtomicReference<>();

        private CompileStates() {
            vao = GL.genVertexArray();
            vbo = GL.genBuffer();
            ebo = GL.genBuffer();
        }

        public BlockRenderLayer layer() {
            return layer;
        }

        public int vao() {
            return vao;
        }

        public int vbo() {
            return vbo;
        }

        public int ebo() {
            return ebo;
        }

        public void setIndexCount(int indexCount) {
            this.indexCount.set(indexCount);
        }

        public int indexCount() {
            return indexCount.get();
        }

        public void markCompiled() {
            this.hadCompiled.set(true);
        }

        /**
         * Had this chunk compiled?
         */
        public boolean hadCompiled() {
            return hadCompiled.get();
        }

        public void setUploaded(boolean uploaded) {
            this.uploaded.set(uploaded);
        }

        /**
         * Is this chunk re-compiled and then uploaded?
         */
        public boolean uploaded() {
            return uploaded.get();
        }

        public void setExpanded(boolean expanded) {
            this.expanded.set(expanded);
        }

        /**
         * Is the buffer of this chunk expanded?
         */
        public boolean expanded() {
            return expanded.get();
        }

        public void setData(MemorySegment data) {
            this.data.set(data);
        }

        public MemorySegment data() {
            return data.get();
        }

        public void setIndexData(MemorySegment indexData) {
            this.indexData.set(indexData);
        }

        public MemorySegment indexData() {
            return indexData.get();
        }

        @Override
        public void close() {
            RenderSystem.deleteVertexArray(vao);
            RenderSystem.deleteArrayBuffer(vbo);
            GL.deleteBuffer(ebo);
            MemoryUtil.free(data());
            MemoryUtil.free(indexData());
        }
    }

    public void compile(BufferedVertexBuilder builder) {
        if (!dirty()) return;
        final BlockRenderer renderer = client.blockRenderer();
        builder.begin(GLDrawMode.TRIANGLES);
        for (Direction direction : Direction.list()) {
            for (int x = x0(); x <= x1(); x++) {
                for (int y = y0(); y <= y1(); y++) {
                    for (int z = z0(); z <= z1(); z++) {
                        final BlockType block = world().getBlock(x, y, z);
                        if (renderer.shouldRenderFace(block, world(), x, y, z, direction)) {
                            renderer.renderBlockFace(builder, block, x, y, z, direction);
                        }
                    }
                }
            }
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final MemorySegment pDataSize = stack.calloc(ValueLayout.JAVA_LONG);
            states.setIndexCount(builder.end(pDataSize, null, null));
            final long dataSize = pDataSize.get(ValueLayout.JAVA_LONG, 0);
            // if never compiled
            if (!states.hadCompiled()) {
                states.setData(MemoryUtil.calloc(1, dataSize));
                states.setIndexData(MemoryUtil.calloc(states.indexCount(), ValueLayout.JAVA_INT));
                states.setExpanded(true);
            } else {
                // if expanded
                if (dataSize > states.data().byteSize()) {
                    states.setData(MemoryUtil.realloc(states.data(), dataSize));
                    states.setExpanded(true);
                }
                final long indexDataSize = (long) states.indexCount() << 2;
                if (indexDataSize > states.indexData().byteSize()) {
                    states.setIndexData(MemoryUtil.realloc(states.indexData(), indexDataSize));
                    states.setExpanded(true);
                }
            }
            builder.end(null, states.data(), states.indexData());
        }
    }

    public void render() {
        if (states.hadCompiled()) {
            if (!states.uploaded()) {
                final int vertexArrayBinding = GLStateMgr.vertexArrayBinding();
                final int arrayBufferBinding = GLStateMgr.arrayBufferBinding();
                RenderSystem.bindVertexArray(states.vao());
                RenderSystem.bindArrayBuffer(states.vbo());
                if (states.expanded()) {
                    GL.bufferData(GL.ARRAY_BUFFER, states.data(), GL.DYNAMIC_DRAW);
                    states.layer().layout().specifyAttributes();
                } else {
                    GL.bufferSubData(GL.ARRAY_BUFFER, 0, states.data());
                }
                RenderSystem.bindArrayBuffer(arrayBufferBinding);
                if (states.expanded()) {
                    GL.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, states.ebo());
                    GL.bufferData(GL.ELEMENT_ARRAY_BUFFER, states.indexData(), GL.DYNAMIC_DRAW);
                } else {
                    GL.bufferSubData(GL.ELEMENT_ARRAY_BUFFER, 0, states.indexData());
                }
                RenderSystem.bindVertexArray(vertexArrayBinding);
                states.setExpanded(false);
                states.setUploaded(true);
            }
            final int vertexArrayBinding = GLStateMgr.vertexArrayBinding();
            RenderSystem.bindVertexArray(states.vao());
            GL.drawElements(GL.TRIANGLES, states.indexCount(), GL.UNSIGNED_INT, MemorySegment.NULL);
            RenderSystem.bindVertexArray(vertexArrayBinding);
        }
    }

    public void markDirty() {
        dirty.set(true);
    }

    public void markNotDirty() {
        dirty.set(false);
    }

    public boolean dirty() {
        return dirty.get();
    }

    public void setSubmitted(boolean submitted) {
        this.submitted.set(submitted);
    }

    public boolean submitted() {
        return submitted.get();
    }

    public CompileStates states() {
        return states;
    }

    public double distanceSqr(Entity entity) {
        return entity.position().distanceSquared(x(), y(), z());
    }

    public boolean isVisible(FrustumIntersection frustum) {
        return frustum.testAab(x0(), y0(), z0(), x1() + 1, y1() + 1, z1() + 1);
    }

    @Override
    public void close() {
        states.close();
    }
}
