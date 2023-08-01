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

import cuboidx.event.EventBus;
import cuboidx.event.RegistryEvent;
import cuboidx.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * <h2>Example</h2>
 * {@snippet lang=java:
 * DeferredRegistry<BlockType> BLOCKS = DeferredRegistry.of(EVENT_BUS, Registries.BLOCK_TYPE, "example");
 * RegistryObject<BlockType> MY_BLOCK = BLOCKS.register("my_block", () -> new BlockType.Builder().build());
 * }
 *
 * @author squid233
 * @since 0.1.0
 */
public final class DeferredRegistry<T> {
    private final MutableRegistry<T> registry;
    private final String namespace;
    private final List<RegistryObject<T>> entries = new ArrayList<>();

    private DeferredRegistry(MutableRegistry<T> registry, String namespace) {
        this.registry = registry;
        this.namespace = namespace;
    }

    public static <T> DeferredRegistry<T> of(EventBus eventBus, MutableRegistry<T> registry, String namespace) {
        final DeferredRegistry<T> registry1 = new DeferredRegistry<>(registry, namespace);
        eventBus.addListener(RegistryEvent.id(registry), event -> registry1.registerAll());
        return registry1;
    }

    public RegistryObject<T> register(String name, Supplier<T> entry) {
        final RegistryObject<T> object = new RegistryObject<>(name, entry);
        entries.add(object);
        return object;
    }

    private void registerAll() {
        for (RegistryObject<T> entry : entries) {
            entry.value = Registries.register(registry, ResourceLocation.of(namespace, entry.name), entry.entrySupplier.get());
        }
    }
}
