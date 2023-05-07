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

package cuboidx.client.gl;

import org.joml.Matrix4fc;
import org.joml.Vector4fc;
import org.overrun.glib.gl.GL;
import org.overrun.glib.gl.GLLoader;
import org.overrun.glib.joml.Matrixn;
import org.overrun.glib.joml.Vectorn;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Locale;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class GLUniform {
    private final GLProgram program;
    private final int location;
    private final Type type;
    private final MemorySegment data;
    private boolean dirty = true;

    public GLUniform(GLProgram program, int location, Type type) {
        this.program = program;
        this.location = location;
        this.type = type;
        this.data = program.uniformArena().allocate(type.layout());
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    public enum Type {
        VEC4(GLDataType.FLOAT, Vectorn.VEC4F),
        MAT4(GLDataType.FLOAT, Matrixn.MAT4F),
        ;

        private final GLDataType dataType;
        private final MemoryLayout layout;

        Type(GLDataType dataType, MemoryLayout layout) {
            this.dataType = dataType;
            this.layout = layout;
        }

        public static Type byName(String name) {
            return valueOf(name.toUpperCase(Locale.ROOT));
        }

        public GLDataType dataType() {
            return dataType;
        }

        public MemoryLayout layout() {
            return layout;
        }
    }

    public void set(float x, float y, float z, float w) {
        markDirty();
        data.set(ValueLayout.JAVA_FLOAT, 0, x);
        data.set(ValueLayout.JAVA_FLOAT, 4, y);
        data.set(ValueLayout.JAVA_FLOAT, 8, z);
        data.set(ValueLayout.JAVA_FLOAT, 12, w);
    }

    public void set(float... v) {
        markDirty();
        MemorySegment.copy(v, 0, data, ValueLayout.JAVA_FLOAT, 0, v.length);
    }

    public void set(Vector4fc v) {
        markDirty();
        Vectorn.put(v, data);
    }

    public void set(Matrix4fc m) {
        markDirty();
        Matrixn.put(m, data);
    }

    private void markDirty() {
        dirty = true;
    }

    public void specify() {
        if (!dirty) return;
        dirty = false;
        if (GLLoader.getExtCapabilities().GL_ARB_separate_shader_objects) {
            switch (type) {
                case VEC4 -> GL.programUniform4fv(program.id(), location, 1, data);
                case MAT4 -> GL.programUniformMatrix4fv(program.id(), location, 1, false, data);
            }
        } else {
            final int currentProgram = GLStateMgr.currentProgram();
            RenderSystem.useProgram(program);
            switch (type) {
                case VEC4 -> GL.uniform4fv(location, 1, data);
                case MAT4 -> GL.uniformMatrix4fv(location, 1, false, data);
            }
            RenderSystem.useProgram(currentProgram);
        }
    }

    public GLProgram program() {
        return program;
    }

    public int location() {
        return location;
    }

    public Type type() {
        return type;
    }

    public MemorySegment data() {
        return data;
    }
}
