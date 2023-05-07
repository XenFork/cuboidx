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

package cuboidx.util;

import org.jetbrains.annotations.Nullable;

/**
 * @author squid233
 * @since 0.1.0
 */
public record ResourceLocation(String namespace, String path) {
    public static final String DEFAULT_NAMESPACE = "cuboidx";
    public static final String ASSETS = "assets";

    public static ResourceLocation cuboidx(String path) {
        return new ResourceLocation(DEFAULT_NAMESPACE, path);
    }

    public static ResourceLocation of(String location) {
        final String[] split = location.split(":", 2);
        return switch (split.length) {
            case 0 -> cuboidx("");
            case 1 -> cuboidx(split[0]);
            default -> new ResourceLocation(split[0], split[1]);
        };
    }

    public String toPath(@Nullable String prefix) {
        if (prefix == null) return namespace + '/' + path;
        return prefix + '/' + namespace + '/' + path;
    }

    @Override
    public String toString() {
        return namespace + ':' + path;
    }
}
