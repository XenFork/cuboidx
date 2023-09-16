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

package cuboidx.client.render.world;

import cuboidx.client.render.BufferedVertexBuilder;
import org.overrun.pooling.KeyedObjectPool;
import org.overrun.pooling.KeyedPool;
import org.overrun.pooling.KeyedPoolObjectState;

/**
 * @author squid233
 * @since 0.1.0
 */
// TODO: 2023/7/8 Multithreading
public final class ChunkCompiler implements AutoCloseable {
    private final KeyedPool<BlockRenderLayer, BufferedVertexBuilder> pool = new KeyedObjectPool<>(
        layer -> new BufferedVertexBuilder(layer.layout(), layer.verticesSize(), layer.indicesSize()),
        BufferedVertexBuilder::close,
        1,
        8);

    public KeyedPoolObjectState<BlockRenderLayer, BufferedVertexBuilder> borrow(BlockRenderLayer layer) {
        return pool.borrow(layer).state();
    }

    public void returning(KeyedPoolObjectState<BlockRenderLayer, BufferedVertexBuilder> builder) {
        pool.returning(builder);
    }

    @Override
    public void close() {
        pool.cleanup();
    }
}
