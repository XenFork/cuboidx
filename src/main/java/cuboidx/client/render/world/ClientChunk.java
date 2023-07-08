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

import java.lang.foreign.MemorySegment;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class ClientChunk extends Chunk implements AutoCloseable {
    private final CompileStates states = new CompileStates();
    private final CuboidX client;
    private boolean dirty = true;

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
        private final int vao, vbo, ebo;
        private int indexCount = 0;
        private boolean hasCompiled = false;

        private CompileStates() {
            vao = GL.genVertexArray();
            vbo = GL.genBuffer();
            ebo = GL.genBuffer();
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
            this.indexCount = indexCount;
        }

        public int indexCount() {
            return indexCount;
        }

        public void markCompiled() {
            this.hasCompiled = true;
        }

        public boolean hasCompiled() {
            return hasCompiled;
        }

        @Override
        public void close() {
            RenderSystem.deleteVertexArray(vao);
            RenderSystem.deleteArrayBuffer(vbo);
            GL.deleteBuffer(ebo);
        }
    }

    public void compile(BufferedVertexBuilder builder) {
        if (!dirty) return;
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
        states.setIndexCount(
            builder.end(states.vao(), states.vbo(), states.ebo(), states.hasCompiled()));
        states.markCompiled();
        dirty = false;
    }

    public void render() {
        if (states.hasCompiled()) {
            final int vertexArrayBinding = GLStateMgr.vertexArrayBinding();
            RenderSystem.bindVertexArray(states.vao());
            GL.drawElements(GL.TRIANGLES, states.indexCount(), GL.UNSIGNED_INT, MemorySegment.NULL);
            RenderSystem.bindVertexArray(vertexArrayBinding);
        }
    }

    public boolean dirty() {
        return dirty;
    }

    @Override
    public void close() {
        states.close();
    }
}
