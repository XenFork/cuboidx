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

package cuboidx.world.entity;

import org.joml.Vector3d;

/**
 * @author squid233
 * @since 0.1.0
 */
public class PlayerEntity {
    private final Vector3d prevPosition = new Vector3d();
    private final Vector3d position = new Vector3d();
    private double eyeHeight = 1.68;

    public PlayerEntity() {
    }

    public void tick() {
        prevPosition.set(position);
    }

    public void setPosition(double x, double y, double z) {
        position.set(x, y, z);
    }

    public void move(double x, double y, double z) {
        position.add(x, y, z);
    }

    public void moveRelative(double x, double y, double z, double speed) {
        move(x * speed, y * speed, z * speed);
    }

    public Vector3d prevPosition() {
        return prevPosition;
    }

    public Vector3d position() {
        return position;
    }

    public double eyeHeight() {
        return eyeHeight;
    }
}
