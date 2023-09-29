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
import cuboidx.world.block.BlockType;
import cuboidx.world.block.BlockTypes;
import overrungl.glfw.GLFW;

import java.lang.foreign.MemorySegment;

/**
 * @author squid233
 * @since 0.1.0
 */
// TODO: 2023/9/29 We will keep using inheriting until we have value objects.
public final class PlayerEntity extends Entity {
    public static final int MAX_HOT_BAR_INDEX = 2;
    private BlockType mainHandItem = BlockTypes.GRASS_BLOCK;
    private final BlockType[] hotBar = {
        BlockTypes.GRASS_BLOCK,
        BlockTypes.DIRT,
        BlockTypes.STONE
    };
    private int hotBarIndex = 0;

    public PlayerEntity() {
        super(EntityTypes.PLAYER);
    }

    public BlockType mainHandItem() {
        return mainHandItem;
    }

    public void setMainHandItem(BlockType mainHandItem) {
        this.mainHandItem = mainHandItem;
    }

    public void setHotBarTo(int index) {
        if (index >= 0 && index <= MAX_HOT_BAR_INDEX) {
            hotBarIndex = index;
            setMainHandItem(hotBar[index]);
        }
    }

    public int hotBarIndex() {
        return hotBarIndex;
    }

    @Override
    public void tick() {
        super.tick();
        final MemorySegment window = CuboidX.getInstance().window();
        double xo = 0.0, yo = 0.0, zo = 0.0;
        if (GLFW.getKey(window, GLFW.KEY_A) == GLFW.PRESS) xo--;
        if (GLFW.getKey(window, GLFW.KEY_D) == GLFW.PRESS) xo++;
        if (GLFW.getKey(window, GLFW.KEY_LEFT_SHIFT) == GLFW.PRESS) yo--;
        if (GLFW.getKey(window, GLFW.KEY_SPACE) == GLFW.PRESS) yo++;
        if (GLFW.getKey(window, GLFW.KEY_W) == GLFW.PRESS) zo--;
        if (GLFW.getKey(window, GLFW.KEY_S) == GLFW.PRESS) zo++;
        final boolean sprint = GLFW.getKey(window, GLFW.KEY_LEFT_CONTROL) == GLFW.PRESS;
        moveRelative(xo, yo, zo, sprint ? 0.8 : 0.6);
    }

    @Override
    public void reset() {
        super.reset();
        mainHandItem = BlockTypes.GRASS_BLOCK;
        hotBarIndex = 0;
    }
}
