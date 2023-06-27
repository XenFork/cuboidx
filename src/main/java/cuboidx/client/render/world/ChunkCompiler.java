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

import cuboidx.client.gl.GLStateMgr;
import cuboidx.client.gl.RenderSystem;
import cuboidx.client.render.VertexBuilder;
import cuboidx.client.render.VertexFormat;
import cuboidx.client.render.VertexLayout;
import cuboidx.util.math.MathUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.overrun.gl.opengl.GL;
import org.overrun.gl.util.MemoryUtil;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class ChunkCompiler implements VertexBuilder, AutoCloseable {
    private static final Logger logger = LogManager.getLogger();
    private static final int MAX_INDEX_COUNT = 64000;
    private static final VertexLayout VERTEX_LAYOUT = VertexLayout.POSITION_COLOR_TEXTURE;
    private static final SequenceLayout LAYOUT = MemoryLayout.sequenceLayout(
        50000,
        VERTEX_LAYOUT.layout()
    );
    // Position, Color, UV0  x, y, z, r, g, b, a, u, v
    private static final VarHandle px = varHandle(VertexFormat.POSITION, 0);
    private static final VarHandle py = varHandle(VertexFormat.POSITION, 1);
    private static final VarHandle pz = varHandle(VertexFormat.POSITION, 2);
    private static final VarHandle cr = varHandle(VertexFormat.COLOR, 0);
    private static final VarHandle cg = varHandle(VertexFormat.COLOR, 1);
    private static final VarHandle cb = varHandle(VertexFormat.COLOR, 2);
    private static final VarHandle ca = varHandle(VertexFormat.COLOR, 3);
    private static final VarHandle tu = varHandle(VertexFormat.UV0, 0);
    private static final VarHandle tv = varHandle(VertexFormat.UV0, 1);
    private final MemorySegment data = MemoryUtil.calloc(1, LAYOUT);
    private final MemorySegment indexData = MemoryUtil.calloc(MAX_INDEX_COUNT, ValueLayout.JAVA_INT);
    private float x, y, z;
    private float r, g, b, a;
    private float u, v;
    private int vertexCount = 0;
    private int indexCount = 0;
    @Deprecated
    private ClientChunk.CompileStates states;

    private static VarHandle varHandle(VertexFormat format, long index) {
        return LAYOUT.varHandle(
            PathElement.sequenceElement(),
            format.pathElement(),
            PathElement.sequenceElement(index)
        );
    }

    public void begin(ClientChunk.CompileStates states) {
        vertexCount = 0;
        indexCount = 0;
        this.states = states;
    }

    public void end() {
        if (vertexCount == 0 || indexCount == 0) return;

        final int vao = states.vao();
        final int vbo = states.vbo();
        final int ebo = states.ebo();
        final boolean hasCompiled = states.hasCompiled();
        final int vertexArrayBinding = GLStateMgr.vertexArrayBinding();
        final int arrayBufferBinding = GLStateMgr.arrayBufferBinding();
        RenderSystem.bindVertexArray(vao);
        RenderSystem.bindArrayBuffer(vbo);
        if (!hasCompiled) {
            GL.bufferData(GL.ARRAY_BUFFER, data, GL.DYNAMIC_DRAW);
            VERTEX_LAYOUT.specifyAttributes();
        } else {
            GL.bufferSubData(GL.ARRAY_BUFFER, 0, LAYOUT.byteOffset(PathElement.sequenceElement(vertexCount)), data);
        }
        RenderSystem.bindArrayBuffer(arrayBufferBinding);
        if (!hasCompiled) {
            GL.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, ebo);
            GL.bufferData(GL.ELEMENT_ARRAY_BUFFER, indexData, GL.DYNAMIC_DRAW);
        } else {
            GL.bufferSubData(GL.ELEMENT_ARRAY_BUFFER, 0, (long) indexCount << 2, indexData);
        }
        RenderSystem.bindVertexArray(vertexArrayBinding);
        states.setIndexCount(indexCount);
        states.markCompiled();

        vertexCount = 0;
        indexCount = 0;
    }

    @Override
    public void indices(int... indices) {
        for (int index : indices) {
            if ((indexCount % 3) == 0 &&
                (indexCount + 3) > MAX_INDEX_COUNT) {
                end();
                logger.warn("Index count exceeds limit: " + MAX_INDEX_COUNT);
                return;
            }
            indexData.setAtIndex(ValueLayout.JAVA_INT, indexCount, vertexCount + index);
            indexCount++;
        }
    }

    @Override
    public ChunkCompiler vertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public ChunkCompiler color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }

    @Override
    public ChunkCompiler texture(float u, float v) {
        this.u = u;
        this.v = v;
        return this;
    }

    @Override
    public void emit() {
        if ((vertexCount % 3) == 0) {
            if ((vertexCount + 3) > LAYOUT.elementCount()) {
                end();
                logger.warn("Vertex count exceeds limit: {}", LAYOUT.elementCount());
                return;
            }
        }
        final long count = vertexCount;
        px.set(data, count, x);
        py.set(data, count, y);
        pz.set(data, count, z);
        cr.set(data, count, MathUtil.denormalize(r));
        cg.set(data, count, MathUtil.denormalize(g));
        cb.set(data, count, MathUtil.denormalize(b));
        ca.set(data, count, MathUtil.denormalize(a));
        tu.set(data, count, u);
        tv.set(data, count, v);
        vertexCount++;
    }

    @Override
    public void close() {
        MemoryUtil.free(data);
        MemoryUtil.free(indexData);
    }
}
