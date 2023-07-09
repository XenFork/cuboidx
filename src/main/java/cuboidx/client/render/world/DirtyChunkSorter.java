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

package cuboidx.client.render.world;

import cuboidx.world.entity.PlayerEntity;
import org.joml.FrustumIntersection;

import java.util.Comparator;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class DirtyChunkSorter implements Comparator<ClientChunk> {
    private final PlayerEntity player;
    private final FrustumIntersection frustum;

    public DirtyChunkSorter(PlayerEntity player, FrustumIntersection frustum) {
        this.player = player;
        this.frustum = frustum;
    }

    @Override
    public int compare(ClientChunk o1, ClientChunk o2) {
        final boolean visible1 = o1.isVisible(frustum);
        final boolean visible2 = o2.isVisible(frustum);
        if (visible1 && !visible2) return -1;
        if (!visible1 && visible2) return 1;
        final double distanceSqr1 = o1.distanceSqr(player);
        final double distanceSqr2 = o2.distanceSqr(player);
        if (distanceSqr1 < distanceSqr2) return -1;
        if (distanceSqr1 > distanceSqr2) return 1;
        final long dirtyTime1 = o1.dirtyTime();
        final long dirtyTime2 = o2.dirtyTime();
        return Long.compare(dirtyTime2, dirtyTime1);
    }
}
