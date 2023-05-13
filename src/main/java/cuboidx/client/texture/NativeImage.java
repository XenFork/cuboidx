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

package cuboidx.client.texture;

import cuboidx.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.overrun.glib.stb.STBImage;
import org.overrun.glib.util.MemoryStack;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * @author squid233
 * @since 0.1.0
 */
public /* value */ record NativeImage(int width, int height, MemorySegment data, boolean failed, int format)
    implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger();
    private static NativeImage failImage;

    public static NativeImage fail() {
        if (failImage == null) {
            final MemorySegment data = Arena.global().allocateArray(ValueLayout.JAVA_INT, 16 * 16);
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    data.setAtIndex(ValueLayout.JAVA_INT,
                        y * 16 + x,
                        (x >= 8) ^ (y >= 8) ? 0xff000000 : 0xfff800f8);
                }
            }
            failImage = new NativeImage(16, 16, data, true, STBImage.RGB_ALPHA);
        }
        return failImage;
    }

    public static NativeImage load(String path) {
        return load(path, STBImage.RGB_ALPHA);
    }

    public static NativeImage load(String path, int channels) {
        final MemorySegment segment = FileUtil.readBinary(path, 8192);
        if (segment == null) return null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final MemorySegment px = stack.calloc(ValueLayout.JAVA_INT);
            final MemorySegment py = stack.calloc(ValueLayout.JAVA_INT);
            final MemorySegment pc = stack.calloc(ValueLayout.JAVA_INT);
            final MemorySegment data = STBImage.loadFromMemory(segment, px, py, pc, channels);
            if (data == null) {
                logger.error("Failed to load the image '{}': {}", path, STBImage.failureReason());
                return fail();
            }
            return new NativeImage(
                px.get(ValueLayout.JAVA_INT, 0),
                py.get(ValueLayout.JAVA_INT, 0),
                data,
                false,
                channels
            );
        }
    }

    @Override
    public void close() {
        if (!failed) STBImage.free(data);
    }
}
