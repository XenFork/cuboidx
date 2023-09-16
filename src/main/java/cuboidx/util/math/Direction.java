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

package cuboidx.util.math;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
public enum Direction {
    WEST("West", 0, 1, -1, 0, 0),
    EAST("East", 1, 0, 1, 0, 0),
    DOWN("Down", 2, 3, 0, -1, 0),
    UP("Up", 3, 2, 0, 1, 0),
    NORTH("North", 4, 5, 0, 0, -1),
    SOUTH("South", 5, 4, 0, 0, 1);

    private static final Direction[] VALUES = values();
    private static final List<Direction> LIST = List.of(VALUES);
    private final String toStringValue;
    private final int id;
    private final int oppositeId;
    private final int axisX;
    private final int axisY;
    private final int axisZ;

    Direction(String toStringValue, int id, int oppositeId, int axisX, int axisY, int axisZ) {
        this.toStringValue = toStringValue;
        this.id = id;
        this.oppositeId = oppositeId;
        this.axisX = axisX;
        this.axisY = axisY;
        this.axisZ = axisZ;
    }

    public static Direction byId(int id) {
        return VALUES[id];
    }

    /**
     * {@return an unmodifiable and cached view of the values}
     */
    public static @UnmodifiableView List<Direction> list() {
        return LIST;
    }

    public int id() {
        return id;
    }

    public int oppositeId() {
        return oppositeId;
    }

    public Direction opposite() {
        return byId(oppositeId());
    }

    public int axisX() {
        return axisX;
    }

    public int axisY() {
        return axisY;
    }

    public int axisZ() {
        return axisZ;
    }

    @Override
    public String toString() {
        return toStringValue;
    }
}
