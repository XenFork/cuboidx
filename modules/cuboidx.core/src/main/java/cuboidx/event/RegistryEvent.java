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

package cuboidx.event;

import cuboidx.registry.MutableRegistry;
import cuboidx.registry.Registry;
import cuboidx.registry.RegistryKey;
import cuboidx.registry.RegistryKeys;
import cuboidx.util.ResourceLocation;

/**
 * @param <T> the type of the registry entry
 * @author squid233
 * @since 0.1.0
 */
public record RegistryEvent<T>(MutableRegistry<T> registry) implements Event<RegistryEvent<T>> {
    private static final RegistryKey<?> ROOT = RegistryKey.of(RegistryKeys.root(), ResourceLocation.cuboidx("registry_event"));

    @SuppressWarnings("unchecked")
    public static <T> RegistryKey<RegistryEvent<T>> id(Registry<T> registry) {
        final ResourceLocation location = registry.registryKey().location();
        return RegistryKey.of((RegistryKey<RegistryEvent<T>>) ROOT, location);
    }

    @Override
    public RegistryKey<RegistryEvent<T>> id() {
        return id(registry);
    }
}
