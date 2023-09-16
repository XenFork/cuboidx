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

package cuboidx.registry;

import cuboidx.util.ResourceLocation;
import cuboidx.world.block.BlockType;
import cuboidx.world.entity.EntityType;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class RegistryKeys {
    private static final RegistryKey<?> ROOT = new RegistryKey<>(null, ResourceLocation.cuboidx("root"));
    public static final RegistryKey<BlockType> BLOCK_TYPE = of("block_type");
    public static final RegistryKey<EntityType> ENTITY_TYPE = of("entity_type");

    @SuppressWarnings("unchecked")
    public static <T> RegistryKey<T> root() {
        return (RegistryKey<T>) ROOT;
    }

    private static <T> RegistryKey<T> of(String name) {
        return RegistryKey.of(root(), ResourceLocation.cuboidx(name));
    }
}
