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
import cuboidx.util.math.Direction;
import cuboidx.world.World;
import cuboidx.world.block.BlockType;
import cuboidx.world.chunk.Chunk;
import overrungl.opengl.GL;
import overrungl.util.MemoryStack;
import overrungl.util.MemoryUtil;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
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
                       int x, int y, int z,
                       int x0, int y0, int z0,
                       int x1, int y1, int z1) {
        super(world, x, y, z, x0, y0, z0, x1, y1, z1);
        this.client = client;
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    public static final class CompileStates implements AutoCloseable {
        private final BlockRenderLayer layer = BlockRenderLayer.OPAQUE;
        private final int vao, vbo, ebo;
        private final AtomicInteger indexCount = new AtomicInteger();
        private final AtomicBoolean compiled = new AtomicBoolean();
        private final AtomicBoolean hadCompiled = new AtomicBoolean();
        private boolean uploaded = false;
        private MemorySegment data;
        private MemorySegment indexData;

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

        public void setCompiled(boolean compiled) {
            this.compiled.set(compiled);
        }

        /**
         * Is this chunk compiled?
         */
        public boolean compiled() {
            return compiled.get();
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
            this.uploaded = uploaded;
        }

        public boolean uploaded() {
            return uploaded;
        }

        public void setData(MemorySegment data) {
            this.data = data;
        }

        public MemorySegment data() {
            return data;
        }

        public void setIndexData(MemorySegment indexData) {
            this.indexData = indexData;
        }

        public MemorySegment indexData() {
            return indexData;
        }

        @Override
        public void close() {
            RenderSystem.deleteVertexArray(vao);
            RenderSystem.deleteArrayBuffer(vbo);
            GL.deleteBuffer(ebo);
            MemoryUtil.free(data);
            MemoryUtil.free(indexData);
        }
    }

    public void compile(BufferedVertexBuilder builder) {
        if (!dirty()) return;
        states.setCompiled(false);
        final BlockRenderer renderer = client.blockRenderer();
        builder.begin(GLDrawMode.TRIANGLES);
        for (Direction direction : Direction.list()) {
            for (int x = x0; x <= x1; x++) {
                for (int y = y0; y <= y1; y++) {
                    for (int z = z0; z <= z1; z++) {
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
            }
            // if expanded
            else if (dataSize > states.data().byteSize()) {
                states.setData(MemoryUtil.realloc(states.data(), dataSize));
            }
            builder.end(null, states.data(), states.indexData());
        }
        states.setCompiled(true);
        dirty.set(false);
    }

    public void render() {
        if (states.compiled()) {
            if (!states.uploaded()) {
                final int vertexArrayBinding = GLStateMgr.vertexArrayBinding();
                final int arrayBufferBinding = GLStateMgr.arrayBufferBinding();
                RenderSystem.bindVertexArray(states.vao());
                RenderSystem.bindArrayBuffer(states.vbo());
                if (!states.hadCompiled()) {
                    GL.bufferData(GL.ARRAY_BUFFER, states.data(), GL.DYNAMIC_DRAW);
                    states.layer().layout().specifyAttributes();
                } else {
                    GL.bufferSubData(GL.ARRAY_BUFFER, 0, states.data());
                }
                RenderSystem.bindArrayBuffer(arrayBufferBinding);
                if (!states.hadCompiled()) {
                    GL.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, states.ebo());
                    GL.bufferData(GL.ELEMENT_ARRAY_BUFFER, states.indexData(), GL.DYNAMIC_DRAW);
                } else {
                    GL.bufferSubData(GL.ELEMENT_ARRAY_BUFFER, 0, states.indexData());
                }
                RenderSystem.bindVertexArray(vertexArrayBinding);
                states.markCompiled();
                states.setUploaded(true);
            }
            final int vertexArrayBinding = GLStateMgr.vertexArrayBinding();
            RenderSystem.bindVertexArray(states.vao());
            GL.drawElements(GL.TRIANGLES, states.indexCount(), GL.UNSIGNED_INT, MemorySegment.NULL);
            RenderSystem.bindVertexArray(vertexArrayBinding);
        }
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

    @Override
    public void close() {
        states.close();
    }
}
