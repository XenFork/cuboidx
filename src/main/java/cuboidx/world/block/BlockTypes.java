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

package cuboidx.world.block;

import cuboidx.util.ResourceLocation;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BlockTypes {
    public static final BlockType GRASS_BLOCK = new BlockType.Builder()
        .texture(direction -> ResourceLocation.cuboidx("block/grass_block_top"))
        .build();

    private BlockTypes() {
        //no instance
    }
}
