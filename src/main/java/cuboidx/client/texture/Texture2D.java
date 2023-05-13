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

package cuboidx.client.texture;

import cuboidx.client.gl.GLStateMgr;
import cuboidx.client.gl.RenderSystem;
import cuboidx.util.ResourceLocation;
import org.overrun.glib.gl.GL;
import org.overrun.glib.stb.STBImage;

/**
 * @author squid233
 * @since 0.1.0
 */
public class Texture2D implements AutoCloseable {
    private final int id;

    private Texture2D() {
        this.id = GL.genTexture();
    }

    public static Texture2D load(ResourceLocation location) {
        final Texture2D texture = new Texture2D();
        final int textureBinding2D = GLStateMgr.textureBinding2D();
        RenderSystem.bindTexture2D(texture);
        GL.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MIN_FILTER, GL.NEAREST_MIPMAP_NEAREST);
        GL.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MAG_FILTER, GL.NEAREST);
        GL.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MAX_LEVEL, 4);
        try (NativeImage image = NativeImage.load(location.toPath(ResourceLocation.ASSETS, ResourceLocation.TEXTURE) + ".png")) {
            final int format = switch (image.format()) {
                case STBImage.GREY -> GL.RED;
                case STBImage.GREY_ALPHA -> GL.RG;
                case STBImage.RGB -> GL.RGB;
                case STBImage.RGB_ALPHA -> GL.RGBA;
                default -> throw new IllegalStateException("Unexpected value: " + image.format());
            };
            GL.texImage2D(GL.TEXTURE_2D,
                0,
                format,
                image.width(),
                image.height(),
                0,
                format,
                GL.UNSIGNED_BYTE,
                image.data()
            );
        }
        GL.generateMipmap(GL.TEXTURE_2D);
        RenderSystem.bindTexture2D(textureBinding2D);
        return texture;
    }

    public int id() {
        return id;
    }

    @Override
    public void close() {
        GL.deleteTexture(id);
    }
}
