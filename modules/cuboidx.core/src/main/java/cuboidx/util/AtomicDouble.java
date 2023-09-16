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

package cuboidx.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class AtomicDouble {
    public static AtomicLong of() {
        return new AtomicLong();
    }

    public static void set(AtomicLong atomicLong, double value) {
        atomicLong.set(Double.doubleToLongBits(value));
    }

    public static double get(AtomicLong atomicLong) {
        return Double.longBitsToDouble(atomicLong.get());
    }
}
