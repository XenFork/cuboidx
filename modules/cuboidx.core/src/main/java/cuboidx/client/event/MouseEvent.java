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

package cuboidx.client.event;

import cuboidx.event.Event;
import cuboidx.registry.RegistryKey;
import cuboidx.registry.RegistryKeys;
import cuboidx.util.ResourceLocation;
import org.overrun.pooling.*;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author squid233
 * @since 0.1.0
 */
public sealed interface MouseEvent<T extends MouseEvent<T>> extends Event<T> {
    @SuppressWarnings("unchecked")
    private static <T> RegistryKey<T> root() {
        class Holder {
            private static final RegistryKey<?> ROOT = RegistryKey.of(RegistryKeys.root(), ResourceLocation.cuboidx("mouse_event"));
        }
        return (RegistryKey<T>) Holder.ROOT;
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    final /* value */ class CursorPos implements MouseEvent<CursorPos>, Poolable {
        public static final RegistryKey<CursorPos> ID = RegistryKey.of(MouseEvent.root(), ResourceLocation.cuboidx("cursor_pos"));
        private static final Pool<CursorPos> POOL = new ObjectPool<>(CursorPos::new);
        private double x;
        private double y;
        private double deltaX;
        private double deltaY;

        public static PoolObjectState<CursorPos> of(double x, double y, double deltaX, double deltaY) {
            final PoolObjectState<CursorPos> state = POOL.borrow().state();
            final CursorPos event = state.get();
            event.x = x;
            event.y = y;
            event.deltaX = deltaX;
            event.deltaY = deltaY;
            return state;
        }

        public static void free(PoolObjectState<CursorPos> result) {
            POOL.returning(result);
        }

        @Override
        public RegistryKey<CursorPos> id() {
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
            if (!(o instanceof MouseEvent.CursorPos pos)) return false;
            return Double.compare(x, pos.x) == 0 && Double.compare(y, pos.y) == 0 && Double.compare(deltaX, pos.deltaX) == 0 && Double.compare(deltaY, pos.deltaY) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, deltaX, deltaY);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", CursorPos.class.getSimpleName() + "[", "]")
                .add("x=" + x)
                .add("y=" + y)
                .add("deltaX=" + deltaX)
                .add("deltaY=" + deltaY)
                .toString();
        }
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    /* value */ record Button(int button, int action, int mods) implements MouseEvent<Button> {
        public static final RegistryKey<Button> ID = RegistryKey.of(MouseEvent.root(), ResourceLocation.cuboidx("button"));

        @Override
        public RegistryKey<Button> id() {
            return ID;
        }
    }
}
