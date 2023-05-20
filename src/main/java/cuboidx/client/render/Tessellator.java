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
import cuboidx.client.gl.GLStateMgr;
import cuboidx.client.gl.RenderSystem;
import cuboidx.util.MathUtil;
import org.overrun.glib.gl.GL;
import org.overrun.glib.util.MemoryUtil;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

/**
 * A vertex builder with builtin vertex buffer objects.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class Tessellator implements VertexBuilder {
    private static final int MAX_INDEX_COUNT = 30000;
    private static final VertexLayout VERTEX_LAYOUT = VertexLayout.POSITION_COLOR_TEXTURE;
    private static final SequenceLayout LAYOUT = MemoryLayout.sequenceLayout(
        30000,
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
    private static Tessellator instance;
    private final MemorySegment data = MemoryUtil.calloc(1, LAYOUT);
    private final MemorySegment indexData = MemoryUtil.calloc(MAX_INDEX_COUNT, ValueLayout.JAVA_INT);
    private float x, y, z;
    private float r, g, b, a;
    private float u, v;
    private GLDrawMode mode = GLDrawMode.TRIANGLES;
    private boolean autoIndices = true;
    private int vertexCount = 0;
    private int indexCount = 0;
    private int vao, vbo, ebo;

    private Tessellator() {
    }

    public static Tessellator getInstance() {
        if (instance == null) {
            instance = new Tessellator();
        }
        return instance;
    }

    public static void free() {
        if (instance != null) {
            instance.close();
        }
    }

    private static VarHandle varHandle(VertexFormat format, long index) {
        return LAYOUT.varHandle(
            PathElement.sequenceElement(),
            format.pathElement(),
            PathElement.sequenceElement(index)
        );
    }

    public void begin(GLDrawMode mode) {
        this.mode = mode;
        vertexCount = 0;
        indexCount = 0;
    }

    public void end() {
        flush();
    }

    public void flush() {
        if (vertexCount == 0 || indexCount == 0) return;

        final boolean noVao = vao == 0;
        final boolean noVbo = vbo == 0;
        final boolean noEbo = ebo == 0;
        if (noVao) vao = GL.genVertexArray();
        if (noVbo) vbo = GL.genBuffer();
        if (noEbo) ebo = GL.genBuffer();

        final int vertexArrayBinding = GLStateMgr.vertexArrayBinding();
        final int arrayBufferBinding = GLStateMgr.arrayBufferBinding();
        RenderSystem.bindVertexArray(vao);
        RenderSystem.bindArrayBuffer(vbo);
        if (noVbo) {
            GL.bufferData(GL.ARRAY_BUFFER, data, GL.STREAM_DRAW);
            VERTEX_LAYOUT.specifyAttributes();
        } else {
            GL.bufferSubData(GL.ARRAY_BUFFER, 0, LAYOUT.byteOffset(PathElement.sequenceElement(vertexCount)), data);
        }
        RenderSystem.bindArrayBuffer(arrayBufferBinding);
        if (noEbo) {
            GL.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, ebo);
            GL.bufferData(GL.ELEMENT_ARRAY_BUFFER, indexData, GL.STREAM_DRAW);
        } else {
            GL.bufferSubData(GL.ELEMENT_ARRAY_BUFFER, 0, (long) indexCount << 2, indexData);
        }
        GL.drawElements(mode.enumValue(), indexCount, GL.UNSIGNED_INT, MemorySegment.NULL);
        RenderSystem.bindVertexArray(vertexArrayBinding);

        vertexCount = 0;
        indexCount = 0;
    }

    public void enableAutoIndices() {
        autoIndices = true;
    }

    public void disableAutoIndices() {
        autoIndices = false;
    }

    public void indices(int... indices) {
        for (int index : indices) {
            if ((indexCount % mode.indexCount()) == 0 &&
                (indexCount + mode.indexCount()) > MAX_INDEX_COUNT) {
                flush();
            }
            indexData.setAtIndex(ValueLayout.JAVA_INT, indexCount, vertexCount + index);
            indexCount++;
        }
    }

    @Override
    public Tessellator vertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public Tessellator color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }

    @Override
    public Tessellator texture(float u, float v) {
        this.u = u;
        this.v = v;
        return this;
    }

    @Override
    public void emit() {
        if ((vertexCount % mode.vertexCount()) == 0) {
            if (autoIndices) {
                indices(mode.indices());
            }
            if ((vertexCount + mode.vertexCount()) > LAYOUT.elementCount()) {
                flush();
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

    private void close() {
        MemoryUtil.free(data);
        MemoryUtil.free(indexData);
        if (GL.isVertexArray(vao)) GL.deleteVertexArray(vao);
        if (GL.isBuffer(vbo)) GL.deleteBuffer(vbo);
        if (GL.isBuffer(ebo)) GL.deleteBuffer(ebo);
    }
}
