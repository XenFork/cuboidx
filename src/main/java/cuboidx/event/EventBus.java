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

package cuboidx.event;

import cuboidx.registry.RegistryKey;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EventBus {
    private final Map<RegistryKey<? extends Event<?>>, List<Consumer<? extends Event<?>>>> listeners = Collections.synchronizedMap(new HashMap<>());
    private final String name;

    public EventBus(String name) {
        this.name = name;
    }

    public EventBus() {
        this("Default");
    }

    public <T extends Event<T>> void addListener(RegistryKey<T> id, Consumer<T> listener) {
        listeners.computeIfAbsent(id, k -> Collections.synchronizedList(new ArrayList<>())).add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T extends Event<T>> void post(T t) {
        final var listenerList = listeners.get(t.id());
        if (listenerList != null) {
            for (var listener : listenerList) {
                ((Consumer<T>) listener).accept(t);
                if (t instanceof Cancellable cancellable && cancellable.canceled()) break;
            }
        }
    }

    public String name() {
        return name;
    }
}
