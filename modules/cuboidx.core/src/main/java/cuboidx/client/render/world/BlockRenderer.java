/*
 * cuboidx - A 3D sandbox game
 * Copyright (C) 2023  XenFork Union
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cuboidx.client.render.world;

import cuboidx.client.CuboidX;
import cuboidx.client.render.VertexBuilder;
import cuboidx.client.texture.TextureAtlas;
import cuboidx.util.ResourceLocation;
import cuboidx.util.math.Direction;
import cuboidx.world.World;
import cuboidx.world.block.BlockType;
import org.overrun.binpacking.PackerFitPos;
import org.overrun.binpacking.PackerRegionSize;

import java.util.Objects;
import java.util.Optional;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BlockRenderer {
    private final CuboidX client;

    public BlockRenderer(CuboidX client) {
        this.client = client;
    }

    // TODO: 2023/6/16 replace with Quad<Vec3>
    private static void renderBlockFace(VertexBuilder builder,
                                        float u0, float v0, float u1, float v1,
                                        float x0, float y0, float z0,
                                        float x1, float y1, float z1,
                                        float x2, float y2, float z2,
                                        float x3, float y3, float z3) {
        builder.vertex(x0, y0, z0).texture(u0, v0).emit();
        builder.vertex(x1, y1, z1).texture(u0, v1).emit();
        builder.vertex(x2, y2, z2).texture(u1, v1).emit();
        builder.vertex(x3, y3, z3).texture(u1, v0).emit();
    }

    public void renderBlockFace(VertexBuilder builder, BlockType block, int x, int y, int z, Direction face) {
        final TextureAtlas atlas = Objects.requireNonNull(client.textureManager().getAsAtlas(TextureAtlas.BLOCK_ATLAS));
        final ResourceLocation texture = block.texture(face);
        final Optional<PackerFitPos> optionalOffset = atlas.getOffset(texture);
        final Optional<PackerRegionSize> optionalSize;
        float u0, v0, u1, v1;
        if (optionalOffset.isEmpty() || (optionalSize = atlas.getSize(texture)).isEmpty()) {
            u0 = 0;
            v0 = 0;
            u1 = 0;
            v1 = 0;
        } else {
            final PackerFitPos offset = optionalOffset.get();
            final PackerRegionSize size = optionalSize.get();
            u0 = atlas.normalizeU(offset.x());
            v0 = atlas.normalizeV(offset.y());
            u1 = atlas.normalizeU(offset.x() + size.width());
            v1 = atlas.normalizeV(offset.y() + size.height());
        }
        float x0 = (float) x;
        float y0 = (float) y;
        float z0 = (float) z;
        float x1 = x0 + 1f;
        float y1 = y0 + 1f;
        float z1 = z0 + 1f;
        builder.indices(0, 1, 2, 2, 3, 0);
        builder.color(1f, 1f, 1f, 1f);
        switch (face) {
            case WEST -> renderBlockFace(builder,
                u0, v0, u1, v1,
                x0, y1, z0,
                x0, y0, z0,
                x0, y0, z1,
                x0, y1, z1);
            case EAST -> renderBlockFace(builder,
                u0, v0, u1, v1,
                x1, y1, z1,
                x1, y0, z1,
                x1, y0, z0,
                x1, y1, z0);
            case DOWN -> renderBlockFace(builder,
                u0, v0, u1, v1,
                x0, y0, z1,
                x0, y0, z0,
                x1, y0, z0,
                x1, y0, z1);
            case UP -> renderBlockFace(builder,
                u0, v0, u1, v1,
                x0, y1, z0,
                x0, y1, z1,
                x1, y1, z1,
                x1, y1, z0);
            case NORTH -> renderBlockFace(builder,
                u0, v0, u1, v1,
                x1, y1, z0,
                x1, y0, z0,
                x0, y0, z0,
                x0, y1, z0);
            case SOUTH -> renderBlockFace(builder,
                u0, v0, u1, v1,
                x0, y1, z1,
                x0, y0, z1,
                x1, y0, z1,
                x1, y1, z1);
        }
    }

    public void renderBlock(VertexBuilder builder, BlockType block, int x, int y, int z) {
        if (block.air()) return;

        for (Direction direction : Direction.list()) {
            renderBlockFace(builder, block, x, y, z, direction);
        }
    }

    public boolean shouldRenderFace(BlockType block, World world, int x, int y, int z, Direction face) {
        if (block.air()) return false;

        final int bx = x + face.axisX();
        final int by = y + face.axisY();
        final int bz = z + face.axisZ();
        if (world.isInBound(bx, by, bz)) {
            return world.getBlock(bx, by, bz).air();
        }
        return true;
    }
}
