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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.overrun.binpacking.*;
import overrungl.opengl.GL;
import overrungl.util.MemoryStack;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class TextureAtlas extends Texture2D {
    private static final Logger logger = LogManager.getLogger();
    public static final ResourceLocation BLOCK_ATLAS = ResourceLocation.cuboidx("texture/atlas/blocks.png-atlas");
    private final Map<ResourceLocation, PackerRegion<?>> map;

    private TextureAtlas(ResourceLocation location, int width, int height, int mipmapLevel, int initialCapacity) {
        super(location, width, height, mipmapLevel);
        this.map = HashMap.newHashMap(initialCapacity);
    }

    public static TextureAtlas load(ResourceLocation name, ResourceLocation... locations) {
        return load(name, Set.of(locations));
    }

    public static TextureAtlas load(ResourceLocation name, Collection<ResourceLocation> locations) {
        final GrowingPacker packer = new GrowingPacker();
        final ArrayList<NativeImage.Region<ResourceLocation>> regions = new ArrayList<>();
        try {
            for (ResourceLocation location : locations) {
                regions.add(NativeImage.load(location.toPath(ResourceLocation.ASSETS, ResourceLocation.TEXTURE) + ".png")
                    .toRegion(location));
            }
            packer.fit(Packer.sort(regions));

            final int width = packer.width();
            final int height = packer.height();
            final TextureAtlas atlas;
            final int textureBinding2D = GLStateMgr.textureBinding2D();

            // compute mipmap level
            final int lvl;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                final MemorySegment seg = stack.ints(-1, 0);
                regions.forEach(region -> region.ifFitPresent((r, f) -> {
                    final int rw = r.width();
                    final int rh = r.height();
                    final int currLvl = seg.get(ValueLayout.JAVA_INT, 0);
                    final int newLvl = computeMipmapLevel(rw, rh);
                    if (currLvl == -1) {
                        seg.set(ValueLayout.JAVA_INT, 0, newLvl);
                        seg.set(ValueLayout.JAVA_INT, 4, newLvl);
                    } else if (newLvl < currLvl) {
                        logger.warn("Texture {} with size {}x{} limits mip level from {} to {}",
                            r.userdata(),
                            rw,
                            rh,
                            currLvl,
                            newLvl);
                        seg.set(ValueLayout.JAVA_INT, 0, newLvl);
                    }
                }));
                final int i = seg.get(ValueLayout.JAVA_INT, 0);
                final int maxLevel = seg.get(ValueLayout.JAVA_INT, 4);
                lvl = i == -1 ? 0 : i;
                if (lvl < maxLevel) {
                    logger.warn("{}: dropping mipmap level from {} to {}",
                        name,
                        maxLevel,
                        lvl);
                }
                atlas = new TextureAtlas(name, width, height, lvl, locations.size());
                RenderSystem.bindTexture2D(atlas);
                GL.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MIN_FILTER, GL.NEAREST_MIPMAP_NEAREST);
                GL.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MAG_FILTER, GL.NEAREST);
                GL.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MAX_LEVEL, lvl);
            }
            // write data
            GL.texImage2D(GL.TEXTURE_2D,
                0,
                GL.RGBA,
                width,
                height,
                0,
                GL.RGBA,
                GL.UNSIGNED_BYTE,
                MemorySegment.NULL
            );
            regions.forEach(region -> region.<NativeImage.Region<ResourceLocation>>ifFitPresent(
                (r, f) -> {
                    GL.texSubImage2D(GL.TEXTURE_2D,
                        0,
                        f.x(),
                        f.y(),
                        r.width(),
                        r.height(),
                        GL.RGBA,
                        GL.UNSIGNED_BYTE,
                        r.image().data()
                    );
                    atlas.map.put(r.userdata(), r);
                }));

            if (lvl > 0) GL.generateMipmap(GL.TEXTURE_2D);
            RenderSystem.bindTexture2D(textureBinding2D);
            return atlas;
        } finally {
            // release data
            regions.forEach(region -> region.image().close());
        }
    }

    public Optional<PackerFitPos> getOffset(ResourceLocation location) {
        if (map.containsKey(location))
            return map.get(location).fit();
        return Optional.empty();
    }

    public Optional<PackerRegionSize> getSize(ResourceLocation location) {
        return Optional.ofNullable(map.get(location));
    }

    public float normalizeU(int offset) {
        return (float) offset / (float) width();
    }

    public float normalizeV(int offset) {
        return (float) offset / (float) height();
    }
}
