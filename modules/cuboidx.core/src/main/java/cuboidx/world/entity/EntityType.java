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

import java.util.function.Supplier;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EntityType<T extends Entity> {
    private final boolean limitedPitch;
    private final double eyeHeight;
    private final Supplier<T> constructor;

    private EntityType(boolean limitedPitch,
                       double eyeHeight,
                       Supplier<T> constructor) {
        this.limitedPitch = limitedPitch;
        this.eyeHeight = eyeHeight;
        this.constructor = constructor;
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    public static final class Builder {
        private boolean limitedPitch = true;
        private double eyeHeight = 1.71;

        public Builder limitedPitch(boolean limitedPitch) {
            this.limitedPitch = limitedPitch;
            return this;
        }

        public Builder eyeHeight(double eyeHeight) {
            this.eyeHeight = eyeHeight;
            return this;
        }

        public <T extends Entity> EntityType<T> build(Supplier<T> constructor) {
            return new EntityType<>(limitedPitch, eyeHeight, constructor);
        }
    }

    public boolean limitedPitch() {
        return limitedPitch;
    }

    public double eyeHeight() {
        return eyeHeight;
    }

    public Supplier<T> constructor() {
        return constructor;
    }
}
