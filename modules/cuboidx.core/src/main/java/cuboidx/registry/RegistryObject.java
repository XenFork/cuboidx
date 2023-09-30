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

import java.util.function.Supplier;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class RegistryObject<T> implements Supplier<T> {
    final String name;
    final Supplier<T> entrySupplier;
    T value;

    public RegistryObject(String name, Supplier<T> entrySupplier) {
        this.name = name;
        this.entrySupplier = entrySupplier;
    }

    @Override
    public T get() {
        return value;
    }
}
