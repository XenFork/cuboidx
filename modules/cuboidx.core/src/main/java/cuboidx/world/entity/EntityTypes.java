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

package cuboidx.world.entity;

import cuboidx.client.CuboidX;
import cuboidx.registry.Registries;
import cuboidx.util.ResourceLocation;
import overrungl.glfw.GLFW;

import java.lang.foreign.MemorySegment;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EntityTypes {
    public static final EntityType PLAYER = of(1, "player", new EntityType.Builder()
        .tick(tick -> entity -> {
            tick.accept(entity);
            playerTick(entity);
        }));

    private EntityTypes() {
        //no instance
    }

    public static void load() {
    }

    private static EntityType of(int rawId, String name, EntityType.Builder builder) {
        return Registries.ENTITY_TYPE.set(rawId, ResourceLocation.cuboidx(name), builder.build());
    }

    private static void playerTick(Entity entity) {
        final MemorySegment window = CuboidX.getInstance().window();
        double xo = 0.0, yo = 0.0, zo = 0.0;
        if (GLFW.getKey(window, GLFW.KEY_A) == GLFW.PRESS) xo--;
        if (GLFW.getKey(window, GLFW.KEY_D) == GLFW.PRESS) xo++;
        if (GLFW.getKey(window, GLFW.KEY_LEFT_SHIFT) == GLFW.PRESS) yo--;
        if (GLFW.getKey(window, GLFW.KEY_SPACE) == GLFW.PRESS) yo++;
        if (GLFW.getKey(window, GLFW.KEY_W) == GLFW.PRESS) zo--;
        if (GLFW.getKey(window, GLFW.KEY_S) == GLFW.PRESS) zo++;
        final boolean sprint = GLFW.getKey(window, GLFW.KEY_LEFT_CONTROL) == GLFW.PRESS;
        entity.moveRelative(xo, yo, zo, sprint ? 0.8 : 0.6);
    }
}
