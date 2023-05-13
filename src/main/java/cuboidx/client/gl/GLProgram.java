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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cuboidx.client.render.VertexLayout;
import cuboidx.util.FileUtil;
import cuboidx.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.overrun.glib.gl.GL;

import java.lang.foreign.Arena;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class GLProgram implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger();
    private final int id;
    private final Arena uniformArena = Arena.ofConfined();
    private final Map<String, GLUniform> uniformMap = new HashMap<>();

    private GLProgram() {
        this.id = GL.createProgram();
    }

    public static GLProgram load(ResourceLocation location, VertexLayout layout) {
        try (Arena arena = Arena.ofConfined()) {
            final GLProgram program = new GLProgram();

            final JsonObject json = JsonParser.parseString(FileUtil.readString(
                location.toPath(ResourceLocation.ASSETS, ResourceLocation.SHADER) + ".json")
            ).getAsJsonObject();

            final int vsh = compileShader(arena, GL.VERTEX_SHADER, "vertex",
                FileUtil.readString(ResourceLocation.of(json.get("vertex").getAsString())
                    .toPath(ResourceLocation.ASSETS, ResourceLocation.SHADER) + ".vert"));
            if (vsh < 0) return null;
            final int fsh = compileShader(arena, GL.FRAGMENT_SHADER, "fragment",
                FileUtil.readString(ResourceLocation.of(json.get("fragment").getAsString())
                    .toPath(ResourceLocation.ASSETS, ResourceLocation.SHADER) + ".frag"));
            if (fsh < 0) return null;

            GL.attachShader(program.id(), vsh);
            GL.attachShader(program.id(), fsh);
            layout.bindLocations(arena, program.id());
            GL.linkProgram(program.id());
            final boolean failed = GL.getProgrami(program.id(), GL.LINK_STATUS) == GL.FALSE;
            if (failed) {
                logger.error("Failed to link the program: {}", GL.getProgramInfoLog(arena, program.id));
            }
            GL.detachShader(program.id(), vsh);
            GL.detachShader(program.id(), fsh);
            GL.deleteShader(vsh);
            GL.deleteShader(fsh);
            if (failed) return null;

            final JsonObject uniforms = json.getAsJsonObject("uniforms");
            for (var e : uniforms.entrySet()) {
                program.initUniform(arena, e.getValue().getAsJsonObject(), e.getKey());
            }

            return program;
        } catch (Exception e) {
            logger.error("Failed to load the program", e);
            return null;
        }
    }

    private static int compileShader(Arena arena, int type, String typeName, String src) {
        final int shader = GL.createShader(type);
        GL.shaderSource(arena, shader, src);
        GL.compileShader(shader);
        if (GL.getShaderi(shader, GL.COMPILE_STATUS) == GL.FALSE) {
            logger.error("Failed to compile the {} shader: {}", typeName, GL.getShaderInfoLog(arena, shader));
            return -1;
        }
        return shader;
    }

    private void initUniform(Arena arena, JsonObject json, String name) {
        final GLUniform.Type type = GLUniform.Type.byName(json.get("type").getAsString());
        final JsonArray values = json.getAsJsonArray("values");
        final GLUniform uniform = Objects.requireNonNull(createUniform(arena, name, type), "No given uniform '" + name + "' found");
        switch (type) {
            case INT -> uniform.set(values.get(0).getAsInt());
            case VEC4 -> uniform.set(
                values.get(0).getAsFloat(),
                values.get(1).getAsFloat(),
                values.get(2).getAsFloat(),
                values.get(3).getAsFloat()
            );
            case MAT4 -> uniform.set(
                values.get(0).getAsFloat(),
                values.get(1).getAsFloat(),
                values.get(2).getAsFloat(),
                values.get(3).getAsFloat(),
                values.get(4).getAsFloat(),
                values.get(5).getAsFloat(),
                values.get(6).getAsFloat(),
                values.get(7).getAsFloat(),
                values.get(8).getAsFloat(),
                values.get(9).getAsFloat(),
                values.get(10).getAsFloat(),
                values.get(11).getAsFloat(),
                values.get(12).getAsFloat(),
                values.get(13).getAsFloat(),
                values.get(14).getAsFloat(),
                values.get(15).getAsFloat()
            );
        }
    }

    private GLUniform createUniform(Arena arena, String name, GLUniform.Type type) {
        final int location = GL.getUniformLocation(arena, id, name);
        if (location == -1) return null;

        final GLUniform uniform = new GLUniform(this, location, type);
        uniformMap.put(name, uniform);
        return uniform;
    }

    public void specifyUniforms() {
        uniformMap.values().forEach(GLUniform::specify);
    }

    public int id() {
        return id;
    }

    public Arena uniformArena() {
        return uniformArena;
    }

    @Override
    public void close() {
        GL.deleteProgram(id);
        uniformArena.close();
    }
}
