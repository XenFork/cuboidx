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

import java.util.function.Supplier;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class DefaultedRegistry<T> extends BaseRegistry<T> {
    private final Supplier<T> lazyDefaultValue;
    private T defaultValue;

    public DefaultedRegistry(RegistryKey<T> registryKey, Supplier<T> lazyDefaultValue) {
        super(registryKey);
        this.lazyDefaultValue = lazyDefaultValue;
    }

    public T defaultValue() {
        if (defaultValue == null) {
            defaultValue = lazyDefaultValue.get();
        }
        return defaultValue;
    }

    @Override
    public T get(ResourceLocation location) {
        return idToEntry.getOrDefault(location, defaultValue());
    }

    @Override
    public T get(int rawId) {
        return rawIdToEntry.getOrDefault(rawId, defaultValue());
    }

    @Override
    public ResourceLocation getId(T entry) {
        return entryToId.getOrDefault(entry, entryToId.get(defaultValue()));
    }

    @Override
    public int getRawId(T entry) {
        return entryToRawId.getOrDefault(entry, entryToRawId.getOrDefault(defaultValue(), INVALID_RAW_ID));
    }
}
