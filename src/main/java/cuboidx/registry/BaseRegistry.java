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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author squid233
 * @since 0.1.0
 */
public class BaseRegistry<T> implements MutableRegistry<T> {
    private static final Logger logger = LogManager.getLogger();
    // TODO: 2023/6/17 primitive generic
    private final RegistryKey<T> registryKey;
    protected final Map<ResourceLocation, T> idToEntry;
    protected final Map<T, ResourceLocation> entryToId;
    protected final Map<Integer, T> rawIdToEntry;
    protected final Map<T, Integer> entryToRawId;
    private int nextId = 0;

    public BaseRegistry(RegistryKey<T> registryKey, int initialCapacity) {
        this.registryKey = registryKey;
        idToEntry = HashMap.newHashMap(initialCapacity);
        entryToId = HashMap.newHashMap(initialCapacity);
        rawIdToEntry = HashMap.newHashMap(initialCapacity);
        entryToRawId = HashMap.newHashMap(initialCapacity);
    }

    public BaseRegistry(RegistryKey<T> registryKey) {
        this(registryKey, 256);
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<ResourceLocation, T>> iterator() {
        return idToEntry.entrySet().iterator();
    }

    @Override
    public RegistryKey<T> registryKey() {
        return registryKey;
    }

    @Override
    public T get(ResourceLocation location) {
        return idToEntry.get(location);
    }

    @Override
    public T get(int rawId) {
        return rawIdToEntry.get(rawId);
    }

    @Override
    public ResourceLocation getId(T entry) {
        return entryToId.get(entry);
    }

    @Override
    public int getRawId(T entry) {
        return entryToRawId.getOrDefault(entry, INVALID_RAW_ID);
    }

    @Override
    public int size() {
        return idToEntry.size();
    }

    @Override
    public <R extends T> R set(int rawId, ResourceLocation location, R entry, boolean force) {
        if (rawId == INVALID_RAW_ID) {
            logger.warn("Invalid raw id. Ignoring.");
            return entry;
        }

        if (rawIdToEntry.containsKey(rawId)) {
            if (force) {
                logger.warn("Forcing to override existing entry with id {} ({})", location, rawId);
            } else {
                throw new IllegalStateException("Couldn't override existing entry with id " + location + "(" + rawId + ")");
            }
        }
        idToEntry.put(location, entry);
        entryToId.put(entry, location);
        rawIdToEntry.put(rawId, entry);
        entryToRawId.put(entry, rawId);
        nextId = Math.max(nextId, rawId);
        return entry;
    }

    @Override
    public <R extends T> R set(int rawId, ResourceLocation location, R entry) {
        return set(rawId, location, entry, false);
    }

    @Override
    public <R extends T> R add(ResourceLocation location, R entry) {
        set(nextId, location, entry);
        nextId++;
        return entry;
    }

    @Override
    public T remove(int rawId) {
        if (rawId == INVALID_RAW_ID) {
            logger.warn("Invalid raw id. Ignoring.");
            return null;
        }

        final T removed = rawIdToEntry.remove(rawId);
        if (removed == null) throw new IllegalStateException("No specified entry for id " + rawId);

        final ResourceLocation id = getId(removed);
        logger.info("Removing entry {} ({})", id, rawId);
        entryToId.remove(idToEntry.remove(id));
        entryToRawId.remove(removed);
        return removed;
    }
}
