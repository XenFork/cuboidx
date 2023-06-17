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

import cuboidx.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class TextureManager implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger();
    private final Map<ResourceLocation, Texture2D> textureMap = HashMap.newHashMap(48);

    public boolean contains(ResourceLocation location) {
        return textureMap.containsKey(location);
    }

    public <T extends Texture2D> T add(ResourceLocation location, T texture) {
        textureMap.put(location, texture);
        return texture;
    }

    public Texture2D get(ResourceLocation location) {
        return textureMap.get(location);
    }

    public TextureAtlas getAsAtlas(ResourceLocation location) {
        return get(location) instanceof TextureAtlas atlas ? atlas : null;
    }

    @Override
    public void close() {
        textureMap.values().forEach(Texture2D::close);
        logger.info("Cleaned up TextureManager");
    }
}
