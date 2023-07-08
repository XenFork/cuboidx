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

package cuboidx.util.pool;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Function;

/**
 * A pool, polls instances with a given key.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class MappedPool<K, T extends Poolable> {
    private final Map<K, List<T>> map;
    private final Map<K, List<T>> mapView;
    private final Map<K, Map<Integer, Boolean>> availableId;
    private final int initialCapacity;
    private final Function<K, T> constructor;

    public MappedPool(int numMappings, int initialCapacity, Function<K, T> constructor) {
        this.map = Collections.synchronizedMap(HashMap.newHashMap(numMappings));
        this.mapView = Collections.unmodifiableMap(this.map);
        this.availableId = Collections.synchronizedMap(HashMap.newHashMap(numMappings));
        this.initialCapacity = initialCapacity;
        this.constructor = constructor;
    }

    public MappedPool(Function<K, T> constructor) {
        this(32, 128, constructor);
    }

    public T poll(K key) {
        var list = map.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>(initialCapacity)));
        var idToState = availableId.computeIfAbsent(key, k -> Collections.synchronizedMap(HashMap.newHashMap(initialCapacity)));
        for (int i = 0, sz = list.size(); i < sz; i++) {
            T t = list.get(i);
            final Boolean b = idToState.get(i);
            if (b != null && b) {
                idToState.put(i, false);
                t.reset();
                return t;
            }
        }
        // no available instance. create it
        final T t = constructor.apply(key);
        list.add(t);
        idToState.put(list.size() - 1, false);
        return t;
    }

    public void free(K key, T t) {
        final List<T> list = map.get(key);
        if (list == null) return;
        final int i = list.indexOf(t);
        if (i == -1) return;
        availableId.get(key).put(i, true);
    }

    /**
     * {@return an unmodifiable view of the internal map}
     */
    public @UnmodifiableView Map<K, List<T>> map() {
        return mapView;
    }
}
