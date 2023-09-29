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

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EntityType {
    private final boolean limitedPitch;
    private final double eyeHeight;
    private final Consumer<Entity> tick;

    private EntityType(boolean limitedPitch,
                       double eyeHeight,
                       Consumer<Entity> tick) {
        this.limitedPitch = limitedPitch;
        this.eyeHeight = eyeHeight;
        this.tick = tick;
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    public static final class Builder {
        private boolean limitedPitch = true;
        private double eyeHeight = 1.71;
        private Consumer<Entity> tick = entity -> entity.prevPosition().set(entity.position());

        public Builder limitedPitch(boolean limitedPitch) {
            this.limitedPitch = limitedPitch;
            return this;
        }

        public Builder eyeHeight(double eyeHeight) {
            this.eyeHeight = eyeHeight;
            return this;
        }

        public Builder tick(Function<Consumer<Entity>, Consumer<Entity>> tick) {
            this.tick = tick.apply(this.tick);
            return this;
        }

        public EntityType build() {
            return new EntityType(limitedPitch, eyeHeight, tick);
        }
    }

    public boolean limitedPitch() {
        return limitedPitch;
    }

    public double eyeHeight() {
        return eyeHeight;
    }

    public Consumer<Entity> tick() {
        return tick;
    }
}
