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

package cuboidx.world.block;

import cuboidx.registry.Registries;
import cuboidx.util.ResourceLocation;
import cuboidx.util.math.AABBox;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BlockTypes {
    public static final BlockType AIR = of(0, "air", new BlockType.Builder().air().outlineShape(AABBox::empty));
    public static final BlockType GRASS_BLOCK = of(1, "grass_block", new BlockType.Builder()
        .texture(direction -> switch (direction) {
            case DOWN -> ResourceLocation.cuboidx("block/dirt");
            case UP -> ResourceLocation.cuboidx("block/grass_block_top");
            default -> ResourceLocation.cuboidx("block/grass_block_side");
        })
    );
    public static final BlockType DIRT = of(2, "dirt", new BlockType.Builder()
        .texture(direction -> ResourceLocation.cuboidx("block/dirt"))
    );
    public static final BlockType STONE = of(3, "stone", new BlockType.Builder()
        .texture(direction -> ResourceLocation.cuboidx("block/stone"))
    );
    public static final BlockType COBBLESTONE = of(4, "cobblestone", new BlockType.Builder()
        .texture(direction -> ResourceLocation.cuboidx("block/cobblestone"))
    );
    public static final BlockType OAK_LOG = of(5, "oak_log", new BlockType.Builder()
        .texture(direction -> switch (direction) {
            case UP, DOWN -> ResourceLocation.cuboidx("block/oak_log_top");
            default -> ResourceLocation.cuboidx("block/oak_log_side");
        })
    );
    public static final BlockType OAK_LEAVES = of(6, "oak_leaves", new BlockType.Builder()
        .texture(direction -> ResourceLocation.cuboidx("block/oak_leaves"))
    );
    public static final BlockType FACING_INDICATOR = of(7, "facing_indicator", new BlockType.Builder()
        .texture(direction -> switch (direction) {
            case WEST -> ResourceLocation.cuboidx("block/facing_indicator_west");
            case EAST -> ResourceLocation.cuboidx("block/facing_indicator_east");
            case DOWN -> ResourceLocation.cuboidx("block/facing_indicator_down");
            case UP -> ResourceLocation.cuboidx("block/facing_indicator_up");
            case NORTH -> ResourceLocation.cuboidx("block/facing_indicator_north");
            case SOUTH -> ResourceLocation.cuboidx("block/facing_indicator_south");
        })
    );

    private BlockTypes() {
        //no instance
    }

    public static void load() {
    }

    private static BlockType of(int rawId, String name, BlockType.Builder builder) {
        return Registries.BLOCK_TYPE.set(rawId, ResourceLocation.cuboidx(name), builder.build());
    }
}
