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

package cuboidx.client.event;

import cuboidx.registry.RegistryKey;
import cuboidx.registry.RegistryKeys;
import cuboidx.util.ResourceLocation;
import cuboidx.util.pool.Pool;
import cuboidx.util.pool.Poolable;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author squid233
 * @since 0.1.0
 */
public sealed interface CursorEvent<T extends CursorEvent<T>> extends Event<T>, Poolable {
    @SuppressWarnings("unchecked")
    private static <T> RegistryKey<T> root() {
        class Holder {
            private static final RegistryKey<?> ROOT = RegistryKey.of(RegistryKeys.root(), ResourceLocation.cuboidx("cursor_event"));
        }
        return (RegistryKey<T>) Holder.ROOT;
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    final class Pos implements CursorEvent<Pos> {
        public static final RegistryKey<Pos> ID = RegistryKey.of(CursorEvent.root(), ResourceLocation.cuboidx("pos"));
        private static final Pool<Pos> POOL = new Pool<>(Pos::new);
        private double x;
        private double y;
        private double deltaX;
        private double deltaY;

        public static Pos of(double x, double y, double deltaX, double deltaY) {
            final Pos event = POOL.poll();
            event.x = x;
            event.y = y;
            event.deltaX = deltaX;
            event.deltaY = deltaY;
            return event;
        }

        public void free() {
            POOL.free(this);
        }

        @Override
        public RegistryKey<Pos> id() {
            return ID;
        }

        @Override
        public void reset() {
            x = 0;
            y = 0;
            deltaX = 0;
            deltaY = 0;
        }

        public double x() {
            return x;
        }

        public double y() {
            return y;
        }

        public double deltaX() {
            return deltaX;
        }

        public double deltaY() {
            return deltaY;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pos pos)) return false;
            return Double.compare(x, pos.x) == 0 && Double.compare(y, pos.y) == 0 && Double.compare(deltaX, pos.deltaX) == 0 && Double.compare(deltaY, pos.deltaY) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, deltaX, deltaY);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Pos.class.getSimpleName() + "[", "]")
                .add("x=" + x)
                .add("y=" + y)
                .add("deltaX=" + deltaX)
                .add("deltaY=" + deltaY)
                .toString();
        }
    }
}
