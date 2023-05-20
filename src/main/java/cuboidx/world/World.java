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

package cuboidx.world;

import cuboidx.world.block.BlockType;
import cuboidx.world.block.BlockTypes;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class World {
    private final int width;
    private final int height;
    private final int depth;
    private final BlockType[] blocks;

    public World(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.blocks = new BlockType[width * height * depth];
    }

    public boolean isInBound(int x, int y, int z) {
        return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
    }

    public BlockType getBlock(int x, int y, int z) {
        if (isInBound(x, y, z))
            return blocks[width * (y * depth + z) + x];
        return BlockTypes.AIR;
    }

    public void setBlock(int x, int y, int z, BlockType block) {
        if (isInBound(x, y, z)) {
            blocks[width * (y * depth + z) + x] = block;
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int depth() {
        return depth;
    }
}
