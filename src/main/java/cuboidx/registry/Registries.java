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

package cuboidx.registry;

import cuboidx.util.ResourceLocation;
import cuboidx.world.block.BlockType;
import cuboidx.world.block.BlockTypes;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Registries {
    private static final MutableRegistry<Registry<?>> ROOT_REGISTRY = new BaseRegistry<>(RegistryKey.ROOT, 2);
    public static final Registry<Registry<?>> ROOT_REGISTRY_VIEW = ROOT_REGISTRY;
    public static final DefaultedRegistry<BlockType> BLOCK_TYPE = of("block_type", () -> BlockTypes.AIR);
    public static final RegistryKey BLOCK_TYPE_KEY = BLOCK_TYPE.registryKey();

    static {
        ROOT_REGISTRY.add(RegistryKey.ROOT.location(), ROOT_REGISTRY);
    }

    public static <T, R extends T> R register(MutableRegistry<T> registry, ResourceLocation location, R entry) {
        return registry.add(location, entry);
    }

    public static <T, R extends T> R register(MutableRegistry<T> registry, String location, R entry) {
        return register(registry, ResourceLocation.of(location), entry);
    }

    public static <T, R extends Registry<T>> R of(ResourceLocation location, Function<RegistryKey, R> registry) {
        final RegistryKey key = RegistryKey.ROOT.child(location);
        return ROOT_REGISTRY.add(key.location(), registry.apply(key));
    }

    public static <T> DefaultedRegistry<T> of(ResourceLocation location, Supplier<T> defaultValue) {
        return of(location, k -> new DefaultedRegistry<>(k, defaultValue));
    }

    private static <T> DefaultedRegistry<T> of(String name, Supplier<T> defaultValue) {
        return of(ResourceLocation.cuboidx(name), defaultValue);
    }
}
