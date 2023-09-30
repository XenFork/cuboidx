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

package cuboidx.world.entity;

import cuboidx.world.World;
import org.joml.Math;
import org.joml.Vector3d;
import org.overrun.pooling.Poolable;

import java.util.UUID;

/**
 * @author squid233
 * @since 0.1.0
 */
public class Entity implements Poolable {
    private final EntityType<?> type;
    private World world;
    private UUID uuid;
    private final Vector3d prevPosition = new Vector3d();
    private final Vector3d position = new Vector3d();
    private final Vector3d rotation = new Vector3d();

    public Entity(EntityType<?> type) {
        this.type = type;
    }

    public void tick() {
        prevPosition().set(position());
    }

    public void spawn(double x, double y, double z) {
        prevPosition().set(x, y, z);
        setPosition(x, y, z);
    }

    public void setPosition(double x, double y, double z) {
        position().set(x, y, z);
    }

    public void move(double x, double y, double z) {
        position().add(x, y, z);
    }

    public void moveRelative(double x, double y, double z, double speed) {
        final double yaw = rotation().y();
        final double sin = Math.sin(yaw);
        final double cos = Math.cosFromSin(sin, yaw);
        move((z * sin + x * cos) * speed, y * speed, (z * cos - x * sin) * speed);
    }

    public void rotate(double x, double y, double z) {
        rotation().add(x, y, z);
        final double yaw = rotation().y();
        if (yaw < 0) rotation().y = 2 * Math.PI + yaw;
        if (yaw > 2 * Math.PI) rotation().y = 2 * Math.PI - yaw;
        if (type().limitedPitch()) {
            // TODO: 2023/8/1 Replace with PI_OVER_2
            rotation().x = Math.clamp(-Math.PI * 0.5, Math.PI * 0.5, rotation().x());
        }
    }

    @Override
    public void reset() {
        world = null;
        uuid = null;
        prevPosition.zero();
        position.zero();
        rotation.zero();
    }

    public EntityType<?> type() {
        return type;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public World world() {
        return world;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID uuid() {
        return uuid;
    }

    public Vector3d prevPosition() {
        return prevPosition;
    }

    public Vector3d position() {
        return position;
    }

    public Vector3d rotation() {
        return rotation;
    }
}
