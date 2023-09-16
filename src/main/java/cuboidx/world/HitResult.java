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

package cuboidx.world;

import cuboidx.world.block.BlockType;

/**
 * @author squid233
 * @since 0.1.0
 */
public final /* value */ class HitResult {
    private boolean missed;
    private int x, y, z;
    private BlockType block;

    public void update(boolean missed, int x, int y, int z, BlockType block) {
        this.missed = missed;
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
    }

    public boolean missed() {
        return missed;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public BlockType block() {
        return block;
    }
}
