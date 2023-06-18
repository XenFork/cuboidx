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

import org.overrun.gl.opengl.GL;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class GLStateMgr {
    private static int currentProgram = 0;
    private static int vertexArrayBinding = 0;
    private static int arrayBufferBinding = 0;
    private static int textureBinding2D = 0;
    private static boolean depthTestEnabled = false;
    private static int depthFunc = GL.LESS;
    private static boolean cullFaceEnabled = false;
    private static int cullFaceMode = GL.BACK;

    public static void useProgram(int program) {
        if (currentProgram != program) {
            currentProgram = program;
            GL.useProgram(program);
        }
    }

    public static int currentProgram() {
        return currentProgram;
    }

    public static void bindVertexArray(int array) {
        if (vertexArrayBinding != array) {
            vertexArrayBinding = array;
            GL.bindVertexArray(array);
        }
    }

    public static int vertexArrayBinding() {
        return vertexArrayBinding;
    }

    public static void bindArrayBuffer(int buffer) {
        if (arrayBufferBinding != buffer) {
            arrayBufferBinding = buffer;
            GL.bindBuffer(GL.ARRAY_BUFFER, buffer);
        }
    }

    public static int arrayBufferBinding() {
        return arrayBufferBinding;
    }

    public static void bindTexture2D(int texture) {
        if (textureBinding2D != texture) {
            textureBinding2D = texture;
            GL.bindTexture(GL.TEXTURE_2D, texture);
        }
    }

    public static int textureBinding2D() {
        return textureBinding2D;
    }

    public static void enableDepthTest() {
        if (!depthTestEnabled) {
            depthTestEnabled = true;
            GL.enable(GL.DEPTH_TEST);
        }
    }

    public static void disableDepthTest() {
        if (depthTestEnabled) {
            depthTestEnabled = false;
            GL.disable(GL.DEPTH_TEST);
        }
    }

    public static boolean depthTestEnabled() {
        return depthTestEnabled;
    }

    public static void depthFunc(int func) {
        if (depthFunc != func) {
            depthFunc = func;
            GL.depthFunc(func);
        }
    }

    public static int depthFunc() {
        return depthFunc;
    }

    public static void enableCullFace() {
        if (!cullFaceEnabled) {
            cullFaceEnabled = true;
            GL.enable(GL.CULL_FACE);
        }
    }

    public static void disableCullFace() {
        if (cullFaceEnabled) {
            cullFaceEnabled = false;
            GL.disable(GL.CULL_FACE);
        }
    }

    public static boolean cullFaceEnabled() {
        return cullFaceEnabled;
    }

    public static void cullFace(int mode) {
        if (cullFaceMode != mode) {
            cullFaceMode = mode;
            GL.cullFace(mode);
        }
    }

    public static int cullFaceMode() {
        return cullFaceMode;
    }
}
