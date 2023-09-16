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

package cuboidx.world.chunk;

import cuboidx.world.World;

/**
 * @author squid233
 * @since 0.1.0
 */
public class Chunk {
    public static final int SIZE = 32;
    private final World world;
    private final double x, y, z;
    private final int x0, y0, z0;
    private final int x1, y1, z1;

    public Chunk(World world,
                 double x, double y, double z,
                 int x0, int y0, int z0,
                 int x1, int y1, int z1) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
    }

    public World world() {
        return world;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public int x0() {
        return x0;
    }

    public int y0() {
        return y0;
    }

    public int z0() {
        return z0;
    }

    public int x1() {
        return x1;
    }

    public int y1() {
        return y1;
    }

    public int z1() {
        return z1;
    }
}
