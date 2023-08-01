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

package cuboidx.world.block;

import cuboidx.util.ResourceLocation;
import cuboidx.util.math.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BlockType {
    private final boolean air;
    private final Map<Direction, ResourceLocation> texture; // TODO: Use Function instead of Map cache; use block state model instead of Function

    private BlockType(boolean air,
                      Function<Direction, ResourceLocation> texture) {
        this.air = air;
        if (air) {
            this.texture = Map.of();
        } else {
            this.texture = HashMap.newHashMap(6);
            for (Direction direction : Direction.list()) {
                this.texture.put(direction, texture.apply(direction));
            }
        }
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    public static final class Builder {
        private boolean air;
        private Function<Direction, ResourceLocation> texture;

        public Builder air() {
            this.air = true;
            return this;
        }

        public Builder texture(Function<Direction, ResourceLocation> texture) {
            this.texture = texture;
            return this;
        }

        public BlockType build() {
            return new BlockType(
                air,
                air ? null : Objects.requireNonNull(texture, "texture")
            );
        }
    }

    public boolean air() {
        return air;
    }

    public ResourceLocation texture(Direction direction) {
        return texture.get(direction);
    }
}
