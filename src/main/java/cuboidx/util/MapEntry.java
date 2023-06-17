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

package cuboidx.util;

import java.util.Map;

/**
 * A wrapper of {@link java.util.Map.Entry} for type patterns.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 * @author squid233
 * @since 0.1.0
 * @deprecated This type is deprecated until value objects are added.
 */
@Deprecated(since = "0.1.0")
public /* value */ record MapEntry<K, V>(K key, V value) implements Map.Entry<K, V> {
    @Override
    public K getKey() {
        return key();
    }

    @Override
    public V getValue() {
        return value();
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public String toString() {
        return key() + "=" + value();
    }
}
