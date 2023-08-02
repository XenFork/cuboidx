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

import cuboidx.client.texture.Texture2D;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.function.Consumer;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class RenderSystem {
    private static final Matrix4fStack projectionMatrix = new Matrix4fStack(4);
    private static final Matrix4fStack viewMatrix = new Matrix4fStack(32);
    private static final Matrix4fStack modelMatrix = new Matrix4fStack(32);
    private static final Matrix4f projectionViewMatrix = new Matrix4f();
    private static final Matrix4f modelViewMatrix = new Matrix4f();
    private static final FrustumIntersection frustum = new FrustumIntersection();

    ///////////////////////////////////////////////////////////////////////////
    // OpenGL state manage
    ///////////////////////////////////////////////////////////////////////////

    public static void useProgram(int program) {
        GLStateMgr.useProgram(program);
    }

    public static GLProgram useProgram(GLProgram program) {
        useProgram(program.id());
        return program;
    }

    public static GLProgram useProgram(GLProgram program, Consumer<GLProgram> consumer) {
        consumer.accept(useProgram(program));
        return program;
    }

    public static void bindVertexArray(int array) {
        GLStateMgr.bindVertexArray(array);
    }

    public static void deleteVertexArray(int array) {
        GLStateMgr.deleteVertexArray(array);
    }

    public static void bindArrayBuffer(int buffer) {
        GLStateMgr.bindArrayBuffer(buffer);
    }

    public static void deleteArrayBuffer(int buffer) {
        GLStateMgr.deleteArrayBuffer(buffer);
    }

    public static void bindTexture2D(int texture) {
        GLStateMgr.bindTexture2D(texture);
    }

    public static void bindTexture2D(Texture2D texture) {
        bindTexture2D(texture.id());
    }

    public static void enableDepthTest() {
        GLStateMgr.enableDepthTest();
    }

    public static void disableDepthTest() {
        GLStateMgr.disableDepthTest();
    }

    public static void depthFunc(int func) {
        GLStateMgr.depthFunc(func);
    }

    public static void enableCullFace() {
        GLStateMgr.enableCullFace();
    }

    public static void disableCullFace() {
        GLStateMgr.disableCullFace();
    }

    public static void cullFace(int mode) {
        GLStateMgr.cullFace(mode);
    }

    public static void lineWidth(float width) {
        GLStateMgr.lineWidth(width);
    }

    public static void enableLineSmooth() {
        GLStateMgr.enableLineSmooth();
    }

    public static void disableLineSmooth() {
        GLStateMgr.disableLineSmooth();
    }

    public static void polygonOffset(float factor, float units) {
        GLStateMgr.polygonOffset(factor, units);
    }

    public static void enablePolygonOffsetLine() {
        GLStateMgr.enablePolygonOffsetLine();
    }

    public static void disablePolygonOffsetLine() {
        GLStateMgr.disablePolygonOffsetLine();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Render system
    ///////////////////////////////////////////////////////////////////////////

    public static Matrix4fStack projectionMatrix() {
        return projectionMatrix;
    }

    public static Matrix4fStack viewMatrix() {
        return viewMatrix;
    }

    public static Matrix4fStack modelMatrix() {
        return modelMatrix;
    }

    public static Matrix4f projectionViewMatrix() {
        return projectionMatrix.mul(viewMatrix, projectionViewMatrix);
    }

    public static Matrix4f modelViewMatrix() {
        return viewMatrix.mul(modelMatrix, modelViewMatrix);
    }

    public static void updateFrustum() {
        frustum.set(projectionViewMatrix());
    }

    public static FrustumIntersection frustum() {
        return frustum;
    }
}
