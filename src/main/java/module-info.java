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

/**
 * @author squid233
 * @since 0.1.0
 */
module cuboidx.core {
    exports cuboidx.client.gl;
    exports cuboidx.client.main;
    exports cuboidx.client.render.world;
    exports cuboidx.client.render;
    exports cuboidx.client.texture;
    exports cuboidx.client;
    exports cuboidx.event;
    exports cuboidx.registry;
    exports cuboidx.util.math;
    exports cuboidx.util.pool;
    exports cuboidx.util;
    exports cuboidx.world.block;
    exports cuboidx.world.chunk;
    exports cuboidx.world.entity;
    exports cuboidx.world;

    requires overrungl.core;
    requires overrungl.glfw;
    requires overrungl.joml;
    requires overrungl.opengl;
    requires overrungl.stb;
    requires org.overrun.binpacking;
    requires org.overrun.bintag;
    requires org.overrun.timer;
    requires org.overrun.unifont;

    requires com.google.gson;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires org.joml;
    requires static org.jetbrains.annotations;
    requires java.desktop;
}
