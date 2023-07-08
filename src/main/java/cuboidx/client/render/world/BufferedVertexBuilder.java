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

import cuboidx.client.gl.GLDrawMode;
import cuboidx.client.gl.GLStateMgr;
import cuboidx.client.gl.RenderSystem;
import cuboidx.client.render.VertexBuilder;
import cuboidx.client.render.VertexFormat;
import cuboidx.client.render.VertexLayout;
import cuboidx.util.math.MathUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import overrungl.opengl.GL;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A buffered {@link VertexBuilder}.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class BufferedVertexBuilder implements Poolable, VertexBuilder, AutoCloseable {
    private static final Logger logger = LogManager.getLogger();
    private final VertexLayout vertexLayout;
    private final SequenceLayout sequenceLayout;
    private final Map<VertexFormat.VarHandleKey, VarHandle> varHandleMap;
    private final int verticesSize;
    private final int indicesSize;
    private final Arena arena;
    private final MemorySegment data;
    private final MemorySegment indexData;
    private float x, y, z;
    private float r, g, b, a;
    private float u, v;
    private int vertexCount = 0;
    private int indexCount = 0;
    private GLDrawMode drawMode = GLDrawMode.TRIANGLES;
    private boolean drawing = false;

    public BufferedVertexBuilder(VertexLayout layout, int verticesSize, int indicesSize) {
        this.vertexLayout = layout;
        this.verticesSize = verticesSize;
        this.indicesSize = indicesSize;
        this.sequenceLayout = MemoryLayout.sequenceLayout(verticesSize, layout.layout());
        this.varHandleMap = HashMap.newHashMap(layout.varHandleCount());
        for (VertexFormat format : layout.formats()) {
            for (int i = 0; i < format.size(); i++) {
                this.varHandleMap.put(format.varHandleKey(i),
                    sequenceLayout.varHandle(PathElement.sequenceElement(),
                        format.pathElement(),
                        PathElement.sequenceElement(i)));
            }
        }
        this.arena = Arena.ofConfined(); // TODO: 2023/7/8 confined or shared ?
        this.data = arena.allocate(sequenceLayout);
        this.indexData = arena.allocateArray(ValueLayout.JAVA_INT, indicesSize);
    }

    @Override
    public void reset() {
        x = 0.0f;
        y = 0.0f;
        z = 0.0f;
        r = 0.0f;
        g = 0.0f;
        b = 0.0f;
        a = 0.0f;
        u = 0.0f;
        v = 0.0f;
        drawMode = GLDrawMode.TRIANGLES;
        drawing = false;
        clear();
    }

    private void clear() {
        vertexCount = 0;
        indexCount = 0;
    }

    public void begin(GLDrawMode mode) {
        if (drawing) {
            logger.warn("Calling begin while drawing; ignoring");
            return;
        }
        clear();
        this.drawMode = mode;
        drawing = true;
    }

    // TODO: 2023/7/8 Value object
    public int end(int vao, int vbo, int ebo, boolean hasCompiled) {
        if (!drawing) {
            logger.warn("Calling end while not drawing; ignoring");
            return 0;
        }
        if (vertexCount == 0 || indexCount == 0) return 0;

        final int vertexArrayBinding = GLStateMgr.vertexArrayBinding();
        final int arrayBufferBinding = GLStateMgr.arrayBufferBinding();
        RenderSystem.bindVertexArray(vao);
        RenderSystem.bindArrayBuffer(vbo);
        if (!hasCompiled) {
            GL.bufferData(GL.ARRAY_BUFFER, data, GL.DYNAMIC_DRAW);
            vertexLayout.specifyAttributes();
        } else {
            GL.bufferSubData(GL.ARRAY_BUFFER, 0, sequenceLayout.byteOffset(PathElement.sequenceElement(vertexCount)), data);
        }
        RenderSystem.bindArrayBuffer(arrayBufferBinding);
        if (!hasCompiled) {
            GL.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, ebo);
            GL.bufferData(GL.ELEMENT_ARRAY_BUFFER, indexData, GL.DYNAMIC_DRAW);
        } else {
            GL.bufferSubData(GL.ELEMENT_ARRAY_BUFFER, 0, (long) indexCount << 2, indexData);
        }
        RenderSystem.bindVertexArray(vertexArrayBinding);

        drawing = false;
        final int count = indexCount;
        vertexCount = 0;
        indexCount = 0;
        return count;
    }

    @Override
    public void indices(int... indices) {
        for (int index : indices) {
            if ((indexCount % drawMode.indexCount()) == 0 &&
                (indexCount + drawMode.indexCount()) > indicesSize) {
                logger.warn("index count {} exceeds the limit {}", indexCount, indicesSize);
                return;
            }
            indexData.setAtIndex(ValueLayout.JAVA_INT, indexCount, vertexCount + index);
            indexCount++;
        }
    }

    public void indicesByMode() {
        indices(drawMode.indices());
    }

    @Override
    public BufferedVertexBuilder vertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public BufferedVertexBuilder color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }

    @Override
    public BufferedVertexBuilder texture(float u, float v) {
        this.u = u;
        this.v = v;
        return this;
    }

    @Override
    public void emit() {
        if ((vertexCount % drawMode.vertexCount()) == 0) {
            if ((vertexCount + drawMode.vertexCount()) > verticesSize) {
                logger.warn("vertex count {} exceeds limit: {}", vertexCount, verticesSize);
                return;
            }
        }
        final long count = vertexCount;
        final List<VertexFormat> formats = vertexLayout.formats();
        final int pos = vertexLayout.indexOf(VertexFormat.POSITION);
        final int color = vertexLayout.indexOf(VertexFormat.COLOR);
        final int uv0 = vertexLayout.indexOf(VertexFormat.UV0);
        if (pos != -1) {
            final VertexFormat format = formats.get(pos);
            varHandle(format, 0).set(data, count, x);
            varHandle(format, 1).set(data, count, y);
            varHandle(format, 2).set(data, count, z);
        }
        if (color != -1) {
            final VertexFormat format = formats.get(color);
            varHandle(format, 0).set(data, count, MathUtil.denormalize(r));
            varHandle(format, 1).set(data, count, MathUtil.denormalize(g));
            varHandle(format, 2).set(data, count, MathUtil.denormalize(b));
            varHandle(format, 3).set(data, count, MathUtil.denormalize(a));
        }
        if (uv0 != -1) {
            final VertexFormat format = formats.get(uv0);
            varHandle(format, 0).set(data, count, u);
            varHandle(format, 1).set(data, count, v);
        }
        vertexCount++;
    }

    private VarHandle varHandle(VertexFormat format, int index) {
        return varHandleMap.get(format.varHandleKey(index));
    }

    @Override
    public void close() {
        arena.close();
    }
}
