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
import java.util.function.Supplier;

/**
 * An object pool.
 *
 * @param <T> the type of the instances
 * @author squid233
 * @since 0.1.0
 */
public final class Pool<T extends Poolable> {
    private final List<T> list;
    private final List<T> listView;
    // TODO: 2023/7/9 primitive generic
    private final Map<Integer, Boolean> availableId;
    private final Supplier<T> constructor;

    public Pool(int initialCapacity, Supplier<T> constructor) {
        this.list = Collections.synchronizedList(new ArrayList<>(initialCapacity));
        this.listView = Collections.unmodifiableList(this.list);
        this.availableId = Collections.synchronizedMap(HashMap.newHashMap(initialCapacity));
        this.constructor = constructor;
    }

    public @UnmodifiableView List<T> list() {
        return listView;
    }
}
