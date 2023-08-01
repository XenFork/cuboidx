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

import java.util.Map;

/**
 * @author squid233
 * @since 0.1.0
 */
public interface Registry<T> extends Iterable<Map.Entry<ResourceLocation, T>> {
    /**
     * An invalid raw identifier. Writing to this raw identifier is considered as invalid and returns {@code null}
     * or default value.
     */
    int INVALID_RAW_ID = -1;

    RegistryKey<T> registryKey();

    T get(ResourceLocation location);

    T get(int rawId);

    ResourceLocation getId(T entry);

    int getRawId(T entry);

    int size();
}
