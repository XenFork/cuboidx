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

package cuboidx.client.render;

import cuboidx.world.entity.Entity;
import org.joml.Vector3d;
import org.overrun.timer.Timer;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Camera {
    private final Vector3d prevPosition = new Vector3d();
    private final Vector3d position = new Vector3d();
    private final Vector3d lerpPosition = new Vector3d();
    private final Vector3d rotation = new Vector3d();

    public void tick() {
        prevPosition.set(position);
    }

    public void setPosition(double x, double y, double z) {
        position.set(x, y, z);
    }

    public void moveToEntity(Entity entity) {
        position.set(entity.position());
        rotation.set(entity.rotation());
    }

    /**
     * Linearly interpolate to the position vector of this camera.
     *
     * @param partialTick see {@link Timer#partialTick()}.
     */
    public void lerp(double partialTick) {
        prevPosition.lerp(position, partialTick, lerpPosition);
    }

    public Vector3d prevPosition() {
        return prevPosition;
    }

    public Vector3d position() {
        return position;
    }

    public Vector3d lerpPosition() {
        return lerpPosition;
    }

    public Vector3d rotation() {
        return rotation;
    }
}
