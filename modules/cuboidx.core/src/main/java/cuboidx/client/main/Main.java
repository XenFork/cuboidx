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

package cuboidx.client.main;

import cuboidx.client.CuboidX;
import cuboidx.util.Log4jStream;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Main {
    public static void main(String[] args) {
        System.setOut(new Log4jStream(System.out, false));
        System.setErr(new Log4jStream(System.err, true));
        CuboidX.getInstance().bootstrap();
    }
}
