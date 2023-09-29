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

package cuboidx.world;

import cuboidx.world.block.BlockType;
import cuboidx.world.block.BlockTypes;
import cuboidx.world.entity.Entity;
import cuboidx.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.overrun.pooling.KeyedObjectPool;
import org.overrun.pooling.KeyedPool;
import org.overrun.pooling.KeyedPoolObjectState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class World {
    private final int width;
    private final int height;
    private final int depth;
    private final BlockType[] blocks;
    private final KeyedPool<EntityType, Entity> entityPool = new KeyedObjectPool<>(Entity::new);
    private final List<Entity> entities = new ArrayList<>();
    private final List<WorldListener> listeners = new ArrayList<>();

    public World(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.blocks = new BlockType[width * height * depth];
        Arrays.fill(this.blocks, BlockTypes.AIR);
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                // generate height-map
                int noise = (int) Math.floor(SimplexOctaves.sumOctaves(8, x, z, 0.5f, 0.001f, -40f, 40f));
                for (int y = 64 + noise; y >= 0; y--) {
                    initBlock(x, y, z, BlockTypes.STONE);
                }
                for (int y = 64 + noise, y0 = 61 + noise; y >= y0; y--) {
                    initBlock(x, y, z, BlockTypes.DIRT);
                }
                initBlock(x, 64 + noise, z, BlockTypes.GRASS_BLOCK);
            }
        }
    }

    public void addListener(WorldListener listener) {
        listeners.add(listener);
    }

    public KeyedPoolObjectState<EntityType, Entity> spawn(EntityType entityType, double x, double y, double z) {
        final var state = entityPool.borrow(entityType).state();
        final Entity entity = state.get();
        entity.setWorld(this);
        entity.setUuid(UUID.randomUUID());
        entity.spawn(x, y, z);
        entities.add(entity);
        return state;
    }

    public @Nullable Entity findEntity(UUID uuid) {
        for (Entity entity : entities) {
            if (uuid.equals(entity.uuid())) return entity;
        }
        return null;
    }

    public void removeEntity(KeyedPoolObjectState<EntityType, Entity> entity) {
        entities.remove(entity.get());
        entityPool.returning(entity);
    }

    public boolean isInBound(int x, int y, int z) {
        return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
    }

    public BlockType getBlock(int x, int y, int z) {
        if (isInBound(x, y, z))
            return blocks[width * (y * depth + z) + x];
        return BlockTypes.AIR;
    }

    private boolean initBlock(int x, int y, int z, BlockType block) {
        if (isInBound(x, y, z)) {
            blocks[width * (y * depth + z) + x] = block;
            return true;
        }
        return false;
    }

    public void setBlock(int x, int y, int z, BlockType block) {
        if (initBlock(x, y, z, block)) {
            for (WorldListener listener : listeners) {
                listener.onBlockChanged(x, y, z, block);
            }
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int depth() {
        return depth;
    }
}
